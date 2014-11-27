package com.dcollioni.graduare.entities;

import java.util.Date;

public class ExamResult {
	private int id;
	private double value;
	private String description;

	private int examId;
	private String examName;
	private String examDescription;
	private double examValue;
	private Date examDate;
	private String examDateStr;

	private int studentId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getExamId() {
		return examId;
	}

	public void setExamId(int examId) {
		this.examId = examId;
	}

	public String getExamName() {
		return examName;
	}

	public void setExamName(String examName) {
		this.examName = examName;
	}

	public String getExamDescription() {
		return examDescription;
	}

	public void setExamDescription(String examDescription) {
		this.examDescription = examDescription;
	}

	public double getExamValue() {
		return examValue;
	}

	public void setExamValue(double examValue) {
		this.examValue = examValue;
	}

	public Date getExamDate() {
		return examDate;
	}

	public void setExamDate(Date examDate) {
		this.examDate = examDate;
	}

	public String getExamDateStr() {
		return examDateStr;
	}

	public void setExamDateStr(String examDateStr) {
		this.examDateStr = examDateStr;
	}

	public int getStudentId() {
		return studentId;
	}

	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}
	
	@Override
	public String toString() {
		return examName;
	}
}