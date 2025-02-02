package io.github.danielliu1123.httpexchange;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Http Clients Configuration Properties.
 *
 * @author Freeman
 */
@Data
@ConfigurationProperties(HttpExchangeProperties.PREFIX)
public class HttpExchangeProperties implements InitializingBean {
    public static final String PREFIX = "http-exchange";

    /**
     * Whether to enable http exchange autoconfiguration, default {@code true}.
     */
    private boolean enabled = true;
    /**
     * Base packages to scan, use {@link EnableExchangeClients#basePackages} first if configured.
     */
    private Set<String> basePackages = new LinkedHashSet<>();
    /**
     * Exchange client interfaces to register as beans, use {@link EnableExchangeClients#clients} first if configured.
     *
     * @since 3.2.0
     */
    private Set<Class<?>> clients = new LinkedHashSet<>();
    /**
     * Default base url, 'http' scheme can be omitted.
     *
     * <p> If loadbalancer is enabled, this value means the service id.
     *
     * <ul>
     *     <li>localhost:8080</li>
     *     <li>http://localhost:8080</li>
     *     <li>https://localhost:8080</li>
     *     <li>localhost:8080/api</li>
     *     <li>user(service id)</li>
     * </ul>
     */
    private String baseUrl;
    /**
     * Default headers will be added to all the requests.
     */
    private List<Header> headers = new ArrayList<>();
    /**
     * Channels configuration.
     */
    private List<Channel> channels = new ArrayList<>();
    /**
     * Whether to convert Java bean to query parameters, default value is {@code false}.
     */
    private boolean beanToQueryEnabled = false;
    /**
     * Refresh configuration.
     */
    private Refresh refresh = new Refresh();
    /**
     * Client type, default {@link ClientType#REST_CLIENT}.
     *
     * <p color="orange"> NOTE: the {@link #connectTimeout} and {@link #readTimeout} settings are not supported by {@link ClientType#WEB_CLIENT}.
     *
     * @see ClientType
     * @since 3.2.0
     */
    private ClientType clientType = ClientType.REST_CLIENT;
    /**
     * whether to process {@link org.springframework.web.bind.annotation.RequestMapping} based annotation,
     * default {@code false}.
     *
     * <p color="red"> Recommending to use {@link org.springframework.web.service.annotation.HttpExchange} instead of {@link org.springframework.web.bind.annotation.RequestMapping}.
     *
     * @since 3.2.0
     */
    private boolean requestMappingSupportEnabled = false;
    /**
     * Connect timeout duration, specified in milliseconds.
     * Negative, zero, or null values indicate that the timeout is not set.
     *
     * @since 3.2.0
     */
    private Integer connectTimeout;
    /**
     * Read timeout duration, specified in milliseconds.
     * Negative, zero, or null values indicate that the timeout is not set.
     *
     * @since 3.2.0
     */
    private Integer readTimeout;
    /**
     * Whether to check unused configuration, default {@code true}.
     *
     * @since 3.2.0
     */
    private boolean warnUnusedConfigEnabled = true;
    /**
     * Whether to enable loadbalancer, default {@code true}.
     *
     * <p> Prerequisites:
     * <ul>
     *     <li> {@code spring-cloud-starter-loadbalancer} dependency in the classpath.</li>
     *     <li> {@code spring.cloud.loadbalancer.enabled=true}</li>
     * </ul>
     *
     * @since 3.2.0
     */
    private boolean loadbalancerEnabled = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        /**
         * Header key.
         */
        private String key;
        /**
         * Header values.
         */
        private List<String> values = new ArrayList<>();
    }

    @Override
    public void afterPropertiesSet() {
        merge();
    }

    /**
     * Merge default configuration to channels configuration.
     */
    void merge() {
        PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        for (Channel chan : channels) {
            mapper.from(baseUrl).when(e -> isNull(chan.getBaseUrl())).to(chan::setBaseUrl);
            mapper.from(clientType).when(e -> isNull(chan.getClientType())).to(chan::setClientType);
            mapper.from(connectTimeout)
                    .when(e -> isNull(chan.getConnectTimeout()))
                    .to(chan::setConnectTimeout);
            mapper.from(readTimeout).when(e -> isNull(chan.getReadTimeout())).to(chan::setReadTimeout);
            mapper.from(loadbalancerEnabled)
                    .when(e -> isNull(chan.getLoadbalancerEnabled()))
                    .to(chan::setLoadbalancerEnabled);

            // defaultHeaders + chan.headers
            LinkedHashMap<String, List<String>> total = headers.stream()
                    .collect(toMap(Header::getKey, Header::getValues, (oldV, newV) -> oldV, LinkedHashMap::new));
            for (Header header : chan.getHeaders()) {
                total.put(header.getKey(), header.getValues());
            }
            List<Header> mergedHeaders = total.entrySet().stream()
                    .map(e -> new Header(e.getKey(), e.getValue()))
                    .toList();
            chan.setHeaders(mergedHeaders);
        }
    }

    HttpExchangeProperties.Channel defaultClient() {
        return new Channel(
                null,
                baseUrl,
                headers,
                clientType,
                connectTimeout,
                readTimeout,
                loadbalancerEnabled,
                List.of(),
                List.of());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Channel {
        /**
         * Optional channel name.
         */
        private String name;
        /**
         * Base url, use {@link HttpExchangeProperties#baseUrl} if not set.
         */
        private String baseUrl;
        /**
         * Default headers, will be merged with {@link HttpExchangeProperties#headers}.
         */
        private List<Header> headers = new ArrayList<>();
        /**
         * Client type, use {@link HttpExchangeProperties#clientType} if not set.
         *
         * <p color="orange"> NOTE: the {@link #connectTimeout} and {@link #readTimeout} settings are not supported by {@link ClientType#WEB_CLIENT}.
         *
         * @see ClientType
         */
        private ClientType clientType;
        /**
         * Connection timeout duration, specified in milliseconds.
         * Negative, zero, or null values indicate that the timeout is not set.
         *
         * <p> Use {@link HttpExchangeProperties#connectTimeout} if not set.
         *
         * @see HttpExchangeProperties#connectTimeout
         * @since 3.2.0
         */
        private Integer connectTimeout;
        /**
         * Read timeout duration, specified in milliseconds.
         * Negative, zero, or null values indicate that the timeout is not set.
         *
         * <p> Use {@link HttpExchangeProperties#readTimeout} if not set.
         *
         * @see HttpExchangeProperties#readTimeout
         * @since 3.2.0
         */
        private Integer readTimeout;
        /**
         * Whether to enable loadbalancer, use {@link HttpExchangeProperties#loadbalancerEnabled} if not set.
         *
         * @see HttpExchangeProperties#loadbalancerEnabled
         * @since 3.2.0
         */
        private Boolean loadbalancerEnabled;
        /**
         * Exchange Clients to apply this channel.
         *
         * <p> e.g. client {@code com.example.client.ExampleClient} can be identified by
         * <ul>
         *     <li> {@code ExampleClient}, {@code exampleClient}, {@code example-client} (Class simple name)
         *     <li> {@code com.example.client.ExampleClient} (Class canonical name)
         *     <li> {@code com.**.*Client}, {@code com.example.**} (<a href="https://stackoverflow.com/questions/2952196/ant-path-style-patterns">Ant style pattern</a>)
         * </ul>
         *
         * <p> This is a more flexible alternative to {@link HttpExchangeProperties.Channel#classes}.
         *
         * @see Class#getCanonicalName()
         * @see org.springframework.util.AntPathMatcher
         */
        private List<String> clients = new ArrayList<>();
        /**
         * Exchange Client classes to apply this channel.
         *
         * <p> This is a more IDE-friendly alternative to {@link HttpExchangeProperties.Channel#clients}.
         */
        private List<Class<?>> classes = new ArrayList<>();
    }

    @Data
    public static class Refresh {
        public static final String PREFIX = HttpExchangeProperties.PREFIX + ".refresh";
        /**
         * Whether to enable refresh exchange clients, default {@code false}.
         *
         * <p> NOTE: this feature needs {@code spring-cloud-context} dependency in the classpath.
         */
        private boolean enabled = false;
    }

    public enum ClientType {
        /**
         * @see RestClient
         */
        REST_CLIENT,
        /**
         * @see WebClient
         */
        WEB_CLIENT,
        /**
         * @see RestTemplate
         */
        REST_TEMPLATE
    }
}
