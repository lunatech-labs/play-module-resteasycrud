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

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

/**
 * Enhances RESTResource classes
 * 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class CRUDPlugin extends PlayPlugin {

    public void enhance(ApplicationClass applicationClass) throws Exception {
    	Thread.currentThread().setContextClassLoader(Play.classloader);
    	new CRUDEnhancer().enhanceThisClass(applicationClass);
    }

}
