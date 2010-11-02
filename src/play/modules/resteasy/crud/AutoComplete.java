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

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.JPA;

/**
 * Utilities for getting field completion.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class AutoComplete {
	/**
	 * Default number of autocomplete results
	 */
	public static final int AUTOCOMPLETE_MAX_RESULTS = 10;

	/**
	 * Gets a list of autocomplete for the given entity and field.
	 * @param entity the type of entity
	 * @param field the property of the entity to autocomplete for.
	 * @param q an optional query for the completion
	 * @return a list of max AUTOCOMPLETE_MAX_RESULTS autocomplete results
	 */
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
