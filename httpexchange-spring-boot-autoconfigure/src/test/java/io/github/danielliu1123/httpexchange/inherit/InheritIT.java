package io.github.danielliu1123.httpexchange.inherit;

import io.github.danielliu1123.PortGetter;
import io.github.danielliu1123.httpexchange.EnableExchangeClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Freeman
 */
class InheritIT {

    @Test
    void testInherit() {
        int port = PortGetter.availablePort();
        var ctx = new SpringApplicationBuilder(Cfg.class)
                .properties("server.port=" + port)
                .properties("http-exchange.base-url=localhost:" + port)
                .run();

        PetApi petApi = ctx.getBean(PetApi.class);
        PostApi postApi = ctx.getBean(PostApi.class);

        assertEquals("1", petApi.get("1").id());
        assertEquals("1", postApi.get("1").id());

        ctx.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableExchangeClients(clients = {PetApi.class, PostApi.class})
    static class Cfg {

        @RestController
        static class PetServer implements PetApi {

            @Override
            public PetDTO get(String id) {
                return new PetDTO(id, "name");
            }
        }

        @RestController
        static class PostServer implements PostApi {

            @Override
            public PostDTO get(String s) {
                return new PostDTO(s, "title", "content");
            }

            @Override
            public List<PostDTO> list(List<String> strings) {
                return List.of();
            }
        }
    }
}
