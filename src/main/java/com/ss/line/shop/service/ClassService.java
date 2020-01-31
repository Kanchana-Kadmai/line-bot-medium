package com.ss.line.shop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import com.linecorp.bot.model.message.TextMessage;
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
	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	@Autowired
	private LineBotController LineBotController;
	public UserLog userLog;

	public ArrayList<Map<String, Object>> searchClass(UserLog userLog) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" SELECT rm.subject_table ");
			sql1.append(" FROM db_room rm  ");
			sql1.append(" JOIN db_student st ON (rm.room_id = st.room_id)  ");
			sql1.append(" WHERE st.line_id::VARCHAR = :lineId ");

			MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("lineId", userLog.getUserID());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			int size = result.size();
			if (size > 0) {
				String detail = "";
				detail+=(String)result.get(0).get("subject_table");
				LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage(detail)));
				userLog.setStatusBot(status.DEFAULT);

			} else {
				LineBotController.push(userLog.getUserID(),
						Arrays.asList(new TextMessage("ไม่มีการบันทึกตารางเรียน กรุณาติดต่อเจ้าหน้าที่ ")));
				userLog.setStatusBot(status.DEFAULT);
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

}
