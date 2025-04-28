package msa.lime1st.cloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@EnableConfigServer
@SpringBootApplication
public class ConfigApplication {


    private static final Logger log = LoggerFactory.getLogger(ConfigApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(ConfigurableApplicationContext ctx) {
        return args -> {
            String repoLocation = ctx.getEnvironment()
                .getProperty("spring.cloud.config.server.native.search-locations[0]");
            log.info("Search locations: '{}'", repoLocation);
        };
    }
}
