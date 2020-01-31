package com.shd.linebot.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.shd.linebot.controller.LineBotController;
import com.shd.linebot.model.UserLineModel;
import com.shd.linebot.model.UserLog;
import com.shd.linebot.model.UserLog.status;
import com.shd.linebot.utils.BeanUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
@Transactional
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
	private LineBotController LineBotController;
	public UserLog userLog;

	public ArrayList<Map<String, Object>> searchName(UserLog userLog, String studentId) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> account_line = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> student_name = new ArrayList<Map<String, Object>>();
		try {
			System.out.println(dataSource.toString());
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();

			List<UserLineModel> userLines = new ArrayList<UserLineModel>();
			Connection connect = null;
			ResultSet rec = null;
			Statement st = null;
			try {
				Class.forName("org.postgresql.Driver");
				connect = DriverManager
						.getConnection("jdbc:postgresql://raja.db.elephantsql.com:5432/mbsqvzky?user=mbsqvzky&password=TR-Sgyxa6dcNFg4vM_o0dSzAOl_XpXdE&serverTimezone=UTC");
				st = connect.createStatement();
				String sql = null;
				sql = " SELECT line_id FROM db_student WHERE line_id='"+userLog.getUserID()+"'";
				rec = st.executeQuery(sql);
				while (rec.next()) {
					UserLineModel userLine = new UserLineModel();
					userLine.setLineId(rec.getString("line_id"));
					userLines.add(userLine);
				}
				System.out.println(userLines.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// sql1 = new StringBuilder();
			// sql1.append(" SELECT line_id ");
			// sql1.append(" FROM db_student  ");
			// sql1.append(" WHERE line_id = :lineId ");
			// System.out.println();
			// MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			// parameter1.addValue("lineId", userLog.getUserID());
			// account_line = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			// int size_line = account_line.size();
			if (userLines.size() > 0) {
				LineBotController.push(userLog.getUserID(),
						Arrays.asList(new TextMessage("คุณได้ลงทะเบียนไปแล้วเรียบร้อย กรุณาติดต่อผู้ดูแลระบบ ")));
				userLog.setStatusBot(status.DEFAULT);
			} else {
				sql2 = new StringBuilder();
				sql2.append(" SELECT st.student_title || st.student_name || ' ' || rm.room_number as student_name");
				sql2.append(" FROM db_student st ");
				sql2.append(" JOIN db_room rm ON rm.room_id = st.room_id  ");
				sql2.append(" WHERE st.student_id = :studentId ");

				MapSqlParameterSource parameter2 = new MapSqlParameterSource();
				parameter2.addValue("studentId", studentId);
				student_name = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql2.toString(), parameter2);

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
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
			userLog.setStatusBot(status.DEFAULT);
			LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage("Try again!! ")));
		}
		return result;
	}
}
