package com.guiji.apiautomationfinal.entity;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "tfx")
public class RunEnvEntity {
    private String baseUrl;
    private List<String> countries;
    private List<String> modules;
    private String environment;
    private boolean parallel;
    private Map<String, Map<String, Object>> papiHeaders;
}
