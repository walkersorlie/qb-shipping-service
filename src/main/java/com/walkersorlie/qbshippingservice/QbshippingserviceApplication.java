package com.walkersorlie.qbshippingservice;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class QbshippingserviceApplication {

	public static void main(String[] args) {
//		SpringApplication.run(QbshippingserviceApplication.class, args);
//		SpringApplication application = new SpringApplication(ApplicationUI.class);
//		application.setHeadless(false);
//		application.run(args);
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ApplicationUI.class)
				.headless(false)
				.run(args);

		java.awt.EventQueue.invokeLater(() -> {
			ApplicationUI ex = ctx.getBean(ApplicationUI.class);
//			ex.setVisible(true);
		});

	}
}
