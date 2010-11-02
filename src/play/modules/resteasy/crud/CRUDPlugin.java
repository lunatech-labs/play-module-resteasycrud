/*
    This file is part of resteasy-play-module.
    
    Copyright Lunatech Research 2010

    resteasy-play-module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    resteasy-play-module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Lesser Public License
    along with resteasy-play-module.  If not, see <http://www.gnu.org/licenses/>.
*/

package play.modules.resteasy.crud;

import play.Logger;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

public class CRUDPlugin extends PlayPlugin {

	private static void log(String message, Object... params){
		Logger.info("RESTEasy CRUD plugin: "+message, params);
	}

    public void enhance(ApplicationClass applicationClass) throws Exception {
    	log("Enhancing? %s", applicationClass.name);
    	new CRUDEnhancer().enhanceThisClass(applicationClass);
    }

}
