package play.modules.resteasy.crud;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class DataTableQuery {
	@QueryParam("start")
	@DefaultValue("0")
	public int start;
	@QueryParam("length")
	@DefaultValue("10")
	public int length;
	@QueryParam("sort")
	public String sort;
	@QueryParam("search")
	public String search;
	@QueryParam("echo")
	public String echo;
	@QueryParam("oob")
	public boolean oob;
	@Context
	public UriInfo uriInfo;
}
