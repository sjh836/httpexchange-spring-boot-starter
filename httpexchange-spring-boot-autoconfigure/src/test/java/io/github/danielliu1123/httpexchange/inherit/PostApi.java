package io.github.danielliu1123.httpexchange.inherit;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * @author Freeman
 */
@HttpExchange("/post")
public interface PostApi extends ApiBase<PostApi.PostDTO, String> {

    record PostDTO(String id, String title, String content) {
    }

    @PostExchange("/list")
    List<PostDTO> list(@RequestBody List<String> ids);
}
