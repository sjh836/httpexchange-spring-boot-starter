package io.github.danielliu1123.httpexchange.inherit;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.io.Serializable;

/**
 * @author Freeman
 */
public interface ApiBase<T, ID extends Serializable> {

    @GetExchange("/{id}")
    T get(@PathVariable("id") ID id);
}
