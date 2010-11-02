package play.modules.resteasy.crud;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.JPA;

public class AutoComplete {
	public static final int AUTOCOMPLETE_MAX_RESULTS = 10;

	public static List<String> getAutoComplete(Class<?> entity, String field, String q) {
		String hql = "SELECT DISTINCT " + field + " FROM " + entity.getName();
		if (!StringUtils.isEmpty(q))
			hql += " WHERE LOCATE(lower(:q), lower(" + field + ")) > 0";
		hql += " ORDER BY " + field;
		Query query = JPA.em().createQuery(hql);
		query.setMaxResults(AUTOCOMPLETE_MAX_RESULTS);
		if (!StringUtils.isEmpty(q))
			query.setParameter("q", q);
		return (List<String>)query.getResultList();
	}

}
