package play.modules.resteasy.crud;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InvalidResponse {

	private List<InvalidResponseField> errors = new ArrayList<InvalidResponseField>();
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