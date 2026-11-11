package com.xinchao.FoodApp;

import com.xinchao.FoodApp.email_notification.dtos.NotificationDTO;
import com.xinchao.FoodApp.email_notification.service.NotificationService;
import com.xinchao.FoodApp.enums.NotificationType;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@AllArgsConstructor
public class FoodAppApplication {
	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FoodAppApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(){
		return args -> {
			NotificationDTO notificationDTO = NotificationDTO.builder()
					.recipient("jennyt.htw@gmail.com")
					.subject("Hello Jenny")
					.body("Hey this is Trang")
					.type(NotificationType.EMAIL)
					.build();

			notificationService.sendEmail(notificationDTO);
		};
	}

}
