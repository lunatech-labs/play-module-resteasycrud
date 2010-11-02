package play.modules.resteasy.crud;

import java.net.HttpURLConnection;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.ValidationEvent;

public class UnmarshallingError extends WebApplicationException {

	public UnmarshallingError(List<ValidationEvent> events) {
		super(buildResponse(events));
	}

	private static Response buildResponse(List<ValidationEvent> events) {
		InvalidResponse response = new InvalidResponse();
		for(ValidationEvent e : events)
			response.addGlobalError(e.getMessage());
		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(response).build();
	}

}
