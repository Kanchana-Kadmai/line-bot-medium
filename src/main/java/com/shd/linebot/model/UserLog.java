package com.shd.linebot.model;

import java.security.Timestamp;

import lombok.Data;

@Data
public class UserLog {
	
	public enum status {DEFAULT, SAVE, Register, Comfrim, AcademicResults, TeachInstead, Leave};
	
	public status getStatusBot() {
		return statusBot;
	}
	
	public void setStatusBot(final status statusBot) {
		this.statusBot = statusBot;
	}

	public UserLog(final String userID, final status statusBot) {
		this.userID = userID;
		this.statusBot = statusBot;
	}
	
	public UserLog() {
	}

	private String userID;
	private Integer leaveID;
	private status statusBot;
	private String leaveType;
	private String detail;
	private Timestamp startDate;
	private Timestamp end_Date;
	private String empCode;
	private String period;
	
}
