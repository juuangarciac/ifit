package com.uca.juangarcia.adminpanel.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configura el {@link RestClient} que habla con el API Gateway de iFit.
 *
 * <p>Usa {@link JdkClientHttpRequestFactory} (HttpClient de la JDK) para soportar el verbo
 * <strong>PATCH</strong> (necesario para {@code toggle-active}), que la factoría por defecto
 * basada en {@code HttpURLConnection} no admite.
 */
@Configuration
public class GatewayClientConfig {

    @Bean
    public RestClient gatewayRestClient(@Value("${ifit.gateway.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }
}
