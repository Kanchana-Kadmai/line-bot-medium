package com.ss.line.shop.service;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ss.line.shop.model.FoundModel;
import com.ss.line.shop.model.HolidayModel;
import com.ss.line.shop.model.Line;
import com.ss.line.shop.model.RoomModel;

public interface LineService {

	public void switchMenu(Line data);

	public void foundStudent(FoundModel data) throws JsonParseException, JsonMappingException, IOException;

	public void holiday(HolidayModel data) throws JsonParseException, JsonMappingException, IOException;

	public void changeClassroom(RoomModel data) throws JsonParseException, JsonMappingException, IOException;
}
