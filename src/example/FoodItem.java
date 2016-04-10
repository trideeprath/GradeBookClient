package example;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import jdk.internal.jfr.events.FileWriteEvent;
//import sun.org.mozilla.javascript.internal.json.JsonParser;

public class FoodItem extends HttpServlet {

	
	
	public static String serverRoot = "";
	
	public static String pathForWindows = "\\WEB-INF\\";
	public static String pathForMacAndLinux = "/WEB-INF/";
	
	//This variable has to be changed according the the System in use. 
	public static String pathToBeUsed = pathForWindows;
	
	
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println(">> URI: POST  " + req.getRequestURI());
		 
		serverRoot = getServletContext().getRealPath("/");
		
		String xml = getRequestString(req);

		//InputStream input = getServletContext().getResourceAsStream("/WEB-INF/foo.properties");
		
		//resp.getWriter().println(xml);
		//resp.setStatus(400);
		
		
		
		
		int type;
		try {
			type = getType(xml);
			//resp.getWriter().println(type);
			switch(type){
			// 1 for NewFoodItem
			case 1: 
				addToDB(xml,resp);
				break;
				
			//2 for GetFoodItem	
			case 2:
				String response = retrieveFromDB(xml);
				resp.getWriter().println(response);
				break;
			
			case 3: 
				String res = createAddErrorResponse();
				resp.getWriter().println(res);
				
			}
			
			
		}
		catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String responseXML = createAddErrorResponse();
			resp.getWriter().println(responseXML);
		} 
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			 
	}
	 
	 
	public String retrieveFromDB(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		
		ArrayList<String> idArray = getIds(xml);
		String responseString = "";
		responseString = startEnd(0);
		for(String id : idArray){
			boolean exists = isPresentInDB(id);
			//System.out.println(id + " " + exists);
			if(exists){
				JSONObject food = getFoodFromDb(id);
				String country = (String)food.get("-country");
				String name = (String)food.get("name");
				String description = (String)food.get("description");
				String category = (String)food.get("category");
				String price = (String)food.get("price");
				responseString += createNewResponse(exists, country, id, name, description, category, price);
				//System.out.println(country + " " + id + " " + name + " " + description + " " + category + " "+ price);
				
				
			}
			else{
				responseString += createNewResponse(exists,null,id,null,null,null,null);
				
			}
			
			//responseString += getResponseString(exists,country,id,name,description,category,price);
		}
		
		responseString += startEnd(1);
		
		System.out.println("==============");
		System.out.println(responseString);
		
		return responseString;
	}
	

	public static String createNewResponse(boolean exists,String country,String id, String name, String desc,String category,String price){
		String xml = "";
		if (exists){
		xml += "<FoodItem country="+ "\"" + country  + "\">";
		xml += "<id>"+ id +"</id>";
		xml += "<name>"+ name +"</name>";
		xml += "<description>"+ desc +"</description>";
		xml += "<category>"+ category +"</category>";
		xml += "<price>"+ price +"</price>";
		xml += "</FoodItem>";}
		else{
			xml += "<InvalidFoodItem>";
			xml += "<FoodItemId>" + id + "</FoodItemId>";
			xml += "</InvalidFoodItem>";
		}
		
		return xml;
		
	}
	
	public static String startEnd(int position){
		String xml = "";
		if (position == 0){
		xml += "<RetrievedFoodItems xmlns=\"http://cse564.asu.edu/PoxAssignment\">";
		}
		else if(position == 1){
			xml += "</RetrievedFoodItems>";
		}
		return xml;
	}


	private JSONObject getFoodFromDb(String id) {
		// TODO Auto-generated method stub
		
		JSONParser parser = new JSONParser();
		boolean exists = false;
		JSONObject foodObj = null;
		try {
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed +"db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			for(int i =0 ; i< foodItems.size() ; i++){
				JSONObject food = (JSONObject) foodItems.get(i);
				if(food.get("id").equals(id)){
					foodObj = food;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return foodObj;

	}


	private boolean isPresentInDB(String id) {
		// TODO Auto-generated method stub		
		JSONParser parser = new JSONParser();
		boolean exists = false;
		
		try {
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed + "db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			for(int i =0 ; i< foodItems.size() ; i++){
				JSONObject food = (JSONObject) foodItems.get(i);
				if(food.get("id").equals(id)){
					exists = true;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return exists;

	}


	private ArrayList<String> getIds(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				
				InputSource is = new InputSource();
				ArrayList<String> al= new ArrayList<String>();
		        is.setCharacterStream(new StringReader(xml));
				Document doc = dBuilder.parse(is);
				//NodeList n1= doc.getAttributes();
				NodeList n1= doc.getElementsByTagName("FoodItemId");
				
				
				for(int i =0 ; i< n1.getLength() ; i++){
					Element element = (Element) n1.item(i);
					String id = element.getTextContent();
					al.add(id);
							
				}
				
				
				return al;
		
		
		
	}


	public static void addToDB(String xml, HttpServletResponse resp) throws NullPointerException, ParseException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		String name = fetchName(xml);
		String category = fetchCategory(xml);
		String description = fetchDescription(xml);
		String country = fetchCountry(xml);
		String price = fetchPrice(xml);
		
		System.out.println(country);
		
		String responseXML ="";
		if((name.length() ==0) || (category.length() == 0)){
			responseXML = createAddErrorResponse();
			resp.getWriter().println(responseXML);
			
		}
		else if(ifExistsinDB(name,category)){
			int itemId =  getItem(name,category);
			responseXML = createExistingFoodResponse(itemId);
			//responseXML = String.valueOf(itemId);
			resp.getWriter().println(responseXML);
		}
	
		
		else if(!ifExistsinDB(name,category)){
			int itemId = getAddedItemId();
			addFoodToDb(country,itemId,name,description,category,price);
			responseXML = createAddedFoodResponse(itemId);
			resp.getWriter().println(responseXML);
		}
		
		
		
	}
	
	
	private static void addFoodToDb(String country, int itemId, String name, 
			String description, String category,String price) throws FileNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
			System.out.println(country+ " " + itemId + " " + name + " " + description + " " +
		" " + category + " " + price );
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed + "db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			
			JSONObject food = new JSONObject();
			food.put("-country",country);
			food.put("id", String.valueOf(itemId));
			food.put("name", name);
			food.put("description", description);
			food.put("category", category);
			food.put("price", price);
			
			foodItems.add(food);
			
			FileWriter file = new FileWriter(serverRoot + pathToBeUsed + "db.txt");
			file.write(jsonObject.toJSONString());
			
			file.flush();
			file.close();
		
	}


	private static int getAddedItemId() throws ParseException {
		// TODO Auto-generated method stub
		JSONParser parser = new JSONParser();
		int max =0;
		
		try {
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed+ "db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			for(int i =0 ; i< foodItems.size() ; i++){
				JSONObject food = (JSONObject) foodItems.get(i);
				int value = Integer.valueOf((String)food.get("id"));
				if(value > max){
					max = value;
				}
			}
			max = max+1;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return max;
	}


	public static String createAddErrorResponse(){
		String xml = "<InvalidMessage xmlns=\"http://cse564.asu.edu/PoxAssignment\"/>";
		return xml;
	}
	
	public static String createAddedFoodResponse(int id){
		String xml = "<FoodItemAdded xmlns=\"http://cse564.asu.edu/PoxAssignment\">";
		xml += "<FoodItemId>";
		xml += Integer.toString(id);
		xml += "</FoodItemId>";
		xml += "</FoodItemAdded>";
		return xml;
	}
	
	
	public static String fetchCountry(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
		Document doc = dBuilder.parse(is);
		NodeList n1= doc.getElementsByTagName("FoodItem");
		  Element e1=(Element)n1.item(0);
		  NamedNodeMap base=e1.getAttributes();
		String country=null;
		for(int i=0;i<base.getLength();i++)
		{
		  
		    Node attr= base.item(i);
		    country=attr.getNodeValue().toString();
		}
		
		return country;
	}
	
	
	public static String fetchPrice(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
		Document doc = dBuilder.parse(is);
		NodeList n1= doc.getChildNodes();
		
		String price=null;
		for(int i=0;i<n1.getLength();i++)
		{
		    Element e1=(Element)n1.item(i);
		    price=e1.getElementsByTagName("price").item(0).getTextContent();
		}
		
		return price;
	}
	
	
	public static String fetchDescription(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
		Document doc = dBuilder.parse(is);
		NodeList n1= doc.getChildNodes();
		
		String description=null;
		for(int i=0;i<n1.getLength();i++)
		{
		    Element e1=(Element)n1.item(i);
		    description=e1.getElementsByTagName("description").item(0).getTextContent();
		}
		
		return description;
	}
	
	public static String fetchCategory(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
		Document doc = dBuilder.parse(is);
		NodeList n1= doc.getChildNodes();
		
		String category=null;
		for(int i=0;i<n1.getLength();i++)
		{
		    Element e1=(Element)n1.item(i);
		    category=e1.getElementsByTagName("category").item(0).getTextContent();
		}
		
		return category;
	}
	
	
	public static String fetchName(String xml) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
		Document doc = dBuilder.parse(is);
		NodeList n1= doc.getChildNodes();
		
		String name=null;
		for(int i=0;i<n1.getLength();i++)
		{
		    Element e1=(Element)n1.item(i);
		    name=e1.getElementsByTagName("name").item(0).getTextContent();
		}
		
		return name;
	}


	private static int getItem(String name,String category) throws ParseException {
		// TODO Auto-generated method stub
		int itemId = 0;
		JSONParser parser = new JSONParser();
		
		try {
			
			//Object obj = parser.parse(new FileReader("C:\\Users\\Trideep\\workspace\\REST\\db.txt"));
			
			
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed + "db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			for(int i =0 ; i< foodItems.size() ; i++){
				JSONObject food = (JSONObject) foodItems.get(i);
				String fname = (String) food.get("name");
				String cname = (String) food.get("category");
				if(fname.equals(name) && cname.equals(category)){
					itemId = Integer.valueOf((String)food.get("id"));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return itemId;

	}


	private static boolean ifExistsinDB(String name, String category) {
		// TODO Auto-generated method stub
		
		JSONParser parser = new JSONParser();
		boolean exists = false;
		
		try {
			
			Object obj = parser.parse(new FileReader(serverRoot+ pathToBeUsed  + "db.txt"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray foodItems = (JSONArray) jsonObject.get("FoodItemData");
			for(int i =0 ; i< foodItems.size() ; i++){
				JSONObject food = (JSONObject) foodItems.get(i);
				String fname = (String) food.get("name");
				String cname = (String) food.get("category");
				if(fname.equals(name) && cname.equals(category)){
					exists = true;
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return exists;

	}
	
	

	public static int getType(String xml) throws ParserConfigurationException, SAXException, IOException {
		String Stringtype = "";
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
    	
        Document doc = dBuilder.parse(is);
		doc.getDocumentElement().normalize();
		Stringtype = doc.getDocumentElement().getNodeName();
		
		if(Stringtype.equals("NewFoodItems")){
			return 1;
		}
		else if(Stringtype.equals("SelectedFoodItems")){
			return 2;
		}
		else{
			return 3;
		}
		
		
	}


	public static String getRequestString(HttpServletRequest req) {
		// TODO Auto-generated method stub
		String xml = null;
		try{
			byte[] xmlData = new byte[req.getContentLength()];
			
			InputStream sis = req.getInputStream();
			
			BufferedInputStream bis = new BufferedInputStream(sis);
			bis.read(xmlData,0,xmlData.length);
			
			if(req.getCharacterEncoding() !=null){
				xml = new String(xmlData, req.getCharacterEncoding());
			}else{
				xml = new String(xmlData);
			}
			
			//resp.setStatus(400);
			//resp.getWriter().println(xml);
			return xml;
		}
		catch(IOException ie){
			System.out.println(ie.toString());
		}

		return xml;
	}


	 
	public static String createExistingFoodResponse(int id){
		String xml = "<FoodItemExists xmlns=\"http://cse564.asu.edu/PoxAssignment\">";
		xml += "<FoodItemId>";
		xml += Integer.toString(id);
		xml += "</FoodItemId>";
		xml += "</FoodItemExists>";
		return xml;
	}
	
	 
}
