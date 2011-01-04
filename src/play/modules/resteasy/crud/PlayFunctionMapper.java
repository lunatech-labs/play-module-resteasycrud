package play.modules.resteasy.crud;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.FunctionMapper;

import org.jboss.el.lang.ExtendedFunctionMapper;

import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.UnexpectedException;
import play.modules.resteasy.crud.CRUDSecure.CRUDSecurity;

/**
 * Resolves Seam Security EL functions, s:hasRole() and s:hasPermission()
 * by decorating a delegate Unified EL FunctionMapper
 *  
 * @author Shane Bryzak
 */
public class PlayFunctionMapper extends ExtendedFunctionMapper
{
	private FunctionMapper functionMapper;

	private static Map<String, Method> methods;

	public PlayFunctionMapper(FunctionMapper functionMapper)
	{
		this.functionMapper = functionMapper;
	}

	@Override 
	public Method resolveFunction(String prefix, String localName) 
	{
		if ( "p".equals(prefix) )
		{
			init();
			return methods.get(localName);
		}
		else if (functionMapper != null)
		{
			return functionMapper.resolveFunction(prefix, localName);
		}
		else
		{
			return null;
		}
	}  

	@Override 
	public Method resolveFunction(String prefix, String localName, int paramCount) 
	{
		if ( "p".equals(prefix) )
		{
			init();
			return methods.get(localName);
		}
		else if (functionMapper != null)
		{
			return functionMapper.resolveFunction(prefix, localName);
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static void init() {
		if(methods != null)
			return;
		methods = new HashMap<String,Method>();
		List<ApplicationClass> classes = Play.classes.getAssignableClasses(CRUDSecurity.class);
		Class<CRUDSecurity> security;
		if(classes.isEmpty()){
			Logger.warn("No CRUDSecurity implementation found, all security checks will be denied");
			security = CRUDSecurity.class;
		}else
			security = (Class<CRUDSecurity>) classes.get(0).javaClass;
		try {
			methods.put("hasPermission", security.getMethod("hasPermission", Object.class, String.class));
			methods.put("hasRole", security.getMethod("hasRole", String.class));
		} catch (SecurityException e) {
			throw new UnexpectedException(e);
		} catch (NoSuchMethodException e) {
			Logger.warn("Missing function in your CRUDSecurity subclass: "+e.getMessage());
			throw new UnexpectedException(e);
		}
	}    


}

