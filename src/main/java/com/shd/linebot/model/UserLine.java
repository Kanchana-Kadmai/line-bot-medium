package com.shd.linebot.model;

import java.security.Timestamp;

import lombok.Data;

@Data
public class UserLine {
	
	private String lineId;
	private String studentName;
	private String studentId;
	private String teacherName;
	private String teacherRe;
	private String busyStatus;
	private String remark;
	
	private String startLeave;
	private String endLeave;
	private String gdpLink;
	private String startDate;
}
