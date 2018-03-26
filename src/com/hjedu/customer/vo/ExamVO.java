 package com.hjedu.customer.vo;

import java.util.Date;

public class ExamVO {
	private String id;
	private String name;
	private String courseName;
	private int classNum;
	private int ord;
	private Date availableBegain;
	private Date availableEnd;
	
	public ExamVO(String id,String name,String courseName,int classNum,int ord,Date availableBegain,Date availableEnd) {
		this.id = id;
		this.name = name;
		this.courseName = courseName;
		this.classNum = classNum;
		this.ord = ord;
		this.availableBegain = availableBegain;
		this.availableEnd = availableEnd;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getClassNum() {
		return classNum;
	}

	public void setClassNum(int classNum) {
		this.classNum = classNum;
	}

	public int getOrd() {
		return ord;
	}

	public void setOrd(int ord) {
		this.ord = ord;
	}

	public Date getAvailableBegain() {
		return availableBegain;
	}

	public void setAvailableBegain(Date availableBegain) {
		this.availableBegain = availableBegain;
	}

	public Date getAvailableEnd() {
		return availableEnd;
	}

	public void setAvailableEnd(Date availableEnd) {
		this.availableEnd = availableEnd;
	}
	
	
}
