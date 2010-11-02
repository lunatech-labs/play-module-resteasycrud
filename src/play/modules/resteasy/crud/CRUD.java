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

import play.db.Model;

/**
 * Place this on your RESTResource subclass to indicate what model this 
 * resource is responsible for.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface CRUD {
	/**
	 * The type of entity this resource is responsible for.
	 */
	public Class<? extends Model> model();
	/**
	 * A URL for single entities. Should include a template parameter with the entity id. Ex: /foo/{id}
	 */
	public String single() default "";
	/**
	 * A URL for collections of entities. Ex: /foo
	 */
	public String collection() default "";
}
