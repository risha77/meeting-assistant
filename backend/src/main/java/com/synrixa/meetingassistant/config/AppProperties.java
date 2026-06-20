package com.synrixa.meetingassistant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class AppProperties {

    private Deepgram deepgram = new Deepgram();
    private Openai openai = new Openai();
    private Cors cors = new Cors();

    @Getter @Setter
    public static class Deepgram {
        private String apiKey;
        private String url;
        private String model;
        private String language;
        private boolean punctuate;
        private boolean diarize;
        private boolean smartFormat;
    }

    @Getter @Setter
    public static class Openai {
        private String apiKey;
        private String model;
        private int maxTokens;
    }

    @Getter @Setter
    public static class Cors {
        private String allowedOrigins;
    }
}
