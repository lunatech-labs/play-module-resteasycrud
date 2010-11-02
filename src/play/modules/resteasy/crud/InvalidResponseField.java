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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Per-field error response.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InvalidResponseField {
	private String field;
	private String error;

	public InvalidResponseField() {
	}

	public InvalidResponseField(String field, String error) {
		this.field = field;
		this.error = error;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
