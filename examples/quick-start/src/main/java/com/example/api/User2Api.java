package com.example.api;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("/user2")
public interface User2Api {
    record UserDTO(String id, String name, List<String> hobbies) {}

    @GetMapping("/getById2/{id}")
    UserDTO getById2(@PathVariable("id") @NotBlank @Length(max = 5) String id);

    @GetMapping("/getByName2/{name}")
    UserDTO getByName2(@PathVariable("name") @NotBlank String name);
}
