package com.shd.linebot.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.shd.linebot.helper.RichMenuHelper;
import com.shd.linebot.model.Found;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(path = "/approve")

public class ApproveController {

	@Autowired
	private LineBotController LineBotController;

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@PostMapping(path = "/foundStudent")
	public void foundStudent(@RequestBody Found data) throws Throwable {
		try {

			Map<String, Object> cusResults = new HashMap<String, Object>();
			// cusResults = approveRepo.approveWaitDoc(data);

			NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
			mf.setMaximumFractionDigits(2);

			String text;
			text = data.getStudentName() + " " + data.getTeacherName() + " ";
			text += "เรียกพบ "+ data.getRemark();

			// LineBotController.push(data.getLine_user_id(), Arrays.asList(new TextMessage(text)));

		} catch (DataIntegrityViolationException e) {
			throw e;
		}
	}

}
