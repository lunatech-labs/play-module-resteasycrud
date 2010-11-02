package play.modules.resteasy.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.links.RESTServiceDiscovery;
import org.jboss.resteasy.links.ResourceFacade;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://www.w3.org/2001/XMLSchema-instance", jsonName = "xsi"),
                        @XmlNsMap(namespace = "http://www.w3.org/2005/Atom", jsonName = "atom") })
public class DataTable<T> implements ResourceFacade<T> {

  @XmlElementRef
  private RESTServiceDiscovery rest;

  @XmlElement(name = "permission")
  private final List<String> permissions = new ArrayList<String>();

  @XmlElement
  private String echo;

  @XmlElement
  private long size;

  @XmlElement(name = "rows")
  private List<T> rows = new ArrayList<T>();

  @XmlElement
  private Object oob;

  private Class<T> type;

  private UriInfo uriInfo;

  public DataTable() {}

  public DataTable(String echo, long size, List<T> rows, Class<T> klass, Object oob, UriInfo uriInfo) {
    this.echo = echo;
    this.size = size;
    this.rows = rows;
    this.oob = oob;
    this.type = klass;
    this.uriInfo = uriInfo;
  }

  @XmlElement
  public String getType() {
    // this is incredibly stupid but the JSON mapping is so
    return type.getSimpleName().substring(0, 1).toLowerCase() + type.getSimpleName().substring(1);
  }

  public Class<T> facadeFor() {
    return type;
  }

  public Map<String, ? extends Object> pathParameters() {
	  MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
	  Map<String, String> ret = new HashMap<String,String>();
	  for(Entry<String, List<String>> entry:  pathParameters.entrySet()){
		  ret.put(entry.getKey(), entry.getValue().get(0));
	  }
	  return ret;
  }

  public void addPermission(String permission) {
    permissions.add(permission);
  }
  
  public String toString(){
	  return "DataTable["
	  +"type: "+getType()+", "
	  +"facadeFor: "+type.getName()+", "
	  +"rows: "+rows+", "
	  +"size: "+size+", "
	  +"oob: "+oob+", "
	  +"]";
  }
}
