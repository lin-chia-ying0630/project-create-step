package tw.com.insurance.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreateBatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(CreateBatchApplication.class, args);
	}
}
