package br.com.sysmap.infrastructure.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by ecellani on 04/06/17.
 */
@Data
@ToString
@Component
@ConfigurationProperties("application")
public class ApplicationConfig {

    private Path path = new Path();
    private Queues queues = new Queues();
    private ThirdParty thirdParty = new ThirdParty();

    @Data
    @ToString
    public static class Path {
        private String apiDoc;
        private String apiTitle;
        private String apiVersion;
        private String businessInteractions;
        private String businessInteractionsDesc;
    }

    @Data
    @ToString
    public static class ThirdParty {
        private String businessInteractionsEndpoint;
    }

    @Data
    @ToString
    public static class Queues {
        private String businessInteractionsGenerate;
    }
}
