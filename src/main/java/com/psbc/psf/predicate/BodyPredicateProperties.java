package com.psbc.psf.predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 2025/3/12 17:12
 * auth: dahua
 * desc:
 */
public class BodyPredicateProperties {

    private static final Logger logger = LoggerFactory.getLogger(BodyPredicateProperties.class);

    private List<KV> keysAndValues;

    public List<KV> getKeysAndValues() {
        return keysAndValues;
    }

    public void setKeysAndValues(List<KV> keysAndValues) {
        this.keysAndValues = keysAndValues;
    }

    public class KV {

        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
