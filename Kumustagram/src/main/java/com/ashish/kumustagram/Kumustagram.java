package com.ashish.kumustagram;

import com.ashish.kumustagram.util.CloudinaryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CloudinaryConfig.class)
public class Kumustagram {

	public static void main(String[] args) {
		SpringApplication.run(Kumustagram.class, args);
	}

}
