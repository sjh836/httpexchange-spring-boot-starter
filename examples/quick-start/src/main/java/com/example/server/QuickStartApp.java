package com.example.server;

import com.example.api.UserDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class QuickStartApp {
    public static void main(String[] args) {
        SpringApplication.run(QuickStartApp.class, args);
    }

    @Bean
    ApplicationRunner restTemplateRunner(RestTemplateBuilder builder, @Value("${server.port}") int port) {
        RestTemplate cli = builder.build();
        return args -> {
            UserDTO user = cli.getForObject("http://localhost:" + port + "/user/1", UserDTO.class);
            System.out.println(user);
        };
    }

    @Bean
    ApplicationRunner restClientRunner(RestClient.Builder builder, @Value("${server.port}") int port) {
        RestClient cli = builder.build();
        return args -> {
            UserDTO user = cli.get()
                    .uri("http://localhost:" + port + "/user/1")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            System.out.println(user);
        };
    }

    @Bean
    ApplicationRunner jdbcTemplateRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            List<User> users = jdbcTemplate.query("select * from `user`", DataClassRowMapper.newInstance(User.class));
            System.out.println(users);
        };
    }

    @Bean
    ApplicationRunner jdbcClientRunner(JdbcClient jdbcClient) {
        return args -> {
            List<User> users =
                    jdbcClient.sql("select * from `user`").query(User.class).list();
            System.out.println(users);
        };
    }

    record User(String id, String name, String hobbies) {}
}
