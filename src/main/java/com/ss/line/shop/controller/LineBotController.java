package com.ss.line.shop.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.ss.line.shop.model.UserLog;
import com.ss.line.shop.model.UserLog.status;
import com.ss.line.shop.service.BusyTeacherService;
import com.ss.line.shop.service.ClassService;
import com.ss.line.shop.service.GpaService;
// import com.ss.line.shop.service.GpaService;
// import com.ss.line.shop.service.LeaveService;
import com.ss.line.shop.service.MyAccountService;
import com.ss.line.shop.service.RichMenuHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan
@LineMessageHandler
@CrossOrigin
@RestController
public class LineBotController {
	@Autowired
	private LineMessagingClient lineMessagingClient;


	@Autowired
	private MyAccountService myAccountService;

	@Autowired
	private BusyTeacherService busyTeacherService;

	@Autowired
	private GpaService gpaService;

	@Autowired
	private ClassService classService;

	private Map<String, UserLog> userMap = new HashMap<String, UserLog>();

	@EventMapping
	public void handleTextMessage(MessageEvent<TextMessageContent> event) throws IOException {
		log.info(event.toString());
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handlePostbackEvent(PostbackEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got postback data " + event.getPostbackContent().getData() + ", param "
				+ event.getPostbackContent().getParams().toString());
	}

	@EventMapping
	public void handleOtherEvent(Event event) {
		log.info("Received message(Ignored): {}", event);
	}

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());
		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}

		String text = content.getText();
		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			switch (text) {
			case "ลงทะเบียน": {
				this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาระบุรหัสนักเรียน ")));
				userLog.setStatusBot(status.Register);
				break;
			}
			case "flex": {
				String pathYamlHome = "asset/richmenu-pico.yml";
				String pathImageHome = "asset/pico-menu.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome,
						event.getSource().getSenderId());
				break;
			}
			case "Flex Back": {
				RichMenuHelper.deleteRichMenu(lineMessagingClient, event.getSource().getSenderId());
				break;
			}
			case "ดูตารางเรียน": {
				classService.searchClass(userLog);
				break;
			}
			case "ดูผลการเรียน": {
				gpaService.searchGpa(userLog);
				break;
			}
			case "คุณครูลา": {
				busyTeacherService.searchBusy(userLog);
				break;
			}
			case "New": {
				RichMenuHelper.deleteRichMenu(lineMessagingClient, userLog.getUserID());

				String pathYamlHome = "asset/richmenu-register.yml";
				String pathImageHome = "asset/richmenu-register.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				break;
			}
			case "แจ้งขอลา": {
				ConfirmTemplate confirmTemplate = new ConfirmTemplate("เลือก", new MessageAction("ลาป่วย", "ลาป่วย"),
						new MessageAction("ลากิจ", "ลากิจ"));
				TemplateMessage templateMessage = new TemplateMessage("เลือกลา", confirmTemplate);
				this.reply(replyToken, templateMessage);
				break;
			}
			case "ลาป่วย": {
				try {
					// leaveService.leave(null, event.getSource().getUserId(), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาส่งรูปใบลาป่วย")));

				break;
			}
			case "ลากิจ": {
				try {
					// leaveService.leave(null, event.getSource().getUserId(), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาส่งรูปใบลากิจ")));

				break;
			}
			default:
				this.push(userLog.getUserID(), Arrays.asList(new TextMessage("สวัสดี ตอนนี้อยู่ระหว่างพัฒนา")));
			}
		} else if (userLog.getStatusBot().equals(status.Register)) {
			myAccountService.searchName(userLog, text);
		} else if (userLog.getStatusBot().equals(status.Comfrim)) {
			switch (text) {
			case "ใช่": {
				myAccountService.updateLineSutudent(userLog, userLog.getStudentId());
				this.reply(replyToken, Arrays.asList(new TextMessage("ลงทะเบียนสำเร็จ ")));
				userLog.setStatusBot(status.DEFAULT);

				RichMenuHelper.deleteRichMenu(lineMessagingClient, userLog.getUserID());

				String pathYamlHome = "asset/richmenu-home.yml";
				String pathImageHome = "asset/richmenu-home.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());

				break;
			}
			case "ไม่ใช่": {
				this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาพิมพ์ รหัสนักเรียน ใหม่อีกครั้ง ")));
				userLog.setStatusBot(status.Register);
				break;
			}
			case "Re": {
				this.replyText(replyToken, text);
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			default:
				this.push(userLog.getUserID(), Arrays.asList(new TextMessage("ไม่เข้าใจคำสั่ง")));
			}
		} else {
			this.push(event.getSource().getSenderId(), Arrays.asList(new TextMessage("บอทหลับอยู่")));
			// this.reply(replyToken, new StickerMessage("1", "17"));
		}

	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken is not empty");
		}

		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "...";
		}
		this.reply(replyToken, new TextMessage(message));
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	public void push(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			lineMessagingClient.pushMessage(new PushMessage(replyToken, messages)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

}
