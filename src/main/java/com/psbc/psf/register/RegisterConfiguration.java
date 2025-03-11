package com.psbc.psf.register;

import com.psbc.psf.properties.nacos.NacosDiscoveryConfigProperties;
import com.psbc.psf.properties.nacos.NacosDiscoveryProperties;
import com.psbc.psf.register.nacos.NacosReactiveDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 2025/3/10 15:43
 * auth: dahua
 * desc:
 */
@Configuration
public class RegisterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RegisterConfiguration.class);

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Bean("PsfReactiveCompositeDiscoveryClient")
    public ReactiveCompositeDiscoveryClient reactiveCompositeDiscoveryClient() {
        List<ReactiveDiscoveryClient> nacosReactiveDiscoveryClientList = new ArrayList<>();
        List<NacosDiscoveryConfigProperties> discoveries = nacosDiscoveryProperties.getDiscoveries();
        for (NacosDiscoveryConfigProperties discovery : discoveries) {
            nacosReactiveDiscoveryClientList.add(new NacosReactiveDiscoveryClient(discovery));
        }
        return new ReactiveCompositeDiscoveryClient(nacosReactiveDiscoveryClientList);
    }
}
