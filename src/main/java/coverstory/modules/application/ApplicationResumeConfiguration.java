package coverstory.modules.application;

import coverstory.modules.application.api.CAP54ResumeUseCase;
import coverstory.modules.application.api.CAP54Seam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class ApplicationResumeConfiguration {
    @Bean
    CAP54ResumeUseCase cap54ResumeUseCase(ObjectProvider<CAP54Seam> seamProvider) {
        CAP54Seam seam = seamProvider.getIfAvailable();
        return new CAP54ResumeUseCase() {
            @Override public CAP54Seam.Reply resume(String owner, UUID journey) {
                if (seam == null) throw new UnsupportedOperationException("CAP-54 application adapter is not installed");
                return seam.resume(new CAP54Seam.Principal(owner), new CAP54Seam.ResumeRequest(journey, null, null));
            }
            @Override public CAP54Seam.Reply list(String owner, int limit, String cursor) {
                if (seam == null) throw new UnsupportedOperationException("CAP-54 application adapter is not installed");
                return seam.listOwned(new CAP54Seam.Principal(owner), limit, cursor);
            }
        };
    }
}
