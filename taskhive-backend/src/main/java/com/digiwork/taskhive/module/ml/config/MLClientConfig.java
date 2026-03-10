package com.digiwork.taskhive.module.ml.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * MLClientConfig
 * ───────────────
 * Configures a dedicated RestTemplate bean for ML server communication.
 *
 * Timeouts (per SRS — non-blocking, admin never waits more than 5s for ML):
 * connect timeout : 3 seconds
 * response timeout : 5 seconds
 *
 * Connection pool size: 10 (sufficient for one ML server)
 */
@Configuration
public class MLClientConfig {

    @Value("${ml.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${ml.response-timeout-ms:5000}")
    private int responseTimeoutMs;

    @Bean("mlRestTemplate")
    public RestTemplate mlRestTemplate() {

        // Connection pool to avoid creating new connections per request
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);

        // Request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeoutMs, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(responseTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}
