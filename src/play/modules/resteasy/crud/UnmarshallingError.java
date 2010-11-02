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

import java.net.HttpURLConnection;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.ValidationEvent;

/**
 * Wraps XML validation errors
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
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
