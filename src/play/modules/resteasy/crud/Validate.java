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

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.annotations.Decorator;
import org.jboss.resteasy.links.impl.LinkDecorator;

/**
 * Place this on your entity or on the JAX-RS method, or parameter if you wish to validate the entity
 * when it is deserialised, and to throw an error if it is not valid.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER,
		ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Decorator(processor = Validator.class, target = Unmarshaller.class)
@Documented
public @interface Validate {
}
