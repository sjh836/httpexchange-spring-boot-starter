package io.github.danielliu1123.httpexchange.it.inherit;

import java.util.Collection;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface ApiBase<T, ID> {
    @GetExchange("/{id}")
    T get(@PathVariable("id") ID id);

    @GetExchange("/list")
    List<T> list(@RequestBody Collection<ID> ids);

    @PostExchange
    T create(@RequestBody T t);

    @PutExchange
    T update(@RequestBody T t);

    @DeleteExchange("/{id}")
    int delete(@PathVariable("id") ID id);
}
