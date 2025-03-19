# PSF网关组件

Psf支持所有配置**动态修改后生效**，如Nacos、Apollo和Spring cloud config等注册中心。

## 1.路由转发配置

Psf完全**兼容**spring cloud gateway（以下简称gateway）原生所有的配置，并扩展了新的断言。

gateway原生route配置包含三部分id、uri、predicates，如：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: https://example.org
        predicates:
        - Cookie=mycookie,mycookievalue
```

gateway原生支持的predicate有：

- Header
- Path
- Cookie
- Query
- Before
- After
- Between
- Host
- Method
- RemoteAddr
- Weight
- XForwardedRemoteAddr
- ReadBody
- CloudFoundry

Psf扩展了predicate，新增了三种断言。

- Body
- WhiteList
- Or

多个断言可叠加，断言间为”且“的关系。

### 断言配置

断言配置分为两大部分name和args，不同的断言args中的参数是不一样的，请根据具体实现具体配置，下面示例是Header断言的配置：

```yaml
- name: 断言名称（Header）
  args:
  	header: 请求头key
  	regexp: 对应的value（这里可以配置正则表达式）
```

上面的格式化标准配置，在gateway中提供了简化配置方式，可以根据逗号分隔自动映射断言配置，所以上述配置亦可配置为

```yaml
- Header=请求头key,对应的value
```

Psf新增的三种断言配置：

**Body断言：**

body断言只匹配body体中的kv，这里key配置格式为jsonpath（需要了解下），以下相同

```yaml
- name: Body
  args: 
    key: key的jsonpath
    value: key的value    
```

也可简化为

```yaml
- Body=key的jsonpath,key的value 
```

**WhiteList断言：**

whiteList断言只匹配body体中的kv，value的配置为数组，其中配置的任意值匹配到key的实际传入值即可断言通过，但不支持简化配置。

```yaml
- name: WhiteList
  args:
    key: key的jsonpath
    value:
      - key可能的value1
      - key可能的value2
```

**Or断言：**

or断言比较特殊，or中可以配置无限多个其他断言，其中任意断言通过则认为or断言通过，但不支持简化配置。

```yaml
- name: Or
  args:
    predicates:
      - name: WhiteList
        args:
          key: key的jsonpath
          value:
            - key可能的value1
            - key可能的value2
      - name: WhiteList
        args:
          key: key的jsonpath
          value:
            - key可能的value1
            - key可能的value2
      - name: Header
        args:
          header: 请求头key
          regexp: 对应的value
```

断言规则中配置的参数一定为字符串类型，但是传递的参数可能为其他类型，所以需要配置是否忽略类型只进行值匹配。

```properties
spring.cloud.gateway.routeBodyEqualsIgnoreType=true #true则只进行值匹配，否则会先类型匹配在值匹配
```

## 2.寻址配置

在gateway配置的路由中包含lb负载均衡时，需要去注册中心动态寻找服务名对应的节点地址，Psf支持在**多个配置中心**中寻找（多**中心中注册的服务名请不要重复**）。如未配置lb负载均衡，则会正常路由到下游地址。目前注册中心只支持Nacos，通过配置多组*spring.cloud.nacos.discoveries*来帮助寻址。

**具体配置：**

```yaml
spring:
  cloud:
    nacos:
      discoveries:
        - serverAddr: nacos服务地址1
          namespace: 命名空间1
          username: nacos用户名1
          password: nacos密码1
          group: nacos组1
        - serverAddr: nacos服务地址2
          namespace: 命名空间2
          username: nacos用户名2
          password: nacos密码2
          group: nacos组2
```

## 3.限流配置

Psf支持动态配置sentinel的流控规则，有flow、system、paramFlow、degrade、authority这6种规则。配置方式和sentinel原生控制台上的配置参数一致，不配置则流控不生效，动态配置动态生效，需要指定路径的流控规则需要和gateway路由规则中的id保持一致，下面是具体的配置：

```yaml
spring:
  cloud:
    sentinel:
      rules:
        flow:
          - resource: routeId01
            count: 1
            grade: 1
            limitApp: default
            strategy: 0
            controlBehavior: 0
          - resource: routeId03
            count: 1
            grade: 1
            limitApp: default
            strategy: 0
            controlBehavior: 0
          - resource: routeId04
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
```

## 4.自定义返回

Psf支持系统配置以下自定义场景返回值，以下优先级递减，匹配第一个断言后则返回，不会匹配后续断言。

- uri
- header
- param
- exception
- default

uri具体配置示例：

```yaml
spring:
  cloud:
    psf:
      predicates:
        uri:
          - path: 请求的uri（支持正则表达式配置）
            return: {"code":"000000","msg":"请求失败","data":["resultData1","resultData2"]}
```

也可以配置为,效果一致

```yaml
spring:
  cloud:
    psf:
      predicates:
        uri:
          - path: 请求的uri（支持正则表达式配置）
            return: 
              code: 000000
              msg: 请求失败
              data:
                - resultData1
                - resultData2
```

以下的断言配置和uri的配置形式一致，同时支持直接配置字符串或者配置对象。下面是其他断言配置示例：

```yaml
spring:
  cloud:
    psf:
      predicates:
        header: #header断言可以配置多个key，需要同时满足才会通过。注：key配置的值可以为数组，且header断言可以配置多组。
          - key1: key1的值
            key2: key2的值
            return: {"code":"000001","msg":"请求头异常","data":["resultData1","resultData2"]}
        param: #param断言可以配置多个key（body体中的不支持），需要同时满足才会通过。注：key配置的值可以为数组，且param断言可以配置多组。
          - key1: key1的值
            key2: key2的值
            return:
              code: 000002
              msg: 请求参数失败
              data:
                - resultData1
                - resultData2
        exception: #异常断言需要配置异常的全路径及返回值。注：exception断言可以配置多组。
          - name: com.psbc.psf.exception.PsfFlowControlException
            return: {"code":"000003","msg":"发生流控","data":["resultData1","resultData2"]}
        defaultReturn: #默认返回断言，当系统非正常运行且无法配置上述断言规则时，则返回默认断言规则内容跟。
          code: 000004
          msg: 系统异常
          data:
            - resultData1
            - resultData2
```