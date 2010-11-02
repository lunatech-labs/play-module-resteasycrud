package play.modules.resteasy.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;

public class PagedQuery<T> {
	private String hql;
	public String order;
	public String group;
	public Map<String, Object> parameters = new HashMap<String,Object>();
	public String search;
	public List<String> searchFields = new ArrayList<String>();
	public Long start;
	public Long limit;
	private Long count;

	public PagedQuery(String hql){
		this.hql = hql;
	}

	public PagedQuery<T> searchFields(String... searchFields){
		this.searchFields.clear();
		Collections.addAll(this.searchFields, searchFields);
		return this;
	}

	public PagedQuery<T> searchFields(Set<String> searchFields){
		this.searchFields.clear();
		this.searchFields.addAll(searchFields);
		return this;
	}

	private String getHQLQuery(boolean forSelect){
		String hql = this.hql;
		if (!StringUtils.isEmpty(search) && !searchFields.isEmpty()) {
			if (hql.toLowerCase().contains(" where ")) {
				hql += " AND (";
			} else {
				hql += " WHERE (";
			}
			boolean first = true;
			for (String field : searchFields) {
				if (!first) {
					hql += " OR ";
				} else
					first = false;
				hql += "LOCATE(lower(:_search), lower(" + field + ")) > 0";
			}
			hql += ")";
			parameters.put("_search", search);
		}
		if(forSelect && !StringUtils.isEmpty(group))
			hql += " GROUP BY " +group;
		if(forSelect && !StringUtils.isEmpty(order))
			hql += " ORDER BY " +order;
		return hql;
	}

	private Query getCountQuery() {
		String hql = getHQLQuery(false);
		hql = hql.trim();
		// FIXME: make this safe
		int fromClause = hql.toLowerCase().indexOf("from ");
		String count;
		if(StringUtils.isEmpty(group))
			count = "COUNT(*) ";
		else{
			count = "COUNT(DISTINCT "+group+") ";
		}
		hql = "SELECT "+count + hql.substring(fromClause);
		return getQuery(hql);
	}

	private Query getQuery() {
		return getQuery(getHQLQuery(true));
	}

	private Query getQuery(String hql) {
		Logger.info("Making HQL query: %s", hql);
		Query query = JPA.em().createQuery(hql);
		for(Entry<String, Object> entry : parameters.entrySet()){
			query.setParameter(entry.getKey(), entry.getValue());
			Logger.info(" Query param %s => %s", entry.getKey(), entry.getValue());
		}
		return query;
	}
	
	public long getCount(){
		if(count == null){
			Query query = getCountQuery();
			count = (Long) query.getSingleResult();
		}
		return count;
	}
	
	public List<T> getResultList(){
		Query query = getQuery();
		if(start != null)
			query.setFirstResult(start.intValue());
		if(limit != null)
			query.setMaxResults(limit.intValue());
		return query.getResultList();
	}
}
