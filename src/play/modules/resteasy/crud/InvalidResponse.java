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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The type returned to the client if validation fails.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InvalidResponse {

	/**
	 * Per-field errors.
	 */
	private List<InvalidResponseField> errors = new ArrayList<InvalidResponseField>();
	/**
	 * Global errors.
	 */
	private List<String> globalErrors = new ArrayList<String>();

	public InvalidResponse() {
	}

	public InvalidResponse(List<InvalidResponseField> errors) {
		this.errors = errors;
	}

	public void addError(String field, String error) {
		errors.add(new InvalidResponseField(field, error));
	}

	public void addGlobalError(String error) {
		globalErrors.add(error);
	}

	public List<InvalidResponseField> getErrors() {
		return errors;
	}

	public void setErrors(List<InvalidResponseField> errors) {
		this.errors = errors;
	}

	public List<String> getGlobalErrors() {
		return globalErrors;
	}

	public void setGlobalErrors(List<String> globalErrors) {
		this.globalErrors = globalErrors;
	}
}