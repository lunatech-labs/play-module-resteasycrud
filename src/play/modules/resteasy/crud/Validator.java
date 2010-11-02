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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.jboss.resteasy.annotations.DecorateTypes;
import org.jboss.resteasy.spi.interception.DecoratorProcessor;

import play.Logger;

/**
 * Validator for the {@link @Validate} entity.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@DecorateTypes({"text/*+xml", "application/*+xml", "application/xml", "application/json", "application/*+json"})
public class Validator implements DecoratorProcessor<Unmarshaller, Validate>{

	static class ErrorCollector extends Listener implements ValidationEventHandler {

		private List<ValidationEvent> events = new ArrayList<ValidationEvent>();
		
		@Override
		public boolean handleEvent(ValidationEvent event) {
			Logger.info("Validation event: %s", event);
			if(event.getSeverity() == ValidationEvent.ERROR || event.getSeverity() == ValidationEvent.FATAL_ERROR)
				events.add(event);
			return true;
		}
		
		@Override
		public void afterUnmarshal(Object target, Object parent) {
			super.afterUnmarshal(target, parent);
			if(!events.isEmpty())
				throw new UnmarshallingError(events);
		}
	}
	
	@Override
	public Unmarshaller decorate(Unmarshaller target, Validate annotation,
            Class type, Annotation[] annotations, MediaType mediaType) {
		try {
			ErrorCollector errorCollector = new ErrorCollector();
			target.setEventHandler(errorCollector);
			target.setListener(errorCollector);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return target;
	}

}
