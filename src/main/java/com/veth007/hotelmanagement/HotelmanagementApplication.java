package com.veth007.hotelmanagement;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotelmanagementApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
		SpringApplication.run(HotelmanagementApplication.class, args);
	}
}
