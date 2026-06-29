package com.blockendcall;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
public class BlockEndCallApplication {

    // The default secret committed to source control — publicly known, must never be used in prod.
    private static final String DEFAULT_JWT_PREFIX = "404E6352";

    public static void main(String[] args) {
        SpringApplication.run(BlockEndCallApplication.class, args);
    }

    @Bean
    ApplicationRunner assertSecretsConfigured(
            @Value("${security.jwt.secret}") String jwtSecret,
            Environment env) {
        return args -> {
            boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
            if (isProd && jwtSecret.startsWith(DEFAULT_JWT_PREFIX)) {
                throw new IllegalStateException(
                        "JWT_SECRET environment variable must be overridden in production. " +
                        "The committed default key is publicly known and allows arbitrary token forgery.");
            }
        };
    }
}
