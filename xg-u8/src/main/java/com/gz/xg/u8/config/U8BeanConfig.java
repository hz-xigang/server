package com.gz.xg.u8.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 用友模块 Bean 配置
 */
@Configuration
@RequiredArgsConstructor
public class U8BeanConfig {

    @Resource
    private U8Config u8Config;

    @Bean
    public RestTemplate u8RestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(u8Config.getConnectTimeout());
        factory.setReadTimeout(u8Config.getReadTimeout());

        return new RestTemplateBuilder()
                .requestFactory(() -> factory)
                .build();
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }
}
