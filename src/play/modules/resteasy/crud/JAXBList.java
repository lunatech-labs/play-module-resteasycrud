package play.modules.resteasy.crud;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collection")
public class JAXBList {
	@XmlElement(name = "values")
	private List<String> list;

	public JAXBList(List<String> list) {
		this.list = list;
	}

	public JAXBList() {
	}

}
