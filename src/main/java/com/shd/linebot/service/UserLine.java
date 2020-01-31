package com.shd.linebot.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.shd.linebot.model.UserLog;
import com.shd.linebot.model.UserLog.status;
import com.shd.linebot.utils.BeanUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
@Transactional
public class UserLine {

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

	public ArrayList<Map<String, Object>> searchLineId() {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			Connection connect = null;
			ResultSet rec = null;
			Statement st = null;
			try {
				Class.forName("org.postgresql.Driver");
				connect = DriverManager
						.getConnection("jdbc:postgresql://raja.db.elephantsql.com:5432/mbsqvzky?user=mbsqvzky&password=TR-Sgyxa6dcNFg4vM_o0dSzAOl_XpXdE&serverTimezone=UTC");
				st = connect.createStatement();
				String sql = null;
				sql = " SELECT line_id FROM db_student";
				rec = st.executeQuery(sql);
				while (rec.next()) {
						System.out.println(rec.getString("line_id"));
						return result;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		return result;
	}
}
