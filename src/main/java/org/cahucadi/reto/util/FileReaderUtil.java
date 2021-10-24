package org.cahucadi.reto.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileReaderUtil {

	private static List<String> readFile(String fileName) throws IOException {

		ClassLoader classLoader = FileReaderUtil.class.getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());

		List<String> content = Collections.emptyList();
	    content = Files.readAllLines(file.toPath());
	    return content;
	    
	}

	public static HashMap<String, HashMap<String, String>> loadFileContent(String fileName) throws Exception {

		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();

		List<String> linesList = readFile(fileName);
		
		HashMap<String, String> filter = new HashMap<String, String>();
		String currentTable = "";
		
		for (Iterator iterator = linesList.iterator(); iterator.hasNext();) {
			
			String line = (String) iterator.next();
			
			if (line.startsWith("<")) {
	
				if(!line.equals(currentTable) ) {
					
					if(currentTable.equals(""))
							currentTable = line;
					
					result.put(currentTable, filter);
					filter = new HashMap<String, String>();
					currentTable = line;
				}
				
			} else {
				
				String[] keyValue = line.split(":");
				filter.put(keyValue[0], keyValue[1]);
				
				if(!iterator.hasNext()) {
					result.put(currentTable, filter);
				}
				
			}
		}
		
		return result;

	}
}
