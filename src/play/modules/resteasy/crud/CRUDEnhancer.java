/*
    This file is part of resteasy-crud-play-module.
    
    Copyright Lunatech Research 2010

    resteasy-crud-play-module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    resteasy-crud-play-module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Lesser Public License
    along with resteasy-crud-play-module.  If not, see <http://www.gnu.org/licenses/>.
*/
package play.modules.resteasy.crud;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.links.AddLinks;
import org.jboss.resteasy.links.ELProvider;
import org.jboss.resteasy.links.LinkELProvider;
import org.jboss.resteasy.links.LinkResource;

import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.db.Model;
import play.db.Model.Factory;
import play.db.Model.Manager;
import play.db.Model.Property;

/**
 * Enhance JAX-RS classes by giving them code if they don't already have them.
 */
public class CRUDEnhancer extends Enhancer {

	public static class Signature {
		public String name;
		public Class<?> returnClass;
		public List<Param> parameters = new ArrayList<Param>();
		public List<AnnotationRef<?>> annotations = new ArrayList<AnnotationRef<?>>();

		public Signature(Class<?> returnClass, String name, AnnotationRef<?>... annotations){
			this.name = name;
			this.returnClass = returnClass;
			for(AnnotationRef<?> a : annotations)
				this.annotations.add(a);
		}
		
		public Signature param(Class<?> type, String name, AnnotationRef<?>... annotations){
			parameters.add(new Param(type, name, annotations));
			return this;
		}
		
		public String signature(){
			StringBuilder ret = new StringBuilder("(");
			ret.append(typeSignature(returnClass));
			ret.append(")");
			boolean first = true;
			for(Param param : parameters){
				if(first)
					first = false;
				else
					ret.append(",");
				ret.append(typeSignature(param.type));
			}
			return ret.toString();
		}
		
		public String decl(){
			StringBuilder ret = new StringBuilder("public ");
			ret.append(t(returnClass));
			ret.append(" ").append(name).append("(");
			boolean first = true;
			for(Param param : parameters){
				if(first)
					first = false;
				else
					ret.append(",");
				ret.append(t(param.type)).append(" ").append(param.name);
			}
			ret.append(")");
			return ret.toString();
		}
		
		public CtMethod method(CtClass ctClass, Class<?> crudModel) throws CannotCompileException, NotFoundException{
			StringBuilder ret = new StringBuilder(decl());
			ret.append("{ return super.").append(name).append("(");
			ret.append(t(crudModel)).append(".class");
			for(Param param : parameters){
				ret.append(", ").append(param.name);
			}
			ret.append("); }");
			// make the method
			CtMethod method = CtMethod.make(ret.toString(), 
					ctClass);
			// now add the annotations
			return annotate(method);
		}

		private CtMethod annotate(CtMethod method) throws NotFoundException {
			ConstPool cp = method.getDeclaringClass().getClassFile().getConstPool();
			// parameters
			ParameterAnnotationsAttribute paramAnnotations = getParameterAnnotations(method);
			for(int i=0;i<parameters.size();i++){
				parameters.get(i).createAnnotations(i, paramAnnotations, cp);
			}
			// and method
			AnnotationsAttribute annotations = getAnnotations(method);
			for(AnnotationRef<?> a : this.annotations)
				createAnnotation(annotations, a.type, a.getProperties(cp));
			return method;
		}
	}

	public static class Param {
		public Class<?> type;
		public String name;
		public List<AnnotationRef<?>> annotations = new ArrayList<AnnotationRef<?>>();

		public Param(Class<?> type, String name, AnnotationRef<?>... annotations) {
			this.type = type;
			this.name = name;
			for(AnnotationRef<?> a : annotations)
				this.annotations.add(a);
		}

		public void createAnnotations(int i,
				ParameterAnnotationsAttribute paramAnnotations, ConstPool cp) {
			for(AnnotationRef<?> a : annotations){
				createAnnotation(i, paramAnnotations, a.type, a.getProperties(cp));
			}
		}
	}
	
	public static class AnnotationRef<T extends Annotation> {
		public Class<T> type;
		public Map<String, Object> properties = new HashMap<String, Object>();
		
		public AnnotationRef(Class<T> type){
			this.type = type;
		}

		public Map<String, MemberValue> getProperties(ConstPool cp) {
			Map<String, MemberValue> ret = new HashMap<String, MemberValue>();
			for(Entry<String, Object> prop : properties.entrySet()){
				ret.put(prop.getKey(), makeMemberValue(prop.getValue(), cp));
			}
			return ret;
		}

		public AnnotationRef(Class<T> type, Object value){
			this(type);
			properties.put("value", value);
		}
		
		public AnnotationRef<T> p(String name, Object value){
			properties.put(name, value);
			return this;
		}
	}
	
	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
		CtClass ctClass = makeClass(applicationClass);

		if (!ctClass.subtypeOf(classPool.get(RESTResource.class.getName()))) {
			Logger.debug("Not a RESTResource subclass");
            return;
        }

		// Enhance only JAX-RS model entities
		CRUD restCRUD = getAnnotation(ctClass, CRUD.class);
		if (restCRUD == null) {
			Logger.debug("Missing CRUD annotation");
			return;
		}
		Logger.info("Enhancing CRUD entity %s", ctClass.getName());
		ConstPool cp = ctClass.getClassFile().getConstPool();
		Factory factory = Manager.factoryFor(restCRUD.model());
		Class<?> idType = factory.keyType();
		String idName = getLastTemplateName(restCRUD.single());

		AnnotationsAttribute annotations = getAnnotations(ctClass);
		if(!hasAnnotation(ctClass, Path.class)){
			createAnnotation(annotations, Path.class, map(cp, "/"));
		}
		if(!hasAnnotation(ctClass, LinkELProvider.class)){
			createAnnotation(annotations, LinkELProvider.class, map(cp, PlayELProvider.class));
		}
		String[] mediaTypes = new String[]{MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON};
		if(!hasAnnotation(ctClass, Produces.class)){
			createAnnotation(annotations, Produces.class, map(cp, mediaTypes));
		}

		// list
		Signature listSignature = new Signature(Response.class, "list", a(GET.class), a(AddLinks.class), 
				a(Path.class, restCRUD.collection()),
				linkResourceAnnotation(restCRUD.model(), "list", "select"));
		listSignature.param(DataTableQuery.class, "q", a(Form.class));
		
		if(!hasMethod(ctClass, listSignature.name, listSignature.signature())){
			CtMethod list = listSignature.method(ctClass, restCRUD.model());
			ctClass.addMethod(list);
		}

		// get
		Signature getSignature = new Signature(Response.class, "get", a(GET.class), a(AddLinks.class), 
				a(Path.class, restCRUD.single()),
				linkResourceAnnotation(restCRUD.model(), "self", "select"));
		getSignature.param(idType, "id", a(PathParam.class, idName));
		
		if(!hasMethod(ctClass, getSignature.name, getSignature.signature())){
			ctClass.addMethod(getSignature.method(ctClass, restCRUD.model()));
		}

		// delete
		Signature deleteSignature = new Signature(Response.class, "delete", a(DELETE.class),
				a(Path.class, restCRUD.single()),
				linkResourceAnnotation(restCRUD.model(), "remove", "delete"));
		deleteSignature.param(idType, "id", a(PathParam.class, idName));
		
		if(!hasMethod(ctClass, deleteSignature.name, deleteSignature.signature())){
			CtMethod delete = deleteSignature.method(ctClass, restCRUD.model());
			ctClass.addMethod(delete);
		}

		// add
		Signature addSignature = new Signature(Response.class, "add", a(POST.class),
				a(Path.class, restCRUD.collection()),
				a(Consumes.class, mediaTypes),
				linkResourceAnnotation(restCRUD.model(), "add", "insert"));
		addSignature.param(restCRUD.model(), "elem", a(Validate.class));
		addSignature.param(UriInfo.class, "uriInfo", a(Context.class));
		
		if(!hasMethod(ctClass, addSignature.name, addSignature.signature())){
			CtMethod add = addSignature.method(ctClass, restCRUD.model());
			ctClass.addMethod(add);
		}

		// edit
		Signature editSignature = new Signature(Response.class, "edit", a(PUT.class),
				a(Path.class, restCRUD.single()),
				a(Consumes.class, mediaTypes),
				linkResourceAnnotation(restCRUD.model(), "update", "update"));
		editSignature.param(idType, "id", a(PathParam.class, idName));
		editSignature.param(restCRUD.model(), "elem", a(Validate.class));
		
		if(!hasMethod(ctClass, editSignature.name, editSignature.signature())){
			CtMethod edit = editSignature.method(ctClass, restCRUD.model());
			ctClass.addMethod(edit);
		}
		
		// autocomplete
		for(Property property : factory.listProperties()){
			Field field = property.field;
			String fieldName = field.getName();
			CRUDField crud = field.getAnnotation(CRUDField.class);
			if(crud == null)
				continue;
			if(crud.autoComplete()){
				// autocomplete
				Signature autoCompleteSignature = new Signature(Response.class, fieldName+"AutoComplete", 
						a(GET.class), a(Path.class, restCRUD.collection()+ "/auto-complete/"+fieldName),
						linkResourceAnnotation(restCRUD.model(), "autocomplete/"+fieldName, "select"));
				autoCompleteSignature.param(String.class, "q", a(QueryParam.class, "q"));
				
				if(!hasMethod(ctClass, autoCompleteSignature.name, autoCompleteSignature.signature())){
					CtMethod edit = CtMethod.make(autoCompleteSignature.decl() + " {"
							+" return super.autoComplete(" + restCRUD.model().getName() + ".class, \""+fieldName+"\", q); }", 
							ctClass);
					autoCompleteSignature.annotate(edit);
					ctClass.addMethod(edit);
				}
			}
		}

		// descriptor
		Signature descriptorSignature = new Signature(Response.class, "descriptor", a(GET.class),
				a(Path.class, restCRUD.collection() + "/descriptor"),
				linkResourceAnnotation(restCRUD.model(), "descriptor", "select"));
		if(!hasMethod(ctClass, descriptorSignature.name, descriptorSignature.signature())){
			CtMethod descriptor = descriptorSignature.method(ctClass, restCRUD.model());
			ctClass.addMethod(descriptor);
		}

		Signature makeDataTableSignature = new Signature(DataTable.class, "makeDataTable");
		makeDataTableSignature.param(String.class, "echo");
		makeDataTableSignature.param(Long.TYPE, "count");
		makeDataTableSignature.param(List.class, "results");
		makeDataTableSignature.param(Class.class, "type");
		makeDataTableSignature.param(Object.class, "oob");
		makeDataTableSignature.param(UriInfo.class, "uriInfo");
		if(!hasMethod(ctClass, makeDataTableSignature.name, makeDataTableSignature.signature())){
			makeDataTable(ctClass, makeDataTableSignature, restCRUD.model());
		}
		
		// Done.
		applicationClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}

	private void makeDataTable(CtClass ctClass, Signature makeDataTableSignature, Class<? extends Model> modelClass) 
	throws Exception {
		// we need the class itself
		CtClass dataTableClass = ctClass.makeNestedClass("__DataTable", true);
		ClassPool cp = ctClass.getClassPool();

		// its superclass
		dataTableClass.setSuperclass(cp.get(DataTable.class.getName()));
		
		// its constructor
		CtClass[] parameters = new CtClass[makeDataTableSignature.parameters.size()];
		for(int i=0;i<parameters.length ;i++){
			parameters[i] = cp.get(makeDataTableSignature.parameters.get(i).type.getName());
		}
		CtConstructor dataTableConstructor = CtNewConstructor.make(parameters, null, CtNewConstructor.PASS_PARAMS, null, null, dataTableClass);

		// it wants some annotations
		ConstPool constPool = dataTableClass.getClassFile().getConstPool();
		AnnotationsAttribute annotations = getAnnotations(dataTableClass);
		createAnnotation(annotations, XmlType.class, map(constPool, "namespace", dataTableClass.getName()));
		createAnnotation(annotations, XmlRootElement.class, map(constPool, "name", "dataTable"));
		createAnnotation(annotations, XmlSeeAlso.class, map(constPool, new Object[]{modelClass}));
		createAnnotation(annotations, Mapped.class, map(constPool, "namespaceMap", 
				new Object[]{
					createAnnotation(XmlNsMap.class, map(constPool, "namespace", "http://www.w3.org/2001/XMLSchema-instance")
							.add("jsonName", "xsi"), constPool),
					createAnnotation(XmlNsMap.class, map(constPool, "namespace", "http://www.w3.org/2005/Atom")
							.add("jsonName", "atom"), constPool)
		}));
		
		// now add the constructor
		dataTableClass.addConstructor(dataTableConstructor);
		
		// and give the whole class to play
		ApplicationClass dataTableApplicationClass = new ApplicationClass(dataTableClass.getName());
		dataTableApplicationClass.compiled(dataTableClass.toBytecode());
		dataTableClass.defrost();
		Play.classes.add(dataTableApplicationClass);

		// now build the method's body
		StringBuilder ret = new StringBuilder(makeDataTableSignature.decl());
		ret.append("{ return new ").append(dataTableClass.getName()).append("(");
		boolean once = false;
		for(Param param : makeDataTableSignature.parameters){
			if(once)
				ret.append(", ");
			else
				once = true;
			ret.append(param.name);
		}
		ret.append("); }");
		// make the method
		CtMethod method = CtMethod.make(ret.toString(), ctClass);
		ctClass.addMethod(method);
}

	private AnnotationRef<?> linkResourceAnnotation(
			Class<? extends Model> model, String rel, String permission) {
		return a(LinkResource.class, model).p("rel", rel)
				.p("constraint", "${p:hasPermission(this, '"+permission+"')}");
	}

	private <T extends Annotation> AnnotationRef<T> a(Class<T> annotationType) {
		return new AnnotationRef<T>(annotationType);
	}

	private <T extends Annotation> AnnotationRef<T> a(Class<T> annotationType, Object value) {
		return new AnnotationRef<T>(annotationType, value);
	}

	public static String t(Class<?> type) {
		return type.getName();
	}

	private static String typeSignature(Class<?> type) {
		if(type.isPrimitive()){
			if(type == Long.TYPE)
				return "J";
			throw new IllegalArgumentException("Primitive types not implemented: "+type);
		}
		return "L"+type.getName()+";";
	}

	private String getLastTemplateName(String url) {
		if(url == null)
			throw new RuntimeException("Missing url template");
		int start = url.lastIndexOf('{');
		int end = url.lastIndexOf('}');
		if(start == -1 || end == -1 || start > end)
			throw new RuntimeException("Missing template in url: "+url);
		String name = url.substring(start+1, end);
		if(name.length() == 0)
			throw new RuntimeException("Empty template in url: "+url);
		return name;
	}

	private boolean hasMethod(CtClass ctClass, String name, String signature) {
		try {
			CtMethod method = ctClass.getMethod(name, signature);
			return method.getDeclaringClass() == ctClass;
		} catch (NotFoundException e) {
			return false;
		}
	}

	public static interface ParamMap<K, V> extends Map<K, V>{
		public ParamMap<K, V> add(K key, Object value);
	}
	
	public static abstract class HashParamMap<K, V> extends HashMap<K, V> implements ParamMap<K, V>{}
	
	private ParamMap<String, MemberValue> map(final ConstPool cp, String key, Object value) {
		ParamMap<String,MemberValue> map = new HashParamMap<String, MemberValue>(){
			@Override
			public ParamMap<String, MemberValue> add(String key, Object value) {
				put(key, makeMemberValue(value, cp));
				return this;
			}
			
		};
		map.put(key, makeMemberValue(value, cp));
		return map;
	}

	private ParamMap<String, MemberValue> map(ConstPool cp, Object value) {
		return map(cp, "value", value);
	}

	private static MemberValue makeMemberValue(Object value, ConstPool cp) {
		if(value instanceof String)
			return new StringMemberValue((String)value, cp);
		if(value instanceof Class)
			return new ClassMemberValue(((Class<?>)value).getName(), cp);
		if(value instanceof javassist.bytecode.annotation.Annotation)
			return new AnnotationMemberValue((javassist.bytecode.annotation.Annotation)value, cp);
		if(value.getClass().isArray()){
			ArrayMemberValue ret = new ArrayMemberValue(cp);
			Object[] values = (Object[])value;
			MemberValue[] elements = new MemberValue[values.length];
			for (int i = 0; i < elements.length; i++) {
				elements[i] = makeMemberValue(values[i], cp);
			}
			ret.setValue(elements);
			return ret;
		}
		throw new RuntimeException("Invalid member value: "+value);
	}

    protected boolean hasAnnotation(CtClass ctClass, Class<? extends Annotation> type) throws ClassNotFoundException {
    	return hasAnnotation(ctClass, type.getName());
    }

	protected <T extends Annotation> T getAnnotation(CtClass ctClass, Class<T> type) throws ClassNotFoundException {
		for (Object object : ctClass.getAvailableAnnotations()) {
			Annotation ann = (Annotation) object;
			if (ann.annotationType() == type) {
				return (T)ann;
			}
		}
		return null;
	}
	
    protected static ParameterAnnotationsAttribute getParameterAnnotations(CtMethod ctMethod) throws NotFoundException {
    	ParameterAnnotationsAttribute annotationsAttribute = (ParameterAnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(ParameterAnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new ParameterAnnotationsAttribute(ctMethod.getMethodInfo().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
        	javassist.bytecode.annotation.Annotation[][] newAnnotations = new javassist.bytecode.annotation.Annotation[ctMethod.getParameterTypes().length][];
            for(int v=0;v < newAnnotations.length;v++)
            	newAnnotations[v] = new javassist.bytecode.annotation.Annotation[0];
            annotationsAttribute.setAnnotations(newAnnotations);
            ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
        }
        return annotationsAttribute;
    }

    protected static void createAnnotation(int i, ParameterAnnotationsAttribute attribute, Class<? extends Annotation> annotationType) {
        createAnnotation(i, attribute, annotationType, new HashMap<String, MemberValue>());
    }
    
    protected static void createAnnotation(int i, ParameterAnnotationsAttribute attribute, Class<? extends Annotation> annotationType, Map<String, MemberValue> members) {
        javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(annotationType.getName(), attribute.getConstPool());
        for (Map.Entry<String, MemberValue> member : members.entrySet()) {
            annotation.addMemberValue(member.getKey(), member.getValue());
        }
        // add the annotation to the existing list
        javassist.bytecode.annotation.Annotation[][] annotations = attribute.getAnnotations();
        // get this param's annotation
        javassist.bytecode.annotation.Annotation[] paramAnnotations = annotations[i];
        // grow the array
        javassist.bytecode.annotation.Annotation[] newParamAnnotations = new javassist.bytecode.annotation.Annotation[paramAnnotations.length+1];
        System.arraycopy(paramAnnotations, 0, newParamAnnotations, 0, paramAnnotations.length);
        newParamAnnotations[paramAnnotations.length] = annotation;
        annotations[i] = newParamAnnotations;
        attribute.setAnnotations(annotations);
    }

    protected static javassist.bytecode.annotation.Annotation createAnnotation(Class<? extends Annotation> annotationType, Map<String, MemberValue> members, ConstPool cp) {
        javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(annotationType.getName(), cp);
        for (Map.Entry<String, MemberValue> member : members.entrySet()) {
            annotation.addMemberValue(member.getKey(), member.getValue());
        }
        return annotation;
    }

}
