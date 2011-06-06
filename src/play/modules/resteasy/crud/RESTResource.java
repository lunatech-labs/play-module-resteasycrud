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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import play.Logger;
import play.db.Model;
import play.db.Model.Factory;
import play.db.Model.Manager;
import play.db.Model.Property;

/**
 * Your CRUD resource must subclass this to gain automagic resources.
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public abstract class RESTResource {
	
	/**
	 * Queries the permission system for a permission
	 * @param target the object we want a permission for, can be a Class if this is a general permission
	 * @param name the name of the permission
	 * @return true if the current user has this permission, false otherwise
	 */
	protected boolean hasPermission(Object target, String name){
		return CRUDSecure.hasPermission(target, name);
	}

	/**
	 * Throws a FORBIDDEN exception if the user doesn't have the given permission
	 * @param target the object we want a permission for, can be a Class if this is a general permission
	 * @param name the name of the permission
	 */
	protected void checkPermission(Object target, String name) {
		if (!hasPermission(target, name))
			throw new WebApplicationException(HttpURLConnection.HTTP_FORBIDDEN);
	}

	/**
	 * Throws a NOT_FOUND if the given parameter is null
	 * @param <T> the type of parameter
	 * @param o the parameter to check
	 * @return the parameter
	 */
	protected <T> T checkNotFound(T o) {
		if (o == null)
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		return o;
	}

	/**
	 * Throws a NOT_FOUND with the given message if the given parameter is null
	 * @param <T> the type of parameter
	 * @param o the parameter to check
	 * @param msg the message format
	 * @param params the message parameters
	 * @return the parameter
	 */
	protected <T> T checkNotFound(T o, String msg, Object... params) {
		if (o == null)
			throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(String.format(msg, params)).build());
		return o;
	}

	/**
	 * Throws BAD_REQUEST if the given parameters are not null or are not empty collections
	 * @param objects the objects to check
	 */
	protected void checkEmpty(Object... objects) {
		for(Object o : objects){
			if (o == null)
				continue;
			if (o instanceof Collection && ((Collection<?>) o).isEmpty())
				continue;
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	/**
	 * Checks that we have the insert permission for the o object, and that oDB is not null and we have the update permission on it.
	 * @param <T> the parameter type
	 * @param o the new object we want to get values from
	 * @param oDB the old object we want to update
	 * @return the old object
	 */
	protected <T> T checkForUpdate(T o, T oDB) {
		checkPermission(o, "insert");
		checkNotFound(oDB);
		checkPermission(oDB, "update");
		return oDB;
	}

	/**
	 * Override if you have special checks for autocomplete queries
	 * @param values the autocomplete query values
	 */
	protected void checkAutoCompleteQuery(String... values) {
	}

	/**
	 * Returns a NOT_FOUND response
	 */
	protected Response notFound() {
		return status(HttpURLConnection.HTTP_NOT_FOUND);
	}

	/**
	 * Returns a FORBIDDEN response
	 */
	protected Response forbidden() {
		return status(HttpURLConnection.HTTP_FORBIDDEN);
	}

	/**
	 * Returns a BAD_REQUEST response
	 */
	protected Response badRequest() {
		return status(HttpURLConnection.HTTP_BAD_REQUEST);
	}

	/**
	 * Returns a BAD_REQUEST response with the specified message
	 * @param message the message format
	 * @param args the message parameters
	 */
	protected Response badRequest(String message, Object... args) {
		return status(HttpURLConnection.HTTP_BAD_REQUEST, message, args);
	}

	/**
	 * Returns a NO_CONTENT response
	 */
	protected Response noContent() {
		return status(HttpURLConnection.HTTP_NO_CONTENT);
	}

	/**
	 * Returns a CREATED response
	 */
	protected Response created() {
		return status(HttpURLConnection.HTTP_CREATED);
	}

	/**
	 * Returns a CREATED response with a Location header
	 */
	protected Response created(URI location) {
		return status(HttpURLConnection.HTTP_CREATED, location);
	}

	/**
	 * Returns an INTERNAL_ERROR response
	 */
	protected Response internalError() {
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR);
	}
	
	/**
	 * Returns an OK response
	 */
	protected Response ok() {
		return status(HttpURLConnection.HTTP_OK);
	}

	/**
	 * Returns an OK response with the given entity
	 * @param entity the entity to send
	 */
	protected Response ok(Object entity) {
		return status(HttpURLConnection.HTTP_OK, entity);
	}

	
	/**
	 * Returns an INTERNAL_ERROR response with the specified message
	 * @param message the message format
	 * @param args the message parameters
	 */
	protected Response internalError(String msg, Object... args) {
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR, msg, args);
	}

	/**
	 * Returns an INTERNAL_ERROR response with the specified message and logs the given error
	 * @param t the error to log
	 * @param message the message format
	 * @param args the message parameters
	 */
	protected Response internalError(Throwable t, String msg, Object... args) {
		Logger.error(t, msg, args);
		return status(HttpURLConnection.HTTP_INTERNAL_ERROR, msg, args);
	}

	/**
	 * Returns a response with the specified status code
	 * @param code the status code
	 */
	protected Response status(int code){
		Logger.info("Returning code %s", code);
		return Response.status(code).build();
	}
	
	/**
	 * Returns a response with the specified status code and message
	 * @param code the status code
	 * @param message the message format
	 * @param args the message parameters
	 */
	protected Response status(int code, String message, Object... args){
		String entity = String.format(message, args)+"\n";
		return status(code, entity);
	}

	/**
	 * Returns a response with the specified status code and entity
	 * @param code the status code
	 * @param entity the response entity
	 */
	protected Response status(int code, Object entity){
		Logger.info("Returning code %s: %s", code, entity);
		return Response.status(code).entity(entity).build();
	}

	/**
	 * Returns a response with the specified status code and Location header
	 * @param code the status code
	 * @param location the Location header
	 */
	protected Response status(int code, URI location){
		Logger.info("Returning code %s: %s", code, location);
		return Response.status(code).location(location).build();
	}

	/**
	 * Throws a WebApplicationException with the given response
	 * @param r the response to send to the client
	 */
	protected void respond(Response r) {
		throw toThrowable(r);
	}

	/**
	 * Makes a WebApplicationException with the given response
	 * @param r the response to wrap in a WebApplicationException
	 * @return the WebApplicationException
	 */
	protected WebApplicationException toThrowable(Response r) {
		return new WebApplicationException(r);
	}

	/**
	 * Makes an OK response with the given list of autocomplete results as entity
	 * @param list the list of autocomplete results
	 */
	protected Response autoComplete(List<String> list) {
		return ok(new JAXBList(list));
	}

	/**
	 * Throws an InvalidStateException for the given validation error
	 * @param entity the entity with an invalid value
	 * @param message the error message
	 * @param field the invalid field
	 * @param value the invalid value
	 */
	protected void throwConstraintValidation(Object entity, String message,
			String field, Object value) {
		throw new InvalidStateException(new InvalidValue[] { new InvalidValue(
				message, entity.getClass(), field, value, entity) });
	}

	//
	// CRUD
	
	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for getting a list of entities.
	 * @param model the model type
	 * @param q the query for the list of entities
	 * @return a response with the list of entities
	 */
	public <T extends Model> Response list(Class<T> model, DataTableQuery q){
		logQuery(q);
		checkPermission(model, "select");
		PagedQuery<T> carriers = findPaged(model);
		return makeQueryResponse(q, carriers, model,
				getSortableColumns(model), q.uriInfo);
	}

	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for getting a single entity.
	 * @param model the model type
	 * @param id the entity id
	 * @return a response with entity
	 */
	public <T extends Model> Response get(Class<T> model, Object id) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		T entity = (T)factory.findById(id);
		checkNotFound(entity, "Entity of type %s with id of %s could not be found", model.getName(), id);
		checkPermission(entity, "select");
		return ok(entity);
	}

	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for deleting an entity
	 * @param model the model type
	 * @param id the entity id
	 * @return a response with no content
	 */
	public <T extends Model> Response delete(Class<T> model, Object id) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		T entity = (T)factory.findById(id);
		checkNotFound(entity, "Entity of type %s with id of %s could not be found", model.getName(), id);
		checkPermission(entity, "delete");
		entity._delete();
		return noContent();
	}

	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for adding an entity.
	 * @param model the model type
	 * @param elem the new entity
	 * @return a response with no content
	 */
	public <T extends Model> Response add(Class<T> model, final T elem, UriInfo uriInfo) {
		checkPermission(elem, "insert");
		// check non-editable field
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				Object newValue;
				try {
					newValue = PropertyUtils.getSimpleProperty(elem, property.name);
				} catch (Exception e) {
					throw toThrowable(internalError(e, "Failed to get property %s", property.name));
				}
				// if that field is not editable, let us barf
				if(crud == null || !crud.editable())
					checkEmpty(newValue);
			}
		});
		elem._save();
		// now get the link to the new element
		UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
		URI uri = uriBuilder.path(getClass()).path(getClass(), "get").build(elem._key());
		return created(uri);
	}

	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for editing an entity.
	 * @param model the model type
	 * @param id the entity id to update
	 * @param elem the new values for the entity
	 * @return a response with no content
	 */
	public <T extends Model> Response edit(Class<T> model, Object id, final T elem) {
		Factory factory = Manager.factoryFor(model);
		@SuppressWarnings("unchecked")
		final T elemFromDB = (T)factory.findById(id);
		checkForUpdate(elem, elemFromDB);
		// copy every field
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				Object newValue;
				try {
					newValue = PropertyUtils.getSimpleProperty(elem, property.name);
				} catch (Exception e) {
					throw toThrowable(internalError(e, "Failed to get property %s", property.name));
				}
				// if that field is not editable, let us barf
				if(crud == null || !crud.editable())
					checkEmpty(newValue);
				else{
					// we can set it
					try{
						PropertyUtils.setSimpleProperty(elemFromDB, property.name, newValue);
					} catch (Exception e) {
						throw toThrowable(internalError(e, "Failed to set property %s", property.name));
					}
				}
			}
		});
		elemFromDB._save();
		return noContent();
	}

	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for getting a list of autocomplete entries for the given field and query
	 * @param model the model type
	 * @param field the field to autocomplete
	 * @param q the query for the autocompletion
	 * @return a response with the list of autocomplete enties
	 */
	public <T extends Model> Response autoComplete(Class<T> model, String field, String q) {
		checkAutoCompleteQuery(q);
		checkPermission(model, "select");
		return autoComplete(AutoComplete.getAutoComplete(model, field, q));
	}
	
	/**
	 * Override this method to implement your own endpoint, otherwise it will be magically bound to the
	 * right path and parameters for getting a descriptor for CRUD fields.
	 * @param model the model type
	 * @return a response with the CRUD fields descriptor
	 */
	public <T extends Model> Response descriptor(Class<T> model) {
		checkPermission(model, "select");
		return Response.ok(new Descriptor<T>(model)).build();
	}

	/**
	 * The CRUD fields descriptor
	 *
	 * @param <T> The type of entity
	 * @author Stéphane Épardaud <stef@epardaud.fr>
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Descriptor<T extends Model> {

		@XmlElement
		public List<Column> columns = new ArrayList<Column>();

		// For JAXB
		public Descriptor(){}
		
		public Descriptor(Class<T> model) {
			walkProperties(model, new PropertyWalker(){
				@Override
				public void walk(Property property, Field field, CRUDField crud) {
					addColumn(property, field, crud);
				}
			});
			CRUDOrder crudOrder = model.getAnnotation(CRUDOrder.class);
			if(crudOrder != null){
				List<Column> sortedColumns = new ArrayList<Column>();
				for(String field : crudOrder.value()){
					int i = findColumn(field);
					if(i == -1)
						throw new RuntimeException("No such field in model: "+field);
					sortedColumns.add(columns.remove(i));
				}
				// now add any remaining column
				sortedColumns.addAll(columns);
				// and swap
				columns = sortedColumns;
			}
		}

		private int findColumn(String field) {
			for(int i=0;i<columns.size();i++)
				if(columns.get(i).field.equals(field))
					return i;
			return -1;
		}

		private void addColumn(Property p, Field field, CRUDField crud) {
			// FIXME: only make it a column if it has a representation in JAXB, and allow @CRUD-less fields
			if(crud == null)
				return;
			columns.add(new Column(p, field, crud));
		}
		
	}

	/**
	 * CRUD field descriptor
	 * @author Stéphane Épardaud <stef@epardaud.fr>
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Column {
		@XmlElement
		public String name;
		@XmlElement
		public String field;
		@XmlElement
		public boolean editable;
		@XmlElement
		public boolean sortable;
		@XmlElement
		public boolean autocomplete;
		@XmlElement
		public String type;
		@XmlElement
		public List<String> validators = new ArrayList<String>();

		// For JAXB
		public Column(){}
		
		public Column(Property p, Field field, CRUDField crud) {
			this.field = name = p.name;
			editable = false;
			autocomplete = false;
			sortable = false;
			Class<?> fieldType = field.getType();
			setType(fieldType);
			if(crud != null){
				if(crud.name().length() > 0)
					name = crud.name();
				editable = crud.editable();
				autocomplete = crud.autoComplete();
				sortable = crud.sortable();
				Type crudType = crud.type();
				if(crudType != Type.DEFAULT)
					type = crudType.name();
			}
			addValidation(field);
		}

		private void addValidation(Field field) {
			if(field.isAnnotationPresent(NotEmpty.class)
					|| field.isAnnotationPresent(NotNull.class))
				validators.add("notempty");
		}

		private void setType(Class<?> fieldType) {
			if(fieldType == String.class){
				type = Type.STRING.name();
			}else if(fieldType == Integer.class
					|| fieldType == Integer.TYPE
					|| fieldType == Long.class
					|| fieldType == Long.TYPE
					|| fieldType == Short.class
					|| fieldType == Short.TYPE
					|| fieldType == BigInteger.class){
				type = Type.INTEGER.name();
			}else if(fieldType == Float.class
					|| fieldType == Float.TYPE
					|| fieldType == Double.class
					|| fieldType == Double.TYPE
					|| fieldType == BigDecimal.class){
				type = Type.DECIMAL.name();
			}else if(fieldType == Date.class
					|| fieldType == Calendar.class
					|| fieldType == java.sql.Date.class
					|| fieldType == java.sql.Timestamp.class){
				type = Type.DATE.name();
			}else if(fieldType == Boolean.class){
				type = Type.BOOLEAN.name();
			}else
				throw new RuntimeException("Unknown field type: "+fieldType.getName());
		}
	}
	
	/**
	 * Walks every property with CRUDFIeld
	 */
	public interface PropertyWalker {
		public void walk(Property property, Field field, CRUDField crud);
	}
	
	/**
	 * Walks every property with CRUDFIeld
	 */
	protected static <T extends Model> void walkProperties(Class<T> model, PropertyWalker walker){
		// FIXME: make this faster (10x slower than doing it manually for some reason)
		Factory factory = Manager.factoryFor(model);
		for(Property prop : factory.listProperties()){
			Field field = prop.field;
			CRUDField crud = field.getAnnotation(CRUDField.class);
			walker.walk(prop, field, crud);
		}
	}

	/**
	 * Gets the list of sortable columns for the given model
	 */
	protected <T extends Model> Set<String> getSortableColumns(Class<T> model) {
		final Set<String> ret = new HashSet<String>();
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				if(crud != null && crud.sortable())
					ret.add(property.name);
			}
		});
		return ret;
	}
	
	/**
	 * Gets the list of searchable columns for the given model
	 */
	protected <T extends Model> Set<String> getSearchableColumns(Class<T> model) {
		final Set<String> ret = new HashSet<String>();
		walkProperties(model, new PropertyWalker(){
			@Override
			public void walk(Property property, Field field, CRUDField crud) {
				if(crud != null && crud.searchable())
					ret.add(property.name);
			}
		});
		return ret;
	}

	/**
	 * Makes a paged query for the given entity
	 */
	protected <T extends Model> PagedQuery<T> findPaged(Class<T> model) {
		PagedQuery<T> query = new PagedQuery<T>("FROM "+model.getSimpleName());
		query.searchFields(getSearchableColumns(model));
		return query;
	}

	/**
	 * Makes a query response
	 * @param <T> the entity type
	 * @param q the query
	 * @param results the paged query results
	 * @param klass the entity type
	 * @param validColumns the list of valid search columns
	 * @param permissions the set of permissions to check and include in the response if the user has them
	 * @return the response
	 */
	protected <T> Response makeQueryResponse(DataTableQuery q,
			PagedQuery<T> results, Class<T> klass, Set<String> validColumns,
			UriInfo uriInfo, String... permissions) {
		return makeQueryResponse(q, results, klass, validColumns, null,
				uriInfo, permissions);
	}

	/**
	 * Makes a query response
	 * @param <T> the entity type
	 * @param q the query
	 * @param results the paged query results
	 * @param klass the entity type
	 * @param oob the OutOfBounds object to include in the response, if not null
	 * @param validColumns the list of valid search columns
	 * @param permissions the set of permissions to check and include in the response if the user has them
	 * @return the response
	 */
	protected <T> Response makeQueryResponse(DataTableQuery q,
			PagedQuery<T> results, Class<T> klass, Set<String> validColumns,
			Object oob, UriInfo uriInfo, String... permissions) {
		results.start = (long)q.start;
		results.limit = (long)q.length;
		if (isSortValid(q.sort, validColumns))
			results.order = q.sort;
		if(!StringUtils.isEmpty(q.search))
			results.search = q.search;
		DataTable<T> dataTable = makeDataTable(q.echo,
				results.getCount(), results.getResultList(), klass, oob,
				uriInfo);
		// add any permission we have
		for (String permission : permissions) {
			if (hasPermission(dataTable.facadeFor(), permission))
				dataTable.addPermission(permission);
		}
		Logger.info("Returning 200 with entity: %s", dataTable);
		return Response.ok(dataTable).build();
	}

	/**
	 * Override this to make your own DataTable with the appropriate {@link @XmlSeeAlso} annotation.
	 * If you do not, one will be provided for you with only T as JAXB context.
	 */
	protected <T> DataTable<T> makeDataTable(String echo, long count, List<T> results, Class<T> type, Object oob, UriInfo uriInfo){
		return new DataTable<T>(echo, count, results, type, oob, uriInfo);
	}

	/**
	 * Checks if the given sort query is valid according to the valid columns
	 * @param sort the query
	 * @param validColumns the valid columns
	 */
	protected boolean isSortValid(String sort, Set<String> validColumns) {
		if (sort == null)
			return false;
		final StringTokenizer tokenizer = new StringTokenizer(sort, ",");
		// we need at least one
		if (!tokenizer.hasMoreTokens())
			return false;
		while (tokenizer.hasMoreTokens()) {
			final String fragment = tokenizer.nextToken().trim();
			String field;
			if (fragment.toLowerCase().endsWith(" desc"))
				field = fragment.substring(0, fragment.length() - 5).trim();
			else if (fragment.toLowerCase().endsWith(" asc"))
				field = fragment.substring(0, fragment.length() - 4).trim();
			else
				field = fragment;
			// can be an int or an allowed name
			if (!field.matches("\\d+") && !validColumns.contains(field))
				return false;
		}
		return true;
	}

	/**
	 * Logs an entity query
	 */
	protected void logQuery(DataTableQuery q) {
		Logger.info("GET start: %s, length: %s, echo: %s, sort: %s, search: %s",
				q.start, q.length, q.echo, q.sort, q.search);
	}

}
