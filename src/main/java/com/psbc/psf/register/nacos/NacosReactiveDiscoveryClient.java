package com.psbc.psf.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.psbc.psf.properties.nacos.NacosDiscoveryConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 2025/3/10 12:03
 * auth: dahua
 * desc: 重写NacosReactiveDiscoveryClient
 */
public class NacosReactiveDiscoveryClient implements ReactiveDiscoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(NacosReactiveDiscoveryClient.class);
    private NacosDiscoveryConfigProperties configProperties;
    private List<String> serviceNames = new ArrayList<>();
    private Map<String, List<ServiceInstance>> serviceInstances = new HashMap<>();

    public NacosReactiveDiscoveryClient(NacosDiscoveryConfigProperties configProperties) {
        this.configProperties = configProperties;
        loadSourceData();
    }

    @Override
    public String description() {
        return "Psf Nacos Reactive Discovery Client " + Thread.currentThread().getName();
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        loadSourceData();
        List<ServiceInstance> serviceInstances = this.serviceInstances.get(serviceId);
        if (serviceInstances == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(this.serviceInstances.get(serviceId));
    }

    @Override
    public Flux<String> getServices() {
        if (serviceNames.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(serviceNames);
    }

    private void loadSourceData() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", configProperties.getServerAddr());
            if (configProperties.getUsername() != null) {
                properties.put("username", configProperties.getUsername());
            }
            if (configProperties.getPassword() != null) {
                properties.put("password", configProperties.getPassword());
            }
            properties.put("namespace", configProperties.getNamespace());
            NamingService namingService = NacosFactory.createNamingService(properties);
            loadServices(namingService, configProperties.getGroup());
        } catch (NacosException e) {
            logger.error("Find instance from nacos error {}", e);
        }
    }

    private void loadServices(NamingService namingService, String group) throws NacosException {
        ListView<String> servicesOfServer = namingService.getServicesOfServer(1, Integer.MAX_VALUE, group);
        serviceNames = servicesOfServer.getData();
        for (String serviceName : serviceNames) {
            loadInstances(namingService, serviceName, group);
        }
    }

    private void loadInstances(NamingService namingService, String serviceName, String group) throws NacosException {
        List<Instance> instances = namingService.getAllInstances(serviceName, group, true);
        List<ServiceInstance> serviceInstances = hostToServiceInstanceList(instances, serviceName);
        this.serviceInstances.put(serviceName, serviceInstances);
    }

    public static List<ServiceInstance> hostToServiceInstanceList(
            List<Instance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            ServiceInstance serviceInstance = hostToServiceInstance(instance, serviceId);
            if (serviceInstance != null) {
                result.add(serviceInstance);
            }
        }
        return result;
    }

    public static ServiceInstance hostToServiceInstance(Instance instance,
                                                        String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return null;
        }
        NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceId(serviceId);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        metadata.putAll(instance.getMetadata());
        nacosServiceInstance.setMetadata(metadata);

        if (metadata.containsKey("secure")) {
            boolean secure = Boolean.parseBoolean(metadata.get("secure"));
            nacosServiceInstance.setSecure(secure);
        }
        return nacosServiceInstance;
    }
}
