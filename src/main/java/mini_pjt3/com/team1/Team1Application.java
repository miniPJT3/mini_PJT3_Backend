package mini_pjt3.com.team1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Team1Application {

	public static void main(String[] args) {
		SpringApplication.run(Team1Application.class, args);
	}

}
