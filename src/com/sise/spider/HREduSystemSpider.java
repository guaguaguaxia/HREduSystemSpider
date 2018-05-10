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

import cn.sise.utils.Utils;

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

	public StudentInfo getMyMessage(String name, String password)
			throws IOException {
		// 1.初始化一个学生信息的bean类
		StudentInfo mes = new StudentInfo();
		// 2.获得main.jsp的页面内容
		Document doc = HREduSystemSpider
				.getContent(
						name,
						password,
						"http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp");
		// 3.根据获得的main.jsp的内容，拼接出个人信息的访问页面
		Element ele = doc.select("table").get(6).select("td").get(0);
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		// 4.访问个人信息页面
		Connection con = Jsoup.connect(link).cookies(
				HREduSystemSpider.getCookies(name, password));
		Document mesDoc = con.post();
		// 5.用jsoup语法筛选出需要的信息，再一一放进bean对象里
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

	public String[][] getCourseDemo(String name, String password,int year,int semester)
			throws IOException {
		String url = "http://class.sise.com.cn:7001/sise/module/student_schedular/student_schedular.jsp?schoolyear="+year+"&semester="+semester;
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
	
	//返回考勤记录
	public HashMap getAttendance(String name, String password)
			throws IOException {
		//获取网页Document
		Document doc = HREduSystemSpider
				.getContent(
						name,
						password,
						"http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp");
		//选择指定网页元素
		Element ele = doc.select("table").get(9).select("td").get(0);
		//获取到指定链接
		String link = "http://class.sise.com.cn:7001"
				+ ele.attr("onclick").split("'")[1].replace("../../../../..",
						"");
		Connection con = Jsoup.connect(link).cookies(
				HREduSystemSpider.getCookies(name, password));
		Document attDoc = con.post();
		Elements attTable = attDoc.select("table").get(6)
				.select("tr.odd,tr.even");
		//用Map存储考勤记录
		HashMap<String, String> map = new HashMap<String, String>();
		//循环放入
		for (int i = 0; i < attTable.size(); i++) {
			Element attSingle = attTable.select("tr.odd,tr.even").get(i);
			String cName = attSingle.select("td").get(1).text();
			String cMes = attSingle.select("td").get(2).text();
			map.put(cName, cMes);
		}
		return map;
	}

	
	
	
	//返回课程名称的字符数组，第一个维度数字为第几节课，第二个维度为星期几
	public String[][] getCourse(String name, String password, int year,int semester,String k)
			throws IOException {
		HREduSystemSpider hs = new HREduSystemSpider();
		String[][] courseDemo = hs.getCourseDemo(name, password,year,semester);
		String[][] course = new String[9][6];
		int m = 1;
		//一二层的for循环为循环二维数组，
		for (int i = 1; i < courseDemo.length; i++) {
			for (int j = 1; j < courseDemo[i].length; j++) {
				/*\u00a0为网页空格$nbsp;的字符表示，因为在网页中没课不是用null表示，而是还有
				 * 一个$nbsp;,所以需要过滤掉，空就不用进入循环了*/
				if (!courseDemo[i][j].replace("\u00a0", "").equals("")) {
					//如果在一个学期里，一个时间段有两门课，即前八周后八周，
					//系统会用逗号把他们分割，所以这里需要判断一下
					if (!courseDemo[i][j].contains(",")) {
						//用正则表达式匹配切割出课程名称
						String[] str = courseDemo[i][j]
								.split("\\([a-zA-Z]{2,}\\d{0,}");
						String[] keyReplace = courseDemo[i][j].split(" ", 2)[1]
								.split("周")[0].split(" ");
						//这段逻辑我忘了，不懂的打断点慢慢看返回值自己领悟
						if (Utils.isContains(keyReplace, k)
								&& !courseDemo[i][j].contains(",")) {
							course[i][j] = str[0];
						}
					}
					//同上
					if (courseDemo[i][j].contains(",")) {
						String[] spCourse = courseDemo[i][j].split(",");

						if (Utils.isContains(
								spCourse[0].split(" ", 2)[1].split("周")[0]
										.split(" "),
								k)) {
							String[] strPart = spCourse[0]
									.split("\\([a-zA-Z]{2,}\\d{0,}");
							course[i][j] = strPart[0];
						}
						if (Utils.isContains(
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
	
	public ArrayList getUsualScore(String name, String password, int year,
			int semester) throws IOException {
		ArrayList<Course> list = Utils.getPart1(name, password);
		for (int i = 0; i < list.size(); i++) {

			String url = "http://class.sise.com.cn:7001/sise/module/commonresult/showdetails.jsp?courseid="
					+ list.get(i).getId()
					+ "&schoolyear="
					+ year
					+ "&semester=" + semester;
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

	public ArrayList<Examination> getExams(String name, String password)
			throws IOException {
		String url = "http://class.sise.com.cn:7001/SISEWeb/pub/exam/studentexamAction.do?method=doMain&studentid="
				+ Utils.getStudentId(name, password);
		Document doc = getContent(name, password, url);
		Elements ele = doc.select("table.table").select("tr.odd,tr.even");
		ArrayList<Examination> exams = new ArrayList<Examination>();
		for (Element e : ele) {
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
	}

}
