package com.telus.credit.dao;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class DaoHelper {

	public static void removeNullAttributes(Map<String, Object> updateMap) {
	
	try {
		if(updateMap!=null ) {       
			   Set<Entry<String, Object>> setOfEntries = updateMap.entrySet();
			   Iterator<Entry<String, Object>> iterator = setOfEntries.iterator();    
			   while (iterator.hasNext()) {
				   Entry<String, Object> entry = iterator.next(); 
				   Object value = entry.getValue();
				   if(value== null) {
					   iterator.remove(); 	   
				   }
		   }
		}
	}
	catch(Throwable e) {
		e.printStackTrace();
	}

	}

}


