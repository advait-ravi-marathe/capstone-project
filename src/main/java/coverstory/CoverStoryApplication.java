package coverstory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Local API shell. Product workflows remain learner work. */
@SpringBootApplication(scanBasePackages = "coverstory")
public class CoverStoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoverStoryApplication.class, args);
    }
}
