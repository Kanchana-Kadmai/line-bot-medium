package com.shd.linebot.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linecorp.bot.model.message.TextMessage;
import com.shd.linebot.controller.LineBotController;
import com.shd.linebot.dao.SlipPaymentDao;
import com.shd.linebot.model.UserLog;
import com.shd.linebot.model.UserLogPayment;
import com.shd.linebot.model.UserLogPayment.statusPayment;
import com.shd.linebot.model.UserLog.status;
import com.shd.linebot.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class LeaveService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SlipPaymentDao slipPaymentDao;

	@Autowired
	private LineBotController lineBotController;

	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;
	private StringBuilder stb1 = null;
	private Map<String, UserLogPayment> userMap = new HashMap<String, UserLogPayment>();

	public void leave(byte[] content, String userId, boolean flag) throws Exception {
		UserLogPayment userLogPayment = userMap.get(userId);
		if (flag) {
			if (userLogPayment == null) {
				userLogPayment = new UserLogPayment(userId, statusPayment.PAYMENT);
				userMap.put(userId, userLogPayment);
			} else {
				userLogPayment.setStatusBot(statusPayment.PAYMENT);
			}
		}
		if (userLogPayment != null && userLogPayment.getStatusBot().equals(statusPayment.PAYMENT) && flag == false) {
//			if (userLogPayment.getUserID() == userId) {
			slipPaymentDao.saveSlipPayment(content, userId);
			sendLine(userId);
			userLogPayment.setStatusBot(statusPayment.DEFAULT);
//			}
		} else if (userLogPayment == null && flag == false) {
			sendLineNoData(userId);
		} else if (userLogPayment.getStatusBot().equals(statusPayment.DEFAULT) && flag == false) {
			sendLineNoData(userId);
		}
	}

	public void sendLine(String userId) throws Exception {
		lineBotController.push(userId, Arrays.asList(new TextMessage(
				"ระบบได้รับใบลาของท่าน เรียบร้อยแล้ว กรุณารอการตอบกลับ จากทางเจ้าหน้าที่")));
	}

	public void sendLineNoData(String userId) throws Exception {
		lineBotController.push(userId, Arrays.asList(new TextMessage(
				"กรุณาทำรายการที่ เมนู แจ้งขอลา แล้วทำขึ้นตอนดังต่อไปนี้  \n- เลือกประเภทการลา \n- ส่งรูปสลิปการโอนเงิน ")));
	}

}