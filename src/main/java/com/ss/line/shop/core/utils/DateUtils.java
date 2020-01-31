package com.ss.line.shop.core.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtils {
	
	public static String format(Date date, String pattern) {
		DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
		return dateFormat.format(date);
	}

	public static Date parse(String source, String pattern) {
		DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
		try {
			return dateFormat.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String format(Date date, String pattern, Locale locale){
		DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
		return dateFormat.format(date);
	}
	
	public static String convertStringFormat(String dateString){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = new Date();
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return formatter.format(date);
	}
	
	public static Date convertStringToDateFormat(String dateString){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = new Date();
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return date;
	}
	
}
