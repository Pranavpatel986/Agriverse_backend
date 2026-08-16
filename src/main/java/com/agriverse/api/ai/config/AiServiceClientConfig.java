package com.agriverse.api.ai.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Two {@link RestClient} beans against the same AI service: one with the
 * short default timeout used by embedding/recommendation calls, one with
 * the longer read timeout LLM generation needs for the chat endpoint.
 * Callers (see {@link com.agriverse.api.ai.service.AiServiceClient}) pick
 * whichever fits.
 *
 * Uses Apache HttpClient 5 (org.apache.httpcomponents.client5:httpclient5),
 * not the JDK's built-in java.net.http.HttpClient. This used to run on the
 * JDK client specifically to avoid the extra dependency, but that client
 * was unreliably dropping POST bodies against this exact AI service setup
 * -- the AI service's own request validation confirmed it was receiving a
 * completely empty/null body despite AiServiceClient correctly constructing
 * one every time (see AiServiceClient.chat()). Apache HttpClient 5 is the
 * far more battle-tested option for this kind of call and doesn't share
 * that failure mode.
 */
@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceClientConfig {

    @Bean
    public RestClient aiServiceRestClient(AiServiceProperties props) {
        return buildClient(props, props.getDefaultReadTimeoutMs());
    }

    @Bean
    public RestClient aiServiceChatRestClient(AiServiceProperties props) {
        return buildClient(props, props.getChatReadTimeoutMs());
    }

    private RestClient buildClient(AiServiceProperties props, int readTimeoutMs) {
        var connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(
                ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(props.getConnectTimeoutMs()))
                        .setSocketTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                        .build());

        var requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build();

        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Api-Key", props.getInternalApiKey())
                .build();
    }
}
