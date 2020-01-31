package com.ss.line.shop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import com.linecorp.bot.model.message.TextMessage;
// import com.shd.linebot.controller.LineBotController;
// import com.shd.linebot.model.UserLog;
import com.ss.line.shop.controller.LineBotController;
import com.ss.line.shop.model.UserLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class BusyTeacherService {

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

	public ArrayList<Map<String, Object>> searchBusy(UserLog userLog) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" SELECT t.teacher_name ");
			sql1.append("      , bt.remark, bt.start_leave, bt.end_leave ");
			sql1.append(" FROM public.db_teacher t ");
			sql1.append(" JOIN su_busy_teacher bt ON bt.teacher_id = t.teacher_id ");
			sql1.append(" WHERE ':dateNow'::DATE BETWEEN start_leave::DATE AND end_leave::DATE ");

			MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("dateNow", java.time.LocalDate.now());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			int x;
			int size = result.size();
			if (size > 0) {
				String detail = "";
				for (x = 0; x < size; x++) {
					detail += "คุณครู " + (String) result.get(x).get("teacher_name") + " \n"
							+ result.get(x).get("remark") + "\n" + "ตั้งแต่วันที่ " + result.get(x).get("start_leave")
							+ " ถึงวันที่" + result.get(x).get("end_leave") + " \n";
				}

				LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage(detail)));

			} else {
				LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage("วันนี้ไม่มีอาจารย์ลา ")));
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}
