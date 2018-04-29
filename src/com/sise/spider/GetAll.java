package com.sise.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sise.bean.Course;
import com.sise.bean.CourseDetail;
import com.sise.bean.Messages;

public class GetAll {

	public static Map<String, String> getCookies(String name, String password)
			throws IOException {
		// 1.连接至SISE
		Connection con1 = Jsoup
				.connect("http://class.sise.com.cn:7001/sise/login.jsp");

		// 2.获取login.jsp页面内容，为了获取表单项做准备
		Response re1 = con1.execute();
		String html = re1.body();
		Document doc = Jsoup.parse(html);

		// 3.获取随机表单项
		Element ele = doc.select("form input").get(0);
		String Rname = ele.attr("name");
		String value = ele.attr("value");

		// 4.添加表单项内容
		Map<String, String> arg = new HashMap<String, String>();
		arg.put("username", name);
		arg.put("password", password);
		arg.put(Rname, value);

		// 5.发起请求
		Connection con2 = Jsoup.connect(
				"http://class.sise.com.cn:7001/sise/login_check_login.jsp")
				.data(arg);
		Response re2 = con2.execute();

		return re2.cookies();
	}

	// 返回页面内容
	public static Document getContent(String name, String password,String url)
			throws IOException {
		Connection con = Jsoup.connect(
				url).cookies(
				GetAll.getCookies(name, password));
		return con.post();
	}

	// 返回Main.jsp页面
	public static Document getMain(String name, String password)
			throws IOException {
		Connection con = Jsoup
				.connect(
						"http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp")
				.cookies(GetAll.getCookies(name, password));
		return con.post();
	}

	public static Messages getMyMessage(String name, String password)
			throws IOException {
		Messages mes = new Messages();
		Document doc = GetAll.getMain(name, password);
		Element ele = doc.select("table").get(6).select("td").get(0);
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		Connection con = Jsoup.connect(link).cookies(
				GetAll.getCookies(name, password));
		Document mesDoc = con.post();
		Element list = mesDoc.select("table").get(2);
		String studentNumber = list.select("td div").get(1).text().trim();
		String sName = list.select("td div").get(2).text().trim();
		String grade = list.select("td div").get(3).text().trim();
		String major = list.select("td div").get(4).text().trim();
		String idNumber = list.select("td div").get(5).text().trim();
		String email = list.select("td div").get(6).text().trim();
		String administrativeClass = list.select("td.td_left").get(8).text()
				.trim().replace(" ", "");
		String chief = list.select("td div").get(7).text().trim();
		String instructor = list.select("td div").get(8).text().trim();
		String credits = mesDoc.select("table").get(10).select("tr").get(6)
				.select("font").get(1).text();
		mes.setStudentNumber(studentNumber);
		mes.setSName(sName);
		mes.setGrade(grade);
		mes.setMajor(major);
		mes.setIdNumber(idNumber);
		mes.setEmail(email);
		mes.setAdministrativeClass(administrativeClass);
		mes.setChief(chief);
		mes.setInstructor(instructor);
		mes.setCredits(credits);
		return mes;
	}

	public static String[][] getCourseDemo(String name, String password)
			throws IOException {
		String url = "http://class.sise.com.cn:7001/sise/module/student_schedular/student_schedular.jsp?schoolyear=2017&semester=1";
		Connection con = Jsoup.connect(url).cookies(
				GetAll.getCookies(name, password));
		Document doc = con.post();
		Element table = doc.select("table").get(6);
		String[][] course = new String[9][6];
		for (int i = 1; i < 9; i++) {
			Element row = table.select("tr").get(i);
			for (int j = 1; j < 6; j++) {
				Element column = row.select("td").get(j);
				String columnText = column.text();
				course[i][j] = columnText;
			}
		}
		return course;
	}

	public static HashMap getAttendance(String name, String password)
			throws IOException {
		Document doc = GetAll.getMain(name, password);
		Element ele = doc.select("table").get(9).select("td").get(0);
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		Connection con = Jsoup.connect(link).cookies(
				GetAll.getCookies(name, password));
		Document attDoc = con.post();
		Elements attTable = attDoc.select("table").get(6)
				.select("tr.odd,tr.even");
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < attTable.size(); i++) {
			Element attSingle = attTable.select("tr.odd,tr.even").get(i);
			String cName = attSingle.select("td").get(1).text();
			String cMes = attSingle.select("td").get(2).text();
			map.put(cName, cMes);
		}
		return map;
	}

	public static boolean isContains(String[] s, String keyweek) {
		for (String s1 : s) {
			if (s1.equals(keyweek)) {
				return true;
			}
		}
		return false;
	}

	// k:哪周的课
	public static String[][] getCourse(String name, String password, String k)
			throws IOException {

		String[][] courseDemo = GetAll.getCourseDemo(name, password);
		String[][] course = new String[9][6];
		int m = 1;
		for (int i = 1; i < courseDemo.length; i++) {
			for (int j = 1; j < courseDemo[i].length; j++) {
				if (!courseDemo[i][j].replace("\u00a0", "").equals("")) {
					if (!courseDemo[i][j].contains(",")) {
						String[] str = courseDemo[i][j].split("\\([a-zA-Z]{2,}\\d{0,}");
						String[] keyReplace = courseDemo[i][j].split(" ", 2)[1].split("周")[0].split(" ");
						if (isContains(keyReplace, k)
								&& !courseDemo[i][j].contains(",")) {
							course[i][j] = str[0];
						}
					}
					if (courseDemo[i][j].contains(",")) {
						String[] spCourse = courseDemo[i][j].split(",");

						if (isContains(
								spCourse[0].split(" ", 2)[1].split("周")[0].split(" "), k)) 
						{
							String[] strPart = spCourse[0].split("\\([a-zA-Z]{2,}\\d{0,}");
							course[i][j] = strPart[0];
						}
						if (isContains(
								spCourse[1].split(" ", 2)[1].split("周")[0].split(" "), k)) {
							String[] strPart = spCourse[1].split("\\([a-zA-Z]{2,}\\d{0,}");
							course[i][j] = strPart[0];
						}
					}
				}
			}
		}
		return course;
	}
	
	public static Course getUsualScore(String name,String password) throws IOException{
//		Document doc = getContent(name, password, "http://class.sise.com.cn:7001/sise/module/commonresult/index.jsp?schoolyear=2017&semester=1");
//		Elements attTable = doc.select("table").get(4).select("tbody").get(1).select("tr");
//		System.out.println(attTable.toString());
		int i = 0;
//		for(Element e:attTable){
//			if(i != 0){
////http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2800&schoolyear=2017&semester=1
//			    String id = e.select("td").get(0).select("a").first().attr("href").split("=")[1].split("&")[0];
////			    System.out.println(id);
//				
////			    String Coursename = e.select("td").get(0).select("a").first().text();
////			    System.out.println(Coursename);
//			    
////				String coursecode = e.select("td").get(0).select("span").first().text().split(" ")[0];
////				System.out.println(coursecode);
//				
//				String url = "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid="+id+"&schoolyear=2017&semester=1";
//				Document coursedetail = getContent(name, password, url);
//				
//			}
//
//			i++;
//		}
		
		Course c = new Course();
		ArrayList<CourseDetail> test = new ArrayList();
		String url = "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2800&schoolyear=2017&semester=1";
		Document coursedetail = getContent(name, password, url);
		Elements ele = coursedetail.select("table tbody tbody tr");
		for(Element e:ele){
			if(i!=0){
				String[] ss = e.text().split(" ");
				CourseDetail cd = new CourseDetail();
				cd.setSource(ss[0]);
				cd.setPercent(ss[1]);
				cd.setHighscore(ss[2]);
				cd.setScore(ss[3]);	
				test.add(cd);
				c.setCoursedetails(test);
				
			}
			i++;
		}
		return c;
	}

	public static void main(String[] args) throws IOException {
		String name = "1540129539";
		String password = "19960930";
		Course c = getUsualScore(name,password);
		System.out.println(c.getCoursedetails().get(0).getSource());

//		String s = "作业1 15% 15 13";
//		String[] ss = s.split(" ");
//		for(String s1:ss){
//			System.out.println(s1);
//		}
//		Document doc = getContent(name, password, "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2800&schoolyear=2017&semester=1");
//		System.out.println(doc);
//		String[][] course = getCourseDemo(name, password);
//		for(int i = 0; i < 9;i++){
//			for(int j = 0; j < 6;j++){
//				System.out.println(course[i][j]);
//			}
//		}
		
//		HashMap map = getAttendance(name, password);
//		for (Object key : map.keySet()) {
//		    System.out.println(key + " : " + map.get(key));
//		}
		
//		Document doc = getContent(name, password, "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2648&schoolyear=2017&semester=1");
//		System.out.println(doc.toString());
	}

}







