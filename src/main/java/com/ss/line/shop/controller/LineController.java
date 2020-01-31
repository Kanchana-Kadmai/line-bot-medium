package com.ss.line.shop.controller;

import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.ss.line.shop.model.FoundModel;
import com.ss.line.shop.model.HolidayModel;
import com.ss.line.shop.model.RoomModel;
import com.ss.line.shop.service.LineService;
import com.ss.line.shop.service.MyAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@ComponentScan
@LineMessageHandler
@CrossOrigin
@Slf4j
@RestController
@RequestMapping(path = "/line")

public class LineController {

	@Autowired
	private LineService lineService;

	@Autowired
	private MyAccountService myAccountService;

	@PostMapping(path = "/foundStudent")
	public void foundStudent(@RequestBody FoundModel data) throws Exception {
		System.out.println(data);
		lineService.foundStudent(data);
	}

	@PostMapping(path = "/holiday")
	public void holiday(@RequestBody HolidayModel data) throws Exception {
		System.out.println(data);
		lineService.holiday(data);
	}

	@PostMapping(path = "/changeClassroom")
	public void changeClassroom(@RequestBody RoomModel data) throws Exception {
		System.out.println(data);
		lineService.changeClassroom(data);
	}

	@GetMapping(path = "/test")
	public void changeClassroom() throws Exception {
		myAccountService.test();
	}
}
