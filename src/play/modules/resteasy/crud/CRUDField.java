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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as handled by the CRUD module. If this annotation is not present on a field it will
 * not be taken into account by the CRUD module.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface CRUDField {
	/**
	 * Presentation name for the field
	 */
	public String name() default "";
	/**
	 * Set to true if the field should be searched for when the user searches for entries
	 */
	public boolean searchable() default false;
	/**
	 * Set to true if the field is user-editable
	 */
	public boolean editable() default false;
	/**
	 * Set to true if the field can be used to sort the list of entities
	 */
	public boolean sortable() default false;
	/**
	 * Set to true if we should produce autocomplete resources for this field
	 */
	public boolean autoComplete() default false;
	/**
	 * Set to something else than Type.DEFAULT to override the field type, otherwise it is implied from the 
	 * entity's field type.
	 */
	public Type type() default Type.DEFAULT;
}
