package org.sakaiproject.tool.assessment.jsf.convert;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import org.apache.commons.text.StringEscapeUtils;

@FacesConverter("org.sakaiproject.tool.assessment.jsf.convert.AnswerHTMLConverter")
public class AnswerHTMLConverter implements Converter {

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		
		return getAsString(arg0, arg1, arg2);
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String text = (String)arg2;
		if (text == null) return null;

		return StringEscapeUtils.unescapeHtml4(text);
	}

}
