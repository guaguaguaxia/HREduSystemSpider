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
import com.sise.bean.Examination;
import com.sise.bean.StudentInfo;

public class HREduSystemSpider {

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

	// 返回链接页面HTML
	public static Document getContent(String name, String password, String url)
			throws IOException {
		Connection con = Jsoup.connect(url).cookies(
				HREduSystemSpider.getCookies(name, password));
		return con.post();
	}

	public static StudentInfo getMyMessage(String name, String password)
			throws IOException {
		//1.初始化一个学生信息的bean类
		StudentInfo mes = new StudentInfo();
		//2.获得main.jsp的页面内容
		Document doc = HREduSystemSpider.getContent(name, password, "http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp");
		//3.根据获得的main.jsp的内容，拼接出个人信息的访问页面
		Element ele = doc.select("table").get(6).select("td").get(0);
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		//4.访问个人信息页面
		Connection con = Jsoup.connect(link).cookies(
				HREduSystemSpider.getCookies(name, password));
		Document mesDoc = con.post();
		//5.用jsoup语法筛选出需要的信息，再一一放进bean对象里
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
				HREduSystemSpider.getCookies(name, password));
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
		Document doc = HREduSystemSpider.getContent(name, password, "http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp");
		Element ele = doc.select("table").get(9).select("td").get(0);
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		Connection con = Jsoup.connect(link).cookies(
				HREduSystemSpider.getCookies(name, password));
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

		String[][] courseDemo = HREduSystemSpider.getCourseDemo(name, password);
		String[][] course = new String[9][6];
		int m = 1;
		for (int i = 1; i < courseDemo.length; i++) {
			for (int j = 1; j < courseDemo[i].length; j++) {
				if (!courseDemo[i][j].replace("\u00a0", "").equals("")) {
					if (!courseDemo[i][j].contains(",")) {
						String[] str = courseDemo[i][j]
								.split("\\([a-zA-Z]{2,}\\d{0,}");
						String[] keyReplace = courseDemo[i][j].split(" ", 2)[1]
								.split("周")[0].split(" ");
						if (isContains(keyReplace, k)
								&& !courseDemo[i][j].contains(",")) {
							course[i][j] = str[0];
						}
					}
					if (courseDemo[i][j].contains(",")) {
						String[] spCourse = courseDemo[i][j].split(",");

						if (isContains(
								spCourse[0].split(" ", 2)[1].split("周")[0]
										.split(" "),
								k)) {
							String[] strPart = spCourse[0]
									.split("\\([a-zA-Z]{2,}\\d{0,}");
							course[i][j] = strPart[0];
						}
						if (isContains(
								spCourse[1].split(" ", 2)[1].split("周")[0]
										.split(" "),
								k)) {
							String[] strPart = spCourse[1]
									.split("\\([a-zA-Z]{2,}\\d{0,}");
							course[i][j] = strPart[0];
						}
					}
				}
			}
		}
		return course;
	}

	public static Course[] getUsualScore(String name, String password)
			throws IOException {
		Document doc = getContent(
				name,
				password,
				"http://class.sise.com.cn:7001/sise/module/commonresult/index.jsp?schoolyear=2017&semester=1");
		Elements attTable = doc.select("table").get(4).select("tbody").get(1)
				.select("tr");
		int i = 0;
		ArrayList<CourseDetail> list = new ArrayList();
		Course c = new Course();
		Course cc[] = new Course[attTable.size()];
		for (Element e : attTable) {

			if (i != 0) {
				String id = e.select("td").get(0).select("a").first()
						.attr("href").split("=")[1].split("&")[0];
				c.setId(id);
				String Coursename = e.select("td").get(0).select("a").first()
						.text();
				c.setName(Coursename);
				String coursecode = e.select("td").get(0).select("span")
						.first().text().split(" ")[0];
				c.setCoursecode(coursecode);

				String url = "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid="
						+ id + "&schoolyear=2017&semester=1";
				Document coursedetail = getContent(name, password, url);
				Elements ele = coursedetail.select("table tbody tbody tr");
				int j = 0;
				for (Element e1 : ele) {
					if (j != 0) {
						String[] ss = e1.text().split(" ");
						CourseDetail cd = new CourseDetail();
						cd.setSource(ss[0]);
						cd.setPercent(ss[1]);
						cd.setHighscore(ss[2]);
						cd.setScore(ss[3]);
						list.add(cd);
						c.setCoursedetails(list);

					}
					j++;
				}
				cc[i] = c;
			}

			i++;
		}

		return cc;
	}

	public static ArrayList getPart1(String name, String password)
			throws IOException {
		Document doc = getContent(
				name,
				password,
				"http://class.sise.com.cn:7001/sise/module/commonresult/index.jsp?schoolyear=2017&semester=1");
		Elements attTable = doc.select("table").get(4).select("tbody").get(1)
				.select("tr");

		ArrayList<Course> list = new ArrayList<Course>();
		int i = 0;
		for (Element e : attTable) {
			Course c = new Course();
			if (i != 0) {
				String id = e.select("td").get(0).select("a").first()
						.attr("href").split("=")[1].split("&")[0];
				c.setId(id);
				String Coursename = e.select("td").get(0).select("a").first()
						.text();
				c.setName(Coursename);
				String coursecode = e.select("td").get(0).select("span")
						.first().text().split(" ")[0];
				c.setCoursecode(coursecode);
				list.add(c);
			}
			i++;

		}
		return list;
	}

	public static ArrayList getPart2(String name, String password,int year,int semester)
			throws IOException {
		ArrayList<Course> list = getPart1(name, password);
		for (int i = 0; i < list.size(); i++) {
			
			String url = "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid="
					+ list.get(i).getId() + "&schoolyear="+year+"&semester="+semester;
			Document doc = getContent(name, password, url);
			Elements ele = doc.select("table tbody tbody tr");
			int j = 0;
			ArrayList<CourseDetail> listDetail = new ArrayList<CourseDetail>();
			for (Element e1 : ele) {
				if (j != 0) {
					String[] ss = e1.text().split(" ");
					CourseDetail cd = new CourseDetail();
					cd.setSource(ss[0]);
					cd.setPercent(ss[1]);
					cd.setHighscore(ss[2]);
					cd.setScore(ss[3]);
					list.get(i).setScore(ss[3]);
					listDetail.add(cd);
				      }
				list.get(i).setCoursedetails(listDetail);
				
				j++;
			}
			
			
		}
		return list;
	}

	public static String getStudentId(String name,String password) throws IOException{
		String url = "http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp";
		Document doc = getContent(name, password, url);
		String str = doc.select("td table tr td").get(4).attr("onclick").split("studentid=")[1].split("'")[0];
		return str;
		
	}
	public static ArrayList<Examination> getTest(String name,String password) throws IOException{
		String url = "http://class.sise.com.cn:7001/SISEWeb/pub/exam/studentexamAction.do?method=doMain&studentid="+getStudentId(name, password);
		Document doc = getContent(name, password, url);
		Elements ele = doc.select("table.table").select("tr.odd,tr.even");
		ArrayList<Examination> exams = new ArrayList<Examination>();
		for(Element e:ele){
			Examination exam = new Examination();
			exam.setCoursecode(e.select("td").get(0).text());
			exam.setCoursename(e.select("td").get(1).text());
			exam.setDate(e.select("td").get(2).text());
			exam.setTime(e.select("td").get(3).text());
			exam.setExamroom(e.select("td").get(4).text());
			exam.setExamname(e.select("td").get(5).text());
			exam.setExamseat(e.select("td").get(6).text());
			exam.setExamstatus(e.select("td").get(7).text());
			exams.add(exam);
		}
		
		return exams;
	}
	
	public static void main(String[] args) throws IOException {
		String name = "1540129539";
		String password = "19960930";
//		ArrayList<Examination> list = getTest(name, password);
//		for(int i = 0;i<list.size();i++){
//			System.out.println(list.get(i).getCoursename());
//		}
//		ArrayList<Course> list = getPart2(name, password,2017,1);
//		for(int i = 0;i<list.size();i++){
//			System.out.println(list.get(i).getName());
//			System.out.println(list.get(i).getCoursecode());
//			System.out.println(list.get(i).getName());
//			System.out.println(list.get(i).getScore());
//			System.out.println(list.get(i).getCoursedetails().get(0).getSource());
//			System.out.println(list.get(i).getCoursedetails().get(0).getPercent());
//			System.out.println(list.get(i).getCoursedetails().get(0).getHighscore());
//			System.out.println(list.get(i).getCoursedetails().get(0).getScore());
//		}
		// ArrayList<Course> list = getPart1(name, password);
		// for(int i = 0;i<list.size();i++){
		// }
		// Course c = getPart1(name, password);
		// System.out.println(c.getCoursedetails().size());
		// for(int i = 0;i<c.getCoursedetails().size();i++){
		// System.out.println(c.getCoursedetails().get(i).getSource());
		// System.out.println(c.getCoursedetails().get(i).getPercent());
		// System.out.println(c.getCoursedetails().get(i).getHighscore());
		// System.out.println(c.getCoursedetails().get(i).getScore());
		// }

		// Document doc = getContent(name, password,
		// "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2800&schoolyear=2017&semester=1");
		// System.out.println(doc);
		// String[][] course = getCourseDemo(name, password);
		// for(int i = 0; i < 9;i++){
		// for(int j = 0; j < 6;j++){
		// System.out.println(course[i][j]);
		// }
		// }

		// HashMap map = getAttendance(name, password);
		// for (Object key : map.keySet()) {
		// System.out.println(key + " : " + map.get(key));
		// }

		// Document doc = getContent(name, password,
		// "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid=2648&schoolyear=2017&semester=1");
		// System.out.println(doc.toString());
	}

}
