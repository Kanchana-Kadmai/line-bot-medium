package com.ss.line.shop.model;

import java.util.List;

import lombok.Data;

@Data
public class Line {

	private String lineId;
	private String role;
	private String message;
	private List<LineMessage> messageList;
}
