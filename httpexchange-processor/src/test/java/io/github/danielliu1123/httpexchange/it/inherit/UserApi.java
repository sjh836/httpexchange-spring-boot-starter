package io.github.danielliu1123.httpexchange.it.inherit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/user")
public interface UserApi extends ApiBase<UserApi.UserDTO, String> {

    record UserDTO(String id, String name) {}

    @GetExchange("/getName/{id}")
    String getName(@PathVariable String id);
}
