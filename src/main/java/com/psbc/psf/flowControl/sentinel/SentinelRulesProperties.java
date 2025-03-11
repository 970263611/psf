package com.psbc.psf.flowControl.sentinel;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 2025/3/8 10:00
 * auth: dahua
 * desc:
 */
@ConfigurationProperties(prefix = SentinelRulesProperties.PREFIX)
public class SentinelRulesProperties {

    public static final String PREFIX = "spring.cloud.sentinel.rules";
    private List<FlowRule> flow;
    private List<SystemRule> system;
    private List<ParamFlowRule> paramFlow;
    private List<DegradeRule> degrade;
    private List<AuthorityRule> authority;

    public List<FlowRule> getFlow() {
        return flow;
    }

    public void setFlow(List<FlowRule> flow) {
        this.flow = flow;
    }

    public List<SystemRule> getSystem() {
        return system;
    }

    public void setSystem(List<SystemRule> system) {
        this.system = system;
    }

    public List<ParamFlowRule> getParamFlow() {
        return paramFlow;
    }

    public void setParamFlow(List<ParamFlowRule> paramFlow) {
        this.paramFlow = paramFlow;
    }

    public List<DegradeRule> getDegrade() {
        return degrade;
    }

    public void setDegrade(List<DegradeRule> degrade) {
        this.degrade = degrade;
    }

    public List<AuthorityRule> getAuthority() {
        return authority;
    }

    public void setAuthority(List<AuthorityRule> authority) {
        this.authority = authority;
    }
}
