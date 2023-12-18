package io.github.danielliu1123.httpexchange.inherit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * @author Freeman
 */
@HttpExchange("/pet")
public interface PetApi extends ApiBase<PetApi.PetDTO, String> {

    @Override
    @GetExchange("/{id}")
    PetDTO get(@PathVariable String id);

    record PetDTO(String id, String name) {
    }
}
