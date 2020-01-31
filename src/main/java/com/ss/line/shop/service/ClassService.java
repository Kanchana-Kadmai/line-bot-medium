package com.ss.line.shop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.ss.line.shop.controller.LineBotController;
import com.ss.line.shop.model.UserLog;
import com.ss.line.shop.model.UserLog.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class ClassService {

	public class Model {

		public String profileCode;
		public String profileDesc;
		public Boolean active;
		// private String createdProgram;
		// private String updatedProgram;

	}

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	@Autowired
	private LineBotController LineBotController;
	public UserLog userLog;

	public ArrayList<Map<String, Object>> searchClass(UserLog userLog) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> account_line = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> student_name = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" SELECT rm.subject_table ");
			sql1.append(" FROM db_room rm  ");
			sql1.append(" JOIN db_student st ON (rm.room_id = st.room_id)  ");
			sql1.append(" WHERE st.line_id::CHARACTER = :lineId ");

			MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("lineId", userLog.getUserID());
			account_line = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			int size = student_name.size();
			if (size > 0) {
				String detail = "ชื่อ " + student_name.get(0).get("student_name") + " ใช่หรือไม่";
				ConfirmTemplate confirmTemplate = new ConfirmTemplate(detail, new MessageAction("ใช่", "ใช่"),
						new MessageAction("ไม่ใช่", "ไม่ใช่"));
				TemplateMessage templateMessage = new TemplateMessage("ยืนยัน", confirmTemplate);

				LineBotController.push(userLog.getUserID(), Arrays.asList(templateMessage));
				userLog.setStatusBot(status.Comfrim);

			} else {
				LineBotController.push(userLog.getUserID(),
						Arrays.asList(new TextMessage("ไม่มีรหัสนี้ในระบบ\n กรุณากดลงทะเบียนอีกครั้ง ")));
				userLog.setStatusBot(status.DEFAULT);
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

}
