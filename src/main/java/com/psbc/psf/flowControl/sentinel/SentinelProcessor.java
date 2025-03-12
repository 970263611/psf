package com.psbc.psf.flowControl.sentinel;

import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.logging.slf4j.CommandCenterLogLogger;
import com.alibaba.csp.sentinel.logging.slf4j.RecordLogLogger;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.psbc.psf.flowControl.FlowControlProcessor;
import com.psbc.psf.handler.HandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * 2025/3/8 9:28
 * auth: dahua
 * desc:
 */
@Component
public class SentinelProcessor implements FlowControlProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HandlerChain.class);

    @Autowired
    private SentinelRulesProperties sentinelRulesProperties;

    @PostConstruct
    public void initRule() {
        loadRules();
    }

    public void clearRules() {
        FlowRuleManager.loadRules(null);
        logger.debug("Sentinel flow rules clear success");
        SystemRuleManager.loadRules(null);
        logger.debug("Sentinel system rules clear success");
        ParamFlowRuleManager.loadRules(null);
        logger.debug("Sentinel paramFlow rules clear success");
        DegradeRuleManager.loadRules(null);
        logger.debug("Sentinel degrade rules clear success");
        AuthorityRuleManager.loadRules(null);
        logger.debug("Sentinel authority rules clear success");
    }

    public void loadRules() {
        List<FlowRule> flowRules = sentinelRulesProperties.getFlow();
        List<SystemRule> systemRules = sentinelRulesProperties.getSystem();
        List<ParamFlowRule> paramFlowRules = sentinelRulesProperties.getParamFlow();
        List<DegradeRule> degradeRules = sentinelRulesProperties.getDegrade();
        List<AuthorityRule> authorityRules = sentinelRulesProperties.getAuthority();
        try {
            if (flowRules != null && !flowRules.isEmpty()) {
                FlowRuleManager.loadRules(flowRules);
                logger.info("Sentinel load flow rules \n{}", flowRules);
            }
        } catch (Exception e) {
            logger.error("Sentinel load flow rules error {}", e);
        }
        try {
            if (systemRules != null && !systemRules.isEmpty()) {
                SystemRuleManager.loadRules(systemRules);
                logger.info("Sentinel load system rules \n{}", systemRules);
            }
        } catch (Exception e) {
            logger.error("Sentinel load flow rules error {}", e);
        }
        try {
            if (paramFlowRules != null && !paramFlowRules.isEmpty()) {
                ParamFlowRuleManager.loadRules(paramFlowRules);
                logger.info("Sentinel load paramFlow rules \n{}", paramFlowRules);
            }
        } catch (Exception e) {
            logger.error("Sentinel load flow rules error {}", e);
        }
        try {
            if (degradeRules != null && !degradeRules.isEmpty()) {
                DegradeRuleManager.loadRules(degradeRules);
                logger.info("Sentinel load degrade rules \n{}", degradeRules);
            }
        } catch (Exception e) {
            logger.error("Sentinel load flow rules error {}", e);
        }
        try {
            if (authorityRules != null && !authorityRules.isEmpty()) {
                AuthorityRuleManager.loadRules(authorityRules);
                logger.info("Sentinel load authority rules \n{}", authorityRules);
            }
        } catch (Exception e) {
            logger.error("Sentinel load flow rules error {}", e);
        }
    }

    @Override
    public boolean flowControl(String name) {
        if (SphO.entry(name)) {
            try {
                return true;
            } finally {
                SphO.exit();
            }
        }
        return false;
    }

    private boolean needRefreshSentinelRules = false;

    @EventListener
    public void onEnvironmentChangeEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        for (String key : keys) {
            if (key.startsWith(SentinelRulesProperties.PREFIX)) {
                needRefreshSentinelRules = true;
                break;
            }
        }
    }

    @EventListener
    public void onRefreshEvent(RefreshEvent event) {
        if (needRefreshSentinelRules) {
            clearRules();
            loadRules();
            needRefreshSentinelRules = false;
        }
    }
}
