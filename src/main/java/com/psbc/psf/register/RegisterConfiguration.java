package com.psbc.psf.register;

import com.psbc.psf.properties.nacos.NacosDiscoveryConfigProperties;
import com.psbc.psf.properties.nacos.NacosDiscoveryProperties;
import com.psbc.psf.register.nacos.NacosReactiveDiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

    @Bean("PsfReactiveCompositeDiscoveryClient")
    @RefreshScope
    public ReactiveCompositeDiscoveryClient reactiveCompositeDiscoveryClient(NacosDiscoveryProperties nacosDiscoveryProperties) {
        List<ReactiveDiscoveryClient> nacosReactiveDiscoveryClientList = new ArrayList<>();
        List<NacosDiscoveryConfigProperties> discoveries = nacosDiscoveryProperties.getDiscoveries();
        if (discoveries != null) {
            for (NacosDiscoveryConfigProperties discovery : discoveries) {
                nacosReactiveDiscoveryClientList.add(new NacosReactiveDiscoveryClient(discovery));
            }
        }
        return new ReactiveCompositeDiscoveryClient(nacosReactiveDiscoveryClientList);
    }
}
