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
