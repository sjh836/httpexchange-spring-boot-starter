package com.freemanan.starter.httpexchange;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Convert a Java bean to query parameters.
 *
 * <p> Correct usage:
 * <pre>{@code
 * @GetExchange
 * User get(@BeanParam User user);
 *
 * @GetExchange
 * User get(@RequestParam Map<String, Object> user);
 * }</pre>
 *
 * <p> NOTE: if you consider using {@link Map} as a parameter type, you should use {@link RequestParam} instead.
 * <p> Incorrect usage:
 * <pre>{@code
 * @GetExchange
 * User get(@BeanParam Map<String, Object> user); // use @RequestParam instead
 * }</pre>
 *
 * @author Freeman
 * @since 3.1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BeanParam {}
