package com.example.server;

import com.example.api.UserApi;
import com.example.api.UserApiBase;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class QuickStartApp extends UserApiBase /*implements User2Api*/ {

    public static void main(String[] args) {
        SpringApplication.run(QuickStartApp.class, args);
    }

    @Override
    public UserApi.UserDTO getById(String id) {
        return new UserApi.UserDTO(id, "Freeman", List.of("Coding", "Reading"));
    }

    /* @Override
    public User2Api.UserDTO getById2(String id) {
        return null;
    }

    @Override
    public User2Api.UserDTO getByName2(String name) {
        return null;
    }*/
}
