package com.ss.line.shop.service;

import java.sql.SQLException;
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
public class MyAccountService {

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
	private LineBotController lineBotController;

	public UserLog userLog;

	public void test() throws SQLException {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql1 = new StringBuilder();
		final MapSqlParameterSource parameter1 = new MapSqlParameterSource();
		sql1 = new StringBuilder();
		sql1.append(" SELECT * ");
		sql1.append(" FROM db_student  ");
		jdbcTemplate.queryForList(sql1.toString(), parameter1);
	}
//softtradedb.\"TrainSQL\".
	public ArrayList<Map<String, Object>> searchName(final UserLog userLog, final String studentId) {
		final ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> account_line = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> student_name = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" SELECT line_id ");
			sql1.append(" FROM db_student  ");
			sql1.append(" WHERE line_id::VARCHAR = :lineId ");

			final MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("lineId", userLog.getUserID());
			account_line = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			final int size_line = account_line.size();
			if (size_line > 0) {
				lineBotController.push(userLog.getUserID(),
						Arrays.asList(new TextMessage("คุณได้ลงทะเบียนไปแล้วเรียบร้อย กรุณาติดต่อผู้ดูแลระบบ ")));
				userLog.setStatusBot(status.DEFAULT);
			} else {
				sql2 = new StringBuilder();
				sql2.append(" SELECT student_name ");
				sql2.append(" FROM db_student  ");
				sql2.append(" WHERE student_id::VARCHAR = :studentId ");

				final MapSqlParameterSource parameter2 = new MapSqlParameterSource();
				parameter2.addValue("studentId", studentId);
				student_name = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql2.toString(), parameter2);

				final int size = student_name.size();
				if (size > 0) {
					final String detail = "ชื่อ " + student_name.get(0).get("student_name") + " ใช่หรือไม่";
					final ConfirmTemplate confirmTemplate = new ConfirmTemplate(detail, new MessageAction("ใช่", "ใช่"),
							new MessageAction("ไม่ใช่", "ไม่ใช่"));
					final TemplateMessage templateMessage = new TemplateMessage("ยืนยัน", confirmTemplate);

					lineBotController.push(userLog.getUserID(), Arrays.asList(templateMessage));
					userLog.setStatusBot(status.Comfrim);
					userLog.setStudentId(studentId);

				} else {
					lineBotController.push(userLog.getUserID(),
							Arrays.asList(new TextMessage("ไม่มีรหัสนี้ในระบบ\n กรุณากดลงทะเบียนอีกครั้ง ")));
					userLog.setStatusBot(status.DEFAULT);
				}
			}

		} catch (final EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ArrayList<Map<String, Object>> updateLineSutudent(final UserLog userLog) {
		final ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> account_line = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" UPDATE db_student ");
			sql1.append(" SET line_id=:lineId, active=true  ");
			sql1.append(" WHERE student_id=:studentId ");

			final MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("lineId", userLog.getUserID());
			parameter1.addValue("studentId", userLog.getStudentId());
			System.out.println("-----------------lineId-------------"+userLog.getUserID());
			System.out.println("-----------------studentId-------------"+userLog.getStudentId());
			account_line = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			// final int size_line = account_line.size();
			// if (size_line > 0) {
			// 	lineBotController.push(userLog.getUserID(),
			// 			Arrays.asList(new TextMessage("คุณได้ลงทะเบียนไปแล้วเรียบร้อย กรุณาติดต่อผู้ดูแลระบบ ")));
			// 	userLog.setStatusBot(status.DEFAULT);
			// } else {
			// 	sql2 = new StringBuilder();
			// 	sql2.append(" SELECT student_name ");
			// 	sql2.append(" FROM TrainSQL.db_student  ");
			// 	sql2.append(" WHERE student_id::VARCHAR = :studentId ");

			// 	final MapSqlParameterSource parameter2 = new MapSqlParameterSource();
			// 	parameter2.addValue("studentId", studentId);
			// 	student_name = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql2.toString(), parameter2);

			// 	final int size = student_name.size();
			// 	if (size > 0) {
			// 		final String detail = "ชื่อ " + student_name.get(0).get("student_name") + " ใช่หรือไม่";
			// 		final ConfirmTemplate confirmTemplate = new ConfirmTemplate(detail, new MessageAction("ใช่", "ใช่"),
			// 				new MessageAction("ไม่ใช่", "ไม่ใช่"));
			// 		final TemplateMessage templateMessage = new TemplateMessage("ยืนยัน", confirmTemplate);

			// 		lineBotController.push(userLog.getUserID(), Arrays.asList(templateMessage));
			// 		userLog.setStatusBot(status.Comfrim);

			// 	} else {
			// 		lineBotController.push(userLog.getUserID(),
			// 				Arrays.asList(new TextMessage("ไม่มีรหัสนี้ในระบบ\n กรุณากดลงทะเบียนอีกครั้ง ")));
			// 		userLog.setStatusBot(status.DEFAULT);
			// 	}
			// }

		} catch (final EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}
