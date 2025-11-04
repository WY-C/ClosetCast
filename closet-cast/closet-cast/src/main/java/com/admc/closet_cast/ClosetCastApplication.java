package com.admc.closet_cast;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;


@SpringBootApplication(exclude = {
        RestClientAutoConfiguration.class,
        HttpClientAutoConfiguration.class
})
@EnableJpaAuditing
public class ClosetCastApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClosetCastApplication.class, args);
	}

}
