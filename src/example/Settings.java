package example;

import javax.servlet.ServletContext;

public class Settings {

	public static String pathForWindows = "\\WEB-INF\\";
	public static String pathForMacAndLinux = "/WEB-INF/";
	
	//This variable has to be changed according the the System in use. 
	public static String pathToBeUsed = pathForWindows;

	public static String getPath(ServletContext servletContext) {
		// TODO Auto-generated method stub
		return servletContext.getRealPath("/")+ pathToBeUsed + "db_gradebook.txt";
		
	}
}
