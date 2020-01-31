package com.ss.line.shop.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import com.ss.line.shop.controller.LineBotController;
import com.ss.line.shop.model.FoundModel;
import com.ss.line.shop.model.HolidayModel;
import com.ss.line.shop.model.Line;
import com.ss.line.shop.model.RoomModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LineServiceImp implements LineService {

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineBotController lineBotController;

	@Autowired
	private MyAccountService myAccountService;

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	@Override
	public void switchMenu(Line data) {

		if (data.getRole().equals("back")) {
			RichMenuHelper.deleteRichMenu(lineMessagingClient, data.getLineId());
		} else {
			String pathYamlHome = "asset/richmenu-shop.yml";
			String pathImageHome = "asset/shop-menu.jpg";
			RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, data.getLineId());
		}
	}

	public ArrayList<Map<String, Object>> searchLine() {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql = new StringBuilder();
		sql = new StringBuilder();
		sql.append(" SELECT line_id ");
		sql.append(" FROM db_student WHERE line_id::VARCHAR is not null ");

		final MapSqlParameterSource parameter = new MapSqlParameterSource();
		result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), parameter);

		return result;
	}

	@Override
	public void foundStudent(FoundModel data) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Map<String, Object>> result = searchLine();
		int i;
		int size = result.size();
		String detail = "";
		for (i = 0; i < size; i++) {
			result.get(i).get("line_id");
			detail += (String) "คุณครู"+data.getTeacherId()+" เรียกพบ "+data.getStudentId()+"\n"
			       + data.getRemark();
			lineBotController.push(result.get(i).get("line_id").toString(), Arrays.asList(new TextMessage(detail)));
		}
	}

	@Override
	public void holiday(HolidayModel data) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Map<String, Object>> result = searchLine();
		int i;
		int size = result.size();
		String detail = "";
		for (i = 0; i < size; i++) {
			result.get(i).get("line_id");
			detail += (String) "หยุดตั้งแต่วันที่ "+data.getStartDate()+" ถึง วันที่ "+data.getEndDate()+"\n"
			       + data.getRemark();
			lineBotController.push(result.get(i).get("line_id").toString(), Arrays.asList(new TextMessage(detail)));
		}
	}

	@Override
	public void changeClassroom(RoomModel data) throws JsonParseException, JsonMappingException, IOException {
		System.out.println("------data-----"+data);
		ArrayList<Map<String, Object>> result = searchLine();
		int i;
		int size = result.size();
		String detail = "";
		for (i = 0; i < size; i++) {
			result.get(i).get("line_id");
			detail += (String) "คุณครู "+data.getTeacherId()+" ย้ายห้องจาก "+ data.getFirstRoom()+" ไปที่ "+ data.getSecondRoom()
			       + "วันที่ "+ data.getRoomDate()+"\n"+ data.getRemark();
			lineBotController.push(result.get(i).get("line_id").toString(), Arrays.asList(new TextMessage(detail)));
		}
	}
}
