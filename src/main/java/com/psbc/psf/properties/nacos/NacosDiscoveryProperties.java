package com.psbc.psf.properties.nacos;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 2025/3/10 15:33
 * auth: dahua
 * desc:
 */
@ConfigurationProperties(prefix = NacosDiscoveryProperties.PREFIX)
public class NacosDiscoveryProperties {

    public static final String PREFIX = "spring.cloud.nacos";

    List<NacosDiscoveryConfigProperties> discoveries;

    public List<NacosDiscoveryConfigProperties> getDiscoveries() {
        return discoveries;
    }

    public void setDiscoveries(List<NacosDiscoveryConfigProperties> discoveries) {
        this.discoveries = discoveries;
    }
}
