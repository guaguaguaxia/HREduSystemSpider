package com.sise.bean;

import java.util.ArrayList;

public class Course {
	private String teachername;
	private String name;
	private String score;
	private String id;
	private String coursecode;
	private ArrayList<CourseDetail> coursedetails;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCoursecode() {
		return coursecode;
	}
	public void setCoursecode(String coursecode) {
		this.coursecode = coursecode;
	}
	public ArrayList<CourseDetail> getCoursedetails() {
		return coursedetails;
	}
	public void setCoursedetails(ArrayList<CourseDetail> coursedetails) {
		this.coursedetails = coursedetails;
	}
	public String getTeachername() {
		return teachername;
	}
	public void setTeachername(String teachername) {
		this.teachername = teachername;
	}
	
	
}
