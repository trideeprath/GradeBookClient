package example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Exam extends HttpServlet{
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println(">> URI:  " + req.getRequestURI());
		 
		
		String name = req.getParameter("name");
		String percentage = req.getParameter("percentage");
		
	
		try {
			insertToDB(name,percentage,resp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
				
	
	}

	private void insertToDB(String name, String percentage, HttpServletResponse resp) throws IOException, ParseException {
		// TODO Auto-generated method stub
		
		
		
		
		if(ExamData.exams.containsKey(name)){
			resp.getWriter().println("Exam with name " + name +  " already existing" );
			resp.setStatus(400);
		}
		else{
			String path = Settings.getPath(getServletContext());
			System.out.println(path);
			
			File file1 = new File(path);
		
			//No student exists
			if(file1.length() == 0){
				resp.getWriter().println("No student existing in DB, First students have to be added" );
				resp.setStatus(400);
			}
			//Students are existing, exam needs to be added for all students
			else{
				ExamData.exams.put(name, percentage);
				
				JSONParser parser = new JSONParser();
				FileReader fr = new FileReader(path);
				Object obj = parser.parse(fr);
				fr.close();
				JSONObject mainObject = (JSONObject) obj;
				JSONArray jsonArray = (JSONArray) mainObject.get("Students");
				JSONArray newJsonArray = new JSONArray();
				for(int i =0 ; i < jsonArray.size() ; i++){
					JSONObject studentObject = (JSONObject) jsonArray.get(i);
					studentObject.put("E_"+name+"_Grade", "");
					studentObject.put("E_"+name+"_Feedback", "");
					newJsonArray.add(studentObject);
				}
				for(int i = 0 ; i <newJsonArray.size(); i++){
					System.out.println(newJsonArray.get(i).toString());
				}
				mainObject.remove("Students");
				mainObject.put("Students", newJsonArray);
				FileWriter file = new FileWriter(path);
				file.write(mainObject.toJSONString());
				file.flush();
				file.close();
				resp.getWriter().println("Exam have been added for all students");
				resp.setStatus(200);
			}
			

			
		}
		
		
	}
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//System.out.println(">> URI:  " + req.getRequestURI());
		
		String request = req.getRequestURI();
		System.out.println(request);
		String[] params = request.split("/");
		System.out.println(params.length);
		for(String str : params)
			System.out.println(str);
		String exam = params[params.length-1];
		
		System.out.println("Do delete exam : " + exam);
		
		if(exam == null){
			resp.getWriter().println("No exam parameter passed");
		}
		else{
			if(!isExamExisting(exam)){
				resp.getWriter().println("Exam does not exist");
			}
			else{
				
				ExamData.exams.remove(exam);
				JSONParser parser = new JSONParser();
				Object obj =null;
				try {
					obj = parser.parse(new FileReader(Settings.getPath(getServletContext())));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray students= (JSONArray) jsonObject.get("Students");
				JSONArray newstudents = new JSONArray();
				for(int i =0 ; i<students.size() ; i++){
					JSONObject student = (JSONObject) students.get(i);
					student.remove("E_"+exam+"_Grade");
					student.remove("E_"+exam+"_Feedback");
					newstudents.add(student);
				}
				jsonObject.remove("Students");
				jsonObject.put("Students",newstudents);
				
				FileWriter file = new FileWriter(Settings.getPath(getServletContext()));
				file.write(jsonObject.toJSONString());
				file.flush();
				file.close();
				resp.getWriter().println("Exam " + exam + " removed for all students ");
				resp.setStatus(200);
				
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
	

}
