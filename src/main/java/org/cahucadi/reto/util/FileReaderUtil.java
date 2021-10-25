package org.cahucadi.reto.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class FileReaderUtil {

	/**
	 * @param fileName name of the file to read
	 * @return List of lines read from file
	 * @throws IOException if File doesn't exist
	 */
	private static List<String> readFile(String fileName) throws IOException {

		ClassLoader classLoader = FileReaderUtil.class.getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());

		List<String> content = Collections.emptyList();
	    content = Files.readAllLines(file.toPath());
	    return content;
	    
	}
	
	/**
	 * @param fileName name of the file to read
	 * @return List of lines read from file
	 * @throws IOException if File doesn't exist
	 */
	public static void writeFile(String fileName, String content) throws IOException {

		Path path = Paths.get(fileName);
	    Files.writeString(path, content, StandardCharsets.UTF_8);

	}

	/**
	 * @param fileName ame of the file to read
	 * @return a TreeMap with table name on key and a HashMap for filters on values
	 * @throws Exception
	 */
	public static TreeMap<String, HashMap<String, String>> loadFileContent(String fileName) throws Exception {

		TreeMap<String, HashMap<String, String>> result = new TreeMap<String, HashMap<String, String>>();

		List<String> linesList = readFile(fileName);
		
		HashMap<String, String> filter = new HashMap<String, String>();
		String currentTable = "";
		
		for (Iterator<String> iterator = linesList.iterator(); iterator.hasNext();) {
			
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
