package example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion.Setting;

import sun.org.mozilla.javascript.internal.json.JsonParser;

public class Student extends HttpServlet {
	
	

	//This is to add a student to the db
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println(">> URI:  " + req.getRequestURI());
		 
		
		String id = req.getParameter("id");
		String name = req.getParameter("name");
		
		try {
			insertToDB(id,name,resp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("id  " + id + "name " + name);
		
				
	
	}
	



	private void insertToDB(String id, String name, HttpServletResponse resp) throws FileNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
		String path = Settings.getPath(getServletContext());
		System.out.println(path);
		
		JSONParser parser = new JSONParser();
		
		
		//BufferedReader br = new BufferedReader(new FileReader(path));     
		
		File file1 = new File(path);
		

		//Insert first student data
		if(file1.length() == 0){	
			//br.close();
			System.out.println("Inserting first student data");
			JSONObject mainObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			JSONObject studentObject = new JSONObject();
			studentObject.put("id", id);
			studentObject.put("name", name);
			jsonArray.add(studentObject);
			mainObject.put("Students", jsonArray);
			FileWriter file = new FileWriter(path);
			file.write(mainObject.toJSONString());
			file.flush();
			file.close();
			resp.getWriter().println("Student added with id: " + id);
			resp.setStatus(200);
			
			
		}
		//Append student to existing DB
		else{
			System.out.println("Data exists1");
			Object obj = parser.parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray jsonArray= (JSONArray) jsonObject.get("Students");
			if(!isStudentExisting(jsonArray,id)){
				JSONObject studentObject = new JSONObject();
				studentObject.put("id", id);
				studentObject.put("name", name);
				for(String examName : ExamData.exams.keySet()){
					studentObject.put("E_"+examName+"_Grade", "");
					studentObject.put("E_"+examName+"_Feedback", "");
				}
				jsonArray.add(studentObject);
				jsonObject.put("Students", jsonArray);
				
				FileWriter file = new FileWriter(path);
				file.write(jsonObject.toJSONString());
				file.flush();
				file.close();
				resp.getWriter().println("Student added with id: " + id);
				resp.setStatus(200);
			}
			else{
				//Student already existing
				System.out.println("Student already existing");
				resp.getWriter().println("Student with id: " + id + " already existing");
				resp.setStatus(200);
			}
			
			
			
		}
		
	
		
		
		
		
	}




	private boolean isStudentExisting(JSONArray students, String id) {
		// TODO Auto-generated method stub
		
		for(int i = 0 ; i < students.size() ; i++){
			JSONObject student = (JSONObject) students.get(i);
			if(student.get("id").equals(id)){
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws FileNotFoundException, IOException{
		
		
		String request = req.getRequestURI();
		System.out.println(request);
		String[] params = request.split("/");
		System.out.println(params.length);
		for(String str: params)
			System.out.println(str);
		
		
		String path = Settings.getPath(getServletContext());
		
		
		
		//Send all data
		if(params.length == 3){
			sendAllData(req,res,path);
		}
		else if( params.length ==4){
			String id = params[3];
			File file1 = new File(path);
			if(file1.length() == 0){
				res.getWriter().println("No Student exist in Database");
				res.setStatus(200);
			}
			else{
				JSONParser parser = new JSONParser();
				Object obj= null;
				try {
					obj = parser.parse(new FileReader(path));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray students= (JSONArray) jsonObject.get("Students");
				if(isStudentExisting(students, id)){
					for(int i =0 ; i<students.size() ;i++){
						JSONObject student = (JSONObject) students.get(i);
						if(student.get("id").equals(id)){
							res.getWriter().println(student.toJSONString());
							res.setStatus(200);
						}
					}
				}
				else{
					res.getWriter().println("No Student exist in Database with Id: " + id);
					res.setStatus(400);
				}
			}
		}
					
	}
	
	
	public static void sendAllData(HttpServletRequest req, HttpServletResponse res, String path) throws FileNotFoundException, IOException{

		File file1 = new File(path);
		//Insert first student data
		if(file1.length() == 0){
			res.getWriter().println("No Student exist in Database");
			res.setStatus(200);
		}
		
		else{
			JSONParser parser = new JSONParser();
			Object obj= null;
			try {
				obj = parser.parse(new FileReader(path));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray jsonArray= (JSONArray) jsonObject.get("Students");
			String responseStr="";	
			for(int i = 0 ; i<jsonArray.size() ; i++){
				responseStr = responseStr + "\n" + jsonArray.get(i).toString();
			}
			res.getWriter().println(responseStr);
			res.setStatus(200);
		}

	}
	 
	@Override
	public void doPut(HttpServletRequest req,  HttpServletResponse resp) throws IOException {
		
		String request = req.getRequestURI();
		System.out.println(request);
		String[] params = request.split("/");
		System.out.println(params.length);
		for(String str : params)
			System.out.println(str);
	
		String marks = req.getParameter("marks");
		String feedback = req.getParameter("feedback");
		System.out.println(marks + "  " + feedback);
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));

		String data = br.readLine();
		System.out.println(data);
		String[] parameters = data.split("&");
		
		feedback = parameters[0].split("=")[1];
		marks = parameters[1].split("=")[1];
		
		
		
		if(params.length!=5){
			resp.getWriter().println("Invalid URI");
		}
		else{
			String id = params[3];
			String exam = params[4];
			System.out.println(id + " " + exam);
			JSONParser parser = new JSONParser();
			Object obj= null;
			try {
				obj = parser.parse(new FileReader(Settings.getPath(getServletContext())));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray students= (JSONArray) jsonObject.get("Students");
			if(!isStudentExisting(students, id)){
				resp.getWriter().println("Student with id: " +  id + " does not exist");
			}	
			else{
				if(!isExamExisting(exam)){
					resp.getWriter().println("Exam with name: " +  exam + " does not exist");
				}
				else{
					for(int i =0 ; i < students.size(); i++){
						JSONObject student = (JSONObject) students.get(i);
						if(student.get("id").equals(id)){
							student.put("E_"+exam+"_Grade", marks);
							student.put("E_"+exam+"_Feedback", feedback);
							students.remove(i);
							students.add(student);
							jsonObject.put("Students",students);
							FileWriter file = new FileWriter(Settings.getPath(getServletContext()));
							file.write(jsonObject.toJSONString());
							file.flush();
							file.close();
							resp.getWriter().println("Marks updated for student with id: " + id);
							resp.setStatus(200);
							break;
						}
						
						
					}
				}
			}
		}
	 
	}


	private boolean isExamExisting(String exam) {
		// TODO Auto-generated method stub
		
		if(ExamData.exams.containsKey(exam)){
			return true;
		}
		return false;
	}

	
	public void doDelete(HttpServletRequest req,  HttpServletResponse resp) throws IOException {
			
			String request = req.getRequestURI();
			System.out.println(request);
			String[] params = request.split("/");
			System.out.println(params.length);
			for(String str : params)
				System.out.println(str);
		
			
			if(params.length!=5){
				resp.getWriter().println("Invalid URI");
			}
			else{
				String id = params[3];
				String exam = params[4];
				System.out.println(id + " " + exam);
				JSONParser parser = new JSONParser();
				Object obj= null;
				try {
					obj = parser.parse(new FileReader(Settings.getPath(getServletContext())));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray students= (JSONArray) jsonObject.get("Students");
				if(!isStudentExisting(students, id)){
					resp.getWriter().println("Student with id: " +  id + " does not exist");
				}	
				else{
					if(!isExamExisting(exam)){
						resp.getWriter().println("Exam with name: " +  exam + " does not exist");
					}
					else{
						for(int i =0 ; i < students.size(); i++){
							JSONObject student = (JSONObject) students.get(i);
							if(student.get("id").equals(id)){
								student.put("E_"+exam+"_Grade", "");
								student.put("E_"+exam+"_Feedback", "");
								students.remove(i);
								students.add(student);
								jsonObject.put("Students",students);
								FileWriter file = new FileWriter(Settings.getPath(getServletContext()));
								file.write(jsonObject.toJSONString());
								file.flush();
								file.close();
								resp.getWriter().println("Marks deleted for student with id: " + id);
								resp.setStatus(200);
								break;
							}
							
							
						}
					}
				}
			}
		 
		}
	
	
	
}
