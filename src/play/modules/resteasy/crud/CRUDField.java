package play.modules.resteasy.crud;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface CRUDField {
	public String name() default "";
	public boolean searchable() default false;
	public boolean editable() default false;
	public boolean sortable() default false;
	public boolean autoComplete() default false;
	public Type type() default Type.DEFAULT;
}
