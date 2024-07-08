package org.example.config;

import org.example.service.CrptApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class CrptApiConfiguration {

    @Bean
    public CrptApi crptApi(RestClient.Builder restClientBuilder) {
        return new CrptApi(TimeUnit.MINUTES, 5, restClientBuilder);
    }
}
