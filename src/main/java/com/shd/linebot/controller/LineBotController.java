package com.shd.linebot.controller;

import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.shd.linebot.Application;
import com.shd.linebot.helper.RichMenuHelper;
import com.shd.linebot.model.UserLog;
import com.shd.linebot.model.UserLog.status;
import com.shd.linebot.service.BusyTeacherService;
import com.shd.linebot.service.ClassService;
import com.shd.linebot.service.GpaService;
import com.shd.linebot.service.LeaveService;
import com.shd.linebot.service.MyAccountService;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@LineMessageHandler
public class LineBotController {
    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private LeaveService leaveService;

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
    public void handleTextMessage(MessageEvent<TextMessageContent> event) {
        log.info(event.toString());
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessage(MessageEvent<StickerMessageContent> event) {
        log.info(event.toString());
        StickerMessageContent message = event.getMessage();
        reply(event.getReplyToken(), new StickerMessage(message.getPackageId(), message.getStickerId()));
    }

    @EventMapping
    public void handleLocationMessage(MessageEvent<LocationMessageContent> event) {
        log.info(event.toString());
        LocationMessageContent message = event.getMessage();
        reply(event.getReplyToken(),
                new LocationMessage((message.getTitle() == null) ? "Location replied" : message.getTitle(),
                        message.getAddress(), message.getLatitude(), message.getLongitude()));
    }

    @EventMapping
    public void handleImageMessage(MessageEvent<ImageMessageContent> event) throws Exception {
        ImageMessageContent content = event.getMessage();
        MessageContentResponse response = lineMessagingClient.getMessageContent(content.getId()).get();
        byte[] contentInBytes = IOUtils.toByteArray(response.getStream());
        leaveService.leave(contentInBytes, event.getSource().getUserId(), false);

    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) {
        UserLog userLog = userMap.get(event.getSource().getSenderId());
        log.info("Return echo message %s : %s", replyToken, userLog);
        if (userLog == null) {
            userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
            userMap.put(event.getSource().getSenderId(), userLog);
        }

        log.info("Return status bot : %s", userLog.getStatusBot());
        String text = content.getText();

        if (userLog.getStatusBot().equals(status.DEFAULT)) {
            switch (text) {
            case "Profile": {
                String userId = event.getSource().getUserId();
                if (userId != null) {
                    lineMessagingClient.getProfile(userId).whenComplete((profile, throwable) -> {
                        if (throwable != null) {
                            this.replyText(replyToken, throwable.getMessage());
                            return;
                        }
                        this.reply(replyToken,
                                Arrays.asList(new TextMessage("Display name: " + profile.getDisplayName()),
                                        new TextMessage("Status message: " + profile.getStatusMessage()),
                                        new TextMessage("User ID: " + profile.getUserId())));
                    });
                }
                break;
            }
            case "ลงทะเบียน": {
                this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาระบุรหัสนักเรียน ")));
                userLog.setStatusBot(status.Register);
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
                    leaveService.leave(null, event.getSource().getUserId(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.reply(replyToken, Arrays.asList(new TextMessage("กรุณาส่งรูปใบลาป่วย")));

                break;
            }
            case "ลากิจ": {
                try {
                    leaveService.leave(null, event.getSource().getUserId(), true);
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
            this.reply(replyToken, new StickerMessage("1", "17"));
        }
        userMap.put(event.getSource().getSenderId(), userLog);
    }

    private void handleStickerContent(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(content.getPackageId(), content.getStickerId()));
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
            BotApiResponse response = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} => {}", Arrays.toString(args), i);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent saveContent(String ext, MessageContentResponse response) {
        log.info("Content-type: {}", response);
        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(response.getStream(), outputStream);
            log.info("Save {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now() + "-" + UUID.randomUUID().toString() + "." + ext;
        Path tempFile = Application.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));

    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
    }

    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
}
