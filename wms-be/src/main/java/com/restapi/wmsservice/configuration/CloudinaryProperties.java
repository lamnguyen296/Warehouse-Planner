package com.restapi.wmsservice.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String folder = "wms";

    public boolean isConfigured() {
        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }
}
