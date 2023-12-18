package io.github.danielliu1123.httpexchange.it.inherit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Collection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author Freeman
 */
class InheritTests {

    @Test
    @SneakyThrows
    void testInheritInterface() {
        Class<?> clz = Class.forName("io.github.danielliu1123.httpexchange.it.inherit.UserApiBase");
        assertDoesNotThrow(() -> {
            clz.getDeclaredMethod("get", String.class);
            clz.getDeclaredMethod("list", Collection.class);
            clz.getDeclaredMethod("create", UserApi.UserDTO.class);
            clz.getDeclaredMethod("update", UserApi.UserDTO.class);
            clz.getDeclaredMethod("delete", String.class);
            clz.getDeclaredMethod("getName", String.class);
        });
    }
}
