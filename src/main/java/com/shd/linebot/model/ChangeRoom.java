package com.shd.linebot.model;

import lombok.Data;

@Data
public class ChangeRoom {
    private String teacherName;
	private String date;
    private String startTime;
    private String endTime;
    private String remark;
}
