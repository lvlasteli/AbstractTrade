package com.example.metrics;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;

import java.net.InetAddress;

@SpringBootApplication
@EnableKafka
public class MetricsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricsApplication.class, args);
    }

    @Bean
    public CommandLineRunner startupLogger(Environment env) {
        return args -> {
            String protocol = "http";
            String serverPort = env.getProperty("server.port", "8080");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            
            System.out.println("MetricsService URL: " + protocol + "://" + hostAddress + ":" + serverPort);
        };
    }

}
