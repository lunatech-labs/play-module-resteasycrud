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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * The query type, for getting lists of entities
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class DataTableQuery {
	/**
	 * Start offset, defaults to 0
	 */
	@QueryParam("start")
	@DefaultValue("0")
	public int start;
	
	/**
	 * Number of entries returned, defaults to 10
	 */
	@QueryParam("length")
	@DefaultValue("10")
	public int length;
	
	/**
	 * Sort order, as specified by: (&lt;fieldName&gt; (ASC|DESC)?) (, &lt;fieldName&gt; (ASC|DESC)?)*
	 */
	@QueryParam("sort")
	public String sort;
	
	/**
	 * String to search for in all searcheable fields, to filter results
	 */
	@QueryParam("search")
	public String search;
	
	/**
	 * This has to be returned as-is in the response
	 */
	@QueryParam("echo")
	public String echo;
	
	/**
	 * Include Out-Of-Bounds result as well. Defaults to false.
	 */
	@QueryParam("oob")
	public boolean oob;
	
	@Context
	public UriInfo uriInfo;
}
