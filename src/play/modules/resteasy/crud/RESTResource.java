package play.modules.resteasy.crud;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import play.Logger;
import play.db.Model;
import play.db.Model.Factory;
import play.db.Model.Manager;
import play.db.Model.Property;

public abstract class RESTResource {
	
	protected abstract boolean hasPermission(Object target, String name);

	protected void checkPermission(Object target, String name) {
		if (!hasPermission(target, name))
			throw new WebApplicationException(HttpURLConnection.HTTP_FORBIDDEN);
	}

	protected <T> T checkNotFound(T o) {
		if (o == null)
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		return o;
	}

	protected <T> T checkNotFound(T o, String msg, Object... params) {
		if (o == null)
			throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(String.format(msg, params)).build());
		return o;
	}

	protected void checkEmpty(Object... objects) {
		for(Object o : objects){
			if (o == null)
				continue;
			if (o instanceof Collection && ((Collection<?>) o).isEmpty())
				continue;
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	protected <T> T checkForUpdate(T o, T oDB) {
		checkPermission(o, "insert");
		checkNotFound(oDB);
		checkPermission(oDB, "update");
		return oDB;
	}

	protected void checkAutoCompleteQuery(String... values) {
	}

	protected Response notFound() {
		return status(HttpURLConnection.HTTP_NOT_FOUND);
	}

	protected Response forbidden() {
		return status(HttpURLConnection.HTTP_FORBIDDEN);
	}

	protected Response badRequest() {
		return status(HttpURLConnection.HTTP_BAD_REQUEST);
	}

	protected Response badRequest(String message, Object... args) {
		return status(HttpURLConnection.HTTP_BAD_REQUEST, message, args);
	}

	protected Response noContent() {
		return status(HttpURLConnection.HTTP_NO_CONTENT);
	}

	protected Response created() {
		return status(HttpURLConnection.HTTP_CREATED);
	}

	protected Response internalError() {
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR);
	}
	
	protected Response internalError(String msg, Object... args) {
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR, msg, args);
	}

	protected Response internalError(Throwable t, String msg, Object... args) {
		Logger.error(t, msg, args);
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR, msg, args);
	}

	protected Response status(int code){
		Logger.info("Returning code %s", code);
		return Response.status(code).build();
	}
	
	protected Response status(int code, String message, Object... args){
		String entity = String.format(message, args)+"\n";
		return status(code, entity);
	}

	protected Response status(int code, Object entity){
		Logger.info("Returning code %s: %s", code, entity);
		return Response.status(code).entity(entity).build();
	}

	protected void respond(Response r) {
		throw toThrowable(r);
	}

	protected WebApplicationException toThrowable(Response r) {
		return new WebApplicationException(r);
	}

	protected Response ok() {
		return status(HttpURLConnection.HTTP_OK);
	}

	protected Response ok(Object entity) {
		return status(HttpURLConnection.HTTP_OK, entity);
	}

	protected Response autoComplete(List<String> list) {
		return ok(new JAXBList(list));
	}

	protected void throwConstraintValidation(Object entity, String message,
			String field, Object value) {
		throw new InvalidStateException(new InvalidValue[] { new InvalidValue(
				message, entity.getClass(), field, value, entity) });
	}

	//
	// CRUD
	
	public <T extends Model> Response list(Class<T> model, DataTableQuery q){
		logQuery(q);
		PagedQuery<T> carriers = findPaged(model);
		return makeQueryResponse(q, carriers, model,
				getSortableColumns(model), q.uriInfo);
	}

	public <T extends Model> Response get(Class<T> model, Object id) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		T entity = (T)factory.findById(id);
		checkNotFound(entity, "Entity of type %s with id of %s could not be found", model.getName(), id);
		checkPermission(entity, "select");
		return ok(entity);
	}

	public <T extends Model> Response delete(Class<T> model, Object id) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		T entity = (T)factory.findById(id);
		checkNotFound(entity, "Entity of type %s with id of %s could not be found", model.getName(), id);
		checkPermission(entity, "delete");
		entity._delete();
		return noContent();
	}

	public <T extends Model> Response add(Class<T> model, final T elem, UriInfo uriInfo) {
		// check non-editable field
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				Object newValue;
				try {
					newValue = PropertyUtils.getSimpleProperty(elem, property.name);
				} catch (Exception e) {
					throw toThrowable(internalError(e, "Failed to get property %s", property.name));
				}
				// if that field is not editable, let us barf
				if(crud == null || !crud.editable())
					checkEmpty(newValue);
			}
		});
		elem._save();
		return created();
	}

	public <T extends Model> Response edit(Class<T> model, Object id, final T elem) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		final T elemFromDB = (T)factory.findById(id);
		checkForUpdate(elem, elemFromDB);
		// copy every field
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				Object newValue;
				try {
					newValue = PropertyUtils.getSimpleProperty(elem, property.name);
				} catch (Exception e) {
					throw toThrowable(internalError(e, "Failed to get property %s", property.name));
				}
				// if that field is not editable, let us barf
				if(crud == null || !crud.editable())
					checkEmpty(newValue);
				else{
					// we can set it
					try{
						PropertyUtils.setSimpleProperty(elemFromDB, property.name, newValue);
					} catch (Exception e) {
						throw toThrowable(internalError(e, "Failed to set property %s", property.name));
					}
				}
			}
		});
		elemFromDB._save();
		return noContent();
	}

	public <T extends Model> Response autoComplete(Class<T> model, String field, String q) {
		checkAutoCompleteQuery(q);
		return autoComplete(AutoComplete.getAutoComplete(model, field, q));
	}
	
	public <T extends Model> Response descriptor(Class<T> model) {
		return Response.ok(new Descriptor<T>(model)).build();
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Descriptor<T extends Model> {

		@XmlElement
		public List<Column> columns = new ArrayList<Column>();

		// For JAXB
		public Descriptor(){}
		
		public Descriptor(Class<T> model) {
			walkProperties(model, new PropertyWalker(){
				@Override
				public void walk(Property property, Field field, CRUDField crud) {
					addColumn(property, field, crud);
				}
			});
			CRUDOrder crudOrder = model.getAnnotation(CRUDOrder.class);
			if(crudOrder != null){
				List<Column> sortedColumns = new ArrayList<Column>();
				for(String field : crudOrder.value()){
					int i = findColumn(field);
					if(i == -1)
						throw new RuntimeException("No such field in model: "+field);
					sortedColumns.add(columns.remove(i));
				}
				// now add any remaining column
				sortedColumns.addAll(columns);
				// and swap
				columns = sortedColumns;
			}
		}

		private int findColumn(String field) {
			for(int i=0;i<columns.size();i++)
				if(columns.get(i).field.equals(field))
					return i;
			return -1;
		}

		private void addColumn(Property p, Field field, CRUDField crud) {
			// FIXME: only make it a column if it has a representation in JAXB, and allow @CRUD-less fields
			if(crud == null)
				return;
			columns.add(new Column(p, field, crud));
		}
		
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Column {
		@XmlElement
		public String name;
		@XmlElement
		public String field;
		@XmlElement
		public boolean editable;
		@XmlElement
		public boolean sortable;
		@XmlElement
		public boolean autocomplete;
		@XmlElement
		public String type;
		@XmlElement
		public List<String> validators = new ArrayList<String>();

		// For JAXB
		public Column(){}
		
		public Column(Property p, Field field, CRUDField crud) {
			this.field = name = p.name;
			editable = false;
			autocomplete = false;
			sortable = false;
			Class<?> fieldType = field.getType();
			setType(fieldType);
			if(crud != null){
				if(crud.name().length() > 0)
					name = crud.name();
				editable = crud.editable();
				autocomplete = crud.autoComplete();
				sortable = crud.sortable();
				Type crudType = crud.type();
				if(crudType != Type.DEFAULT)
					type = crudType.name();
			}
			addValidation(field);
		}

		private void addValidation(Field field) {
			if(field.isAnnotationPresent(NotEmpty.class)
					|| field.isAnnotationPresent(NotNull.class))
				validators.add("notempty");
		}

		private void setType(Class<?> fieldType) {
			if(fieldType == String.class){
				type = Type.STRING.name();
			}else if(fieldType == Integer.class
					|| fieldType == Integer.TYPE){
				type = Type.INTEGER.name();
			}else if(fieldType == Boolean.class){
				type = Type.BOOLEAN.name();
			}else
				throw new RuntimeException("Unknown field type: "+fieldType.getName());
		}
	}
	
	public interface PropertyWalker {
		public void walk(Property property, Field field, CRUDField crud);
	}
	
	protected static <T extends Model> void walkProperties(Class<T> model, PropertyWalker walker){
		// FIXME: make this faster (10x slower than doing it manually for some reason)
		Factory factory = Manager.factoryFor(model);
		for(Property prop : factory.listProperties()){
			Field field = prop.field;
			CRUDField crud = field.getAnnotation(CRUDField.class);
			walker.walk(prop, field, crud);
		}
	}
	
	protected <T extends Model> Set<String> getSortableColumns(Class<T> model) {
		final Set<String> ret = new HashSet<String>();
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				if(crud != null && crud.sortable())
					ret.add(property.name);
			}
		});
		return ret;
	}
	
	protected <T extends Model> Set<String> getSearchableColumns(Class<T> model) {
		final Set<String> ret = new HashSet<String>();
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				if(crud != null && crud.searchable())
					ret.add(property.name);
			}
		});
		return ret;
	}

	protected <T extends Model> PagedQuery<T> findPaged(Class<T> model) {
		PagedQuery<T> query = new PagedQuery<T>("FROM "+model.getSimpleName());
		query.searchFields(getSearchableColumns(model));
		return query;
	}

	protected <T> Response makeQueryResponse(DataTableQuery q,
			PagedQuery<T> results, Class<T> klass, Set<String> validColumns,
			UriInfo uriInfo, String... permissions) {
		return makeQueryResponse(q, results, klass, validColumns, null,
				uriInfo, permissions);
	}

	protected <T> Response makeQueryResponse(DataTableQuery q,
			PagedQuery<T> results, Class<T> klass, Set<String> validColumns,
			Object oob, UriInfo uriInfo, String... permissions) {
		results.start = (long)q.start;
		results.limit = (long)q.length;
		if (isSortValid(q.sort, validColumns))
			results.order = q.sort;
		if(!StringUtils.isEmpty(q.search))
			results.search = q.search;
		DataTable<T> dataTable = makeDataTable(q.echo,
				results.getCount(), results.getResultList(), klass, oob,
				uriInfo);
		// add any permission we have
		for (String permission : permissions) {
			if (hasPermission(dataTable.facadeFor(), permission))
				dataTable.addPermission(permission);
		}
		Logger.info("Returning 200 with entity: %s", dataTable);
		return Response.ok(dataTable).build();
	}

	protected abstract <T> DataTable<T> makeDataTable(String echo, long count, List<T> results, Class<T> type, Object oob, UriInfo uriInfo);
	
	protected boolean isSortValid(String sort, Set<String> validColumns) {
		if (sort == null)
			return false;
		final StringTokenizer tokenizer = new StringTokenizer(sort, ",");
		// we need at least one
		if (!tokenizer.hasMoreTokens())
			return false;
		while (tokenizer.hasMoreTokens()) {
			final String fragment = tokenizer.nextToken().trim();
			String field;
			if (fragment.toLowerCase().endsWith(" desc"))
				field = fragment.substring(0, fragment.length() - 5).trim();
			else if (fragment.toLowerCase().endsWith(" asc"))
				field = fragment.substring(0, fragment.length() - 4).trim();
			else
				field = fragment;
			// can be an int or an allowed name
			if (!field.matches("\\d+") && !validColumns.contains(field))
				return false;
		}
		return true;
	}

	protected void logQuery(DataTableQuery q) {
		Logger.info("GET start: %s, length: %s, echo: %s, sort: %s, search: %s",
				q.start, q.length, q.echo, q.sort, q.search);
	}

}
