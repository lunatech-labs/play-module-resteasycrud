package play.modules.resteasy.crud;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import play.Play;
import play.utils.Java;

public class CRUDSecure {
	
	public static boolean hasPermission(Object target, String permission){
		return (Boolean) CRUDSecurity.invoke("hasPermission", target, permission);
	}
	
	public static boolean hasRole(String role){
		return (Boolean) CRUDSecurity.invoke("hasRole", role);
	}

	public static class CRUDSecurity {
		public static boolean hasPermission(Object target, String permission){
			return false;
		}
		
		public static boolean hasRole(String role){
			return false;
		}

		public static Object invoke(String m, Object... args) {
            Class<?> security = null;
            List<Class> classes = Play.classloader.getAssignableClasses(CRUDSecurity.class);
            if(classes.size() == 0) {
                security = CRUDSecurity.class;
            } else {
                security = classes.get(0);
            }
            try {
            	Method method;
            	if(m.equals("hasPermission"))
            		method = security.getMethod(m, Object.class, String.class);
            	else if(m.equals("hasRole"))
            		method = security.getMethod(m, String.class);
            	else
            		throw new NoSuchMethodException("No such method: "+m);
            	return method.invoke(null, args);
            } catch(InvocationTargetException e) {
            	if(e.getTargetException() instanceof RuntimeException)
            		throw (RuntimeException)e.getTargetException();
                throw new RuntimeException(e.getTargetException());
            } catch (Exception e) {
            	throw new RuntimeException(e);
			}
        }

	}
}
