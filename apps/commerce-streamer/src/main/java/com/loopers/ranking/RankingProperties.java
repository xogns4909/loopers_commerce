package com.loopers.ranking;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ranking")
public class RankingProperties {
    
    private boolean enabled = true;
    private Batch batch = new Batch();
    private Score score = new Score();
    private Ttl ttl = new Ttl();
    
    @Getter
    @Setter
    public static class Batch {
        private boolean enabled = true;
        private int size = 300;
        private long timeoutMs = 30000;
    }
    
    @Getter
    @Setter
    public static class Score {
        private Weights weights = new Weights();
        
        @Getter
        @Setter
        public static class Weights {
            private double view = 0.1;
            private double like = 0.2;
            private double unlike = -0.2;
            private double order = 0.7;
        }
    }
    
    @Getter
    @Setter
    public static class Ttl {
        private int hours = 48;
    }
}
