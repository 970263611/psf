PSF
```yaml
spring:
  cloud:
    loadbalancer:
      cache:
        caffeine:
          spec: initialCapacity=500,expireAfterWrite=30s
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: abc
          uri: lb://dalu
          predicates:
            - Path=/api/hello
    nacos:
      discoveries:
        - serverAddr: 192.168.100.254:8848
          # username: nacos
          # password: nacos
          # namespace: 2b758abe-54d1-4d46-b54e-97a1adfc2bab
          group: DEFAULT_GROUP
    sentinel:
      rules:
        flow:
          - resource: abc
            count: 1
            grade: 1
            limitApp: default
            strategy: 0
            controlBehavior: 0
        system:
          - avgRt: 100
            maxThread: 20
            qps: 1000
            highestCpuUsage: 0.8
        paramFlow:
          - resource: a
            paramIdx: 0
            count: 20
            paramFlowItemList:
              - object: hot_param_value
                count: 10
        degrade:
          - resource: b
            grade: 0
            count: 100
            timeWindow: 10
            statIntervalMs: 1000
        authority:
          - resource: c
            strategy: 0
            limitApp: 'app1,app2'
    psf:
      predicates:
        uri:
          - path: /api/hello
            return: {"a":1,"b":["a","b"]}
        header:
          - a: '["a","b"]'
            b: c
            return:
              a: 11
              b: 22
        param:
          - b: b
            return:
              a: 111
              b: 222
        exception:
          - name: com.psbc.psf.exception.PsfFlowControlException
            return:
              a: 111
              b: 2221
        defaultReturn:
          a: 1111
          b: 2222
```

