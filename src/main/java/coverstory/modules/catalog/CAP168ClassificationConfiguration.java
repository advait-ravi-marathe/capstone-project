package coverstory.modules.catalog;

import capstone.coverstory.shortlist.api.CAP168ClassificationAction;
import capstone.coverstory.shortlist.api.CAP168Seam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CAP168ClassificationConfiguration {
    @Bean CAP168Seam cap168LearnerSeam(){return new CAP168Seam();}
    @Bean CAP168ClassificationAction cap168LearnerClassifier(CAP168Seam learner){return learner::classifyQuoteReviewLines;}
}
