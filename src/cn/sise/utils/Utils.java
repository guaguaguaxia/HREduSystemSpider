package cn.sise.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sise.bean.Course;
import com.sise.spider.HREduSystemSpider;

public class Utils {
	public static boolean isContains(String[] s, String keyweek) {
		for (String s1 : s) {
			if (s1.equals(keyweek)) {
				return true;
			}
		}
		return false;
	}
	// 返回链接页面HTML
	public static Document getContent(String name, String password, String url)
			throws IOException {
		Connection con = Jsoup.connect(url).cookies(
				HREduSystemSpider.getCookies(name, password));
		return con.post();
	}
	public static String getStudentId(String name, String password)
			throws IOException {
		String url = "http://class.sise.com.cn:7001/sise/module/student_states/student_select_class/main.jsp";
		Document doc = getContent(name, password, url);
		String str = doc.select("td table tr td").get(4).attr("onclick")
				.split("studentid=")[1].split("'")[0];
		return str;

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
}
