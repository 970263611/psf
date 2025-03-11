package com.psbc.psf.properties.nacos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2025/3/11 9:31
 * auth: dahua
 * desc:
 */
public class NacosDiscoveryConfigProperties {

    private static final Logger logger = LoggerFactory.getLogger(NacosDiscoveryConfigProperties.class);

    /**
     * nacos discovery server address.
     */
    private String serverAddr;

    /**
     * the nacos authentication username.
     */
    private String username;

    /**
     * the nacos authentication password.
     */
    private String password;

    /**
     * namespace, separation registry of different environments.
     */
    private String namespace = "public";

    /**
     * weight for service instance, the larger the value, the larger the weight.
     */
    private float weight = 1;

    /**
     * cluster name for nacos .
     */
    private String clusterName = "DEFAULT";

    /**
     * group name for nacos.
     */
    private String group = "DEFAULT_GROUP";

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
