---
title: "SCA核心组件 Sentinel"
date: 2020-10-16T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Sentinel"]
---

*本文源代码下载：[spring-cloud-sentinel.zip](/file/springcloud/spring-cloud-sentinel.zip)*

## Sentinel 介绍

Sentinel是⼀个⾯向云原⽣微服务的流量控制、熔断降级组件。替代Hystrix，针对问题：服务雪崩、服务降级、服务熔断、服务限流

**Hystrix：**

服务消费者（自动投递微服务）—>调⽤服务提供者（简历微服务）

在调用方引入Hystrix—> 单独搞了⼀个Dashboard项⽬—>Turbine

1. 自己搭建监控平台 dashboard
2. 没有提供UI界面进⾏服务熔断、服务降级等配置（而是写代码，入侵了我们源程序环境）

**Sentinel：**

1. 独立可部署Dashboard控制台组件
2. 减少代码开发，通过UI界⾯配置即可完成细粒度控制（自动投递微服务）

**Sentinel 分为两个部分:**

* 核心库：（Java客户端）不依赖任何框架/库，能够运⾏于所有 Java 运⾏时环境，同时对 Dubbo / Spring Cloud 等框架也有较好的⽀持
* 控制台：（Dashboard）基于 Spring Boot 开发，打包后可以直接运⾏，不需要额外的 Tomcat 等 应⽤容器

**Sentinel 具有以下特征:**

* 丰富的应⽤场景：Sentinel 承接了阿⾥巴巴近 10 年的双⼗⼀⼤促流量的核⼼场景，例如秒杀（即突发流量控制在系统容量可以承受的范围）、消息削峰填谷、集群流量控制、实时熔断下游不可用应用等。
* 完备的实时监控：Sentinel 同时提供实时的监控功能。您可以在控制台中看到接入应用的单台机器 秒级数据，甚至 500 台以下规模的集群的汇总运⾏情况。
* ⼴泛的开源生态：Sentinel 提供开箱即⽤的与其它开源框架/库的整合模块，例如与 Spring Cloud、Dubbo的整合。您只需要引入相应的依赖并进⾏简单的配置即可快速地接⼊ Sentinel。
* 完善的 SPI 扩展点：Sentinel 提供简单易用、完善的 SPI 扩展接⼝。您可以通过实现扩展接⼝来快速地定制逻辑。例如定制规则管理、适配动态数据源等。

Sentinel特性（来自官网）

![up-2ba39572a1e10a01ca50460f470f599905e.png](https://i.loli.net/2020/12/08/8d4ltxN5aV216IW.png)

Sentinel 生态（来自官网）

![up-f82e88eac21b9eb66b9a79af0ab0afe844b.png](https://i.loli.net/2020/12/08/SJXkMrwW7D6BHqu.png)

## Sentinel 部署

下载地址：[https://github.com/alibaba/Sentinel/releases](https://github.com/alibaba/Sentinel/releases)  版本为V1.8.0

启动：java -jar sentinel-dashboard-1.8.0.jar &

访问dashboard: http://localhost:8080/

用户名/密码 sentinel/sentinel

## 服务改造

在我们已有的业务场景中，“⾃动投递微服务”调⽤了“简历微服务”，我们在自动动投递微服务进⾏的熔断 降级等控制，那么接下来我们改造⾃动投递微服务，引入Sentinel核⼼包。
为了不污染之前的代码，复制⼀个⾃动投递微服务 service-autodeliver-8098-sentinel

1. pom.xml引入依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel</artifactId>
</dependency>
```

2. application.yml (配置sentinel dashboard，暴露端点依然要有，删除原有hystrix配置，删除原有OpenFeign的降级配置)

```yaml
server:
  port: 8098
spring:
  application:
    name: service-autodeliver
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850

    sentinel:
      transport:
        dashboard: 127.0.0.1:8080 # sentinel dashboard/console 地址
        port: 8719   #  sentinel会在该端口启动http server，那么这样的话，控制台定义的一些限流等规则才能发送传递过来，
                      #如果8719端口被占用，那么会依次+1
management:
  endpoints:
    web:
      exposure:
        include: "*"
  # 暴露健康接口的细节
  endpoint:
    health:
      show-details: always
#针对的被调用方微服务名称,不加就是全局生效
lagou-service-resume:
  ribbon:
    #请求连接超时时间
    ConnectTimeout: 2000
    #请求处理超时时间
    ##########################################Feign超时时长设置
    ReadTimeout: 3000
    #对所有操作都进行重试
    OkToRetryOnAllOperations: true
    ####根据如上配置，当访问到故障请求的时候，它会再尝试访问一次当前实例（次数由MaxAutoRetries配置），
    ####如果不行，就换一个实例进行访问，如果还不行，再换一次实例访问（更换次数由MaxAutoRetriesNextServer配置），
    ####如果依然不行，返回失败信息。
    MaxAutoRetries: 0 #对当前选中实例重试次数，不包括第一次调用
    MaxAutoRetriesNextServer: 0 #切换实例的重试次数
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule #负载策略调整
logging:
  level:
    # Feign日志只会对日志级别为debug的做出响应
    com.lagou.edu.controller.service.ResumeServiceFeignClient: debug
```

3. 上述配置之后，启动⾃动投递微服务，使⽤ Sentinel 监控⾃动投递微服务，此时我们发现控制台没有任何变化，因为懒加载，我们只需要发起⼀次请求触发即可

![76accaef2bdc1ddd0ac844bcb37630e.png](https://i.loli.net/2020/12/08/BRZcEDqLxCOpoXa.png)

## Sentinel 关键概念

| 概念名称 | 概念描述                                                     |
| -------- | ------------------------------------------------------------ |
| 资源     | 它可以是 Java 应⽤程序中的任何内容，例如，由应⽤程序提供的服务，或由应⽤程序调⽤ 的其它应⽤提供的服务，甚⾄可以是⼀段代码。我们请求的API接⼝就是资源 |
| 规则     | 围绕资源的实时状态设定的规则，可以包括流量控制规则、熔断降级规则以及系统保护规 则。所有规则可以动态实时调整。 |

## Sentinel 流控规则模块

系统并发能⼒有限，比如系统A的QPS⽀持1个，如果太多请求过来，那么A就应该进行流量控制了，比如其他请求直接拒绝

![87aa4d1fb2b5dbd2923bea2c69a52f7.png](https://i.loli.net/2020/12/08/sK2PjmEqpGRkMg3.png)

**资源名**：默认请求路径

**针对来源**：Sentinel可以针对调⽤者进⾏限流，填写微服务名称，默认default（不区分来源）

**阈值类型/单机阈值**

QPS：（每秒钟请求数量）当调⽤该资源的QPS达到阈值时进⾏限流

线程数：当调⽤该资源的线程数达到阈值的时候进⾏限流（线程处理请求的时候，如果说业务逻辑执⾏ 时间很⻓，流量洪峰来临时，会耗费很多线程资源，这些线程资源会堆积，最终可能造成服务不可⽤， 进⼀步上游服务不可⽤，最终可能服务雪崩）

**是否集群**：是否集群限流

**流控模式**：
直接：资源调⽤达到限流条件时，直接限流

关联：关联的资源调⽤达到阈值时候限流自己 

链路：只记录指定链路上的流量

**流控效果**：
快速失败：直接失败，抛出异常
Warm Up：根据冷加载因⼦（默认3）的值，从阈值/冷加载因⼦，经过预热时⻓，才达到设置的QPS阈 值
排队等待：匀速排队，让请求匀速通过，阈值类型必须设置为QPS，否则⽆效

**流控模式之关联限流**：
关联的资源调用达到阈值时候限流⾃⼰，比如用户注册接⼝，需要调⽤身份证校验接口（往往身份证校验接⼝），如果身份证校验接⼝请求达到阈值，使用关联，可以对用户注册接⼝进⾏限流。

![7a9ec0d08e146119eee0bcac2f5bca6.png](https://i.loli.net/2020/12/08/TOF42v1oMVPabuf.png)

```java
@RestController
@RequestMapping("/user")
public class UserController {
    /**
     * 用户注册接口
     * @return
     */
    @GetMapping("/register")
    public String register() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:MM:ss");
        System.out.println(simpleDateFormat.format(new Date()) + " Register success!");
        return "Register success!";
    }
    /**
     *  验证注册身份证接口（需要调用公安户籍资源）
     * @return
     */
    @GetMapping("/validateID")
    public String findResumeOpenState() {
        System.out.println("validateID");
        return "ValidateID success!";
    }
}
```

模拟密集式请求/user/validateID验证接口，我们会发现/user/register接⼝也被限流了

**流控模式之链路限流**

链路指的是请求链路（调⽤链）

链路模式下会控制该资源所在的调用链路入口的流量。需要在规则中配置入口资源，即该调用链路入口的上下⽂名称。

⼀棵典型的调⽤树如下图所示：（阿⾥云提供）

```
									machine-root
									/			\
								   /             \
							Entrance1		Entrance2
                            	/					\
                        DefaultNode(nodeA)   DefaultNode(nodeA)
```

上图中来⾃入口 Entrance1 和 Entrance2 的请求都调用到了资源 NodeA，Sentinel 允许只根据某 个调⽤⼊⼝的统计信息对资源限流。⽐如链路模式下设置⼊⼝资源为 Entrance1 来表示只有从入口哦Entrance1 的调用才会记录到 NodeA 的限流统计当中，而不关心经 Entrance2 到来的调⽤。

![426ae23f80708c3517ab65d72b284ae.png](https://i.loli.net/2020/12/08/H5awLufM71mnKOp.png)

**流控效果之Warm up**

当系统长期处于空闲的情况下，当流量突然增加时，直接把系统拉升到高水位可能瞬间把系统压垮，比如如电商网站的秒杀模块。
通过 Warm Up 模式（预热模式），让通过的流量缓慢增加，经过设置的预热时间以后，到达系统处理请求速率的设定值。
Warm Up 模式默认会从设置的 QPS 阈值的 1/3 开始慢慢往上增加⾄ QPS 设置值。

![6ed7e1f1f02ab7c4a8ef849ce74d045.png](https://i.loli.net/2020/12/08/7qd3FPHSvIGOEnw.png)

**流控效果之排队等待**

排队等待模式下会严格控制请求通过的间隔时间，即请求会匀速通过，允许部分请求排队等待，通常用于消息队列削峰填谷等场景。需设置具体的超时时间，当计算的等待时间超过超时时间时请求就会被拒 绝。
很多流量过来了，并不是直接拒绝请求，而是请求进行排队，⼀个⼀个匀速通过（处理），请求能等就等着被处理，不能等（等待时间>超时时间）就会被拒绝
例如，QPS 配置为 5，则代表请求每 200 ms 才能通过⼀个，多出的请求将排队等待通过。超时时间代 表最⼤排队时间，超出最⼤排队时间的请求将会直接被拒绝。排队等待模式下，QPS 设置值不要超过 1000（请求间隔 1 ms）。

## Sentinel 降级规则模块

流控是对外部来的⼤流量进⾏控制，熔断降级的视角是对内部问题进⾏处理。

Sentinel 降级会在调⽤链路中某个资源出现不稳定状态时（例如调用超时或异常⽐例升⾼），对这个资源的调⽤进⾏限制，让请求快速失败，避免影响到其它的资源⽽导致级联错误。当资源被降级后，在接 下来的降级时间窗⼝之内，对该资源的调用都自动熔断.

这⾥的降级其实是Hystrix中的熔断，工作流程如下：

![6d1c48457aa5c15a6a593cab03ca8e9.png](https://i.loli.net/2020/12/08/wRnGchNq2Txki6t.png)

**策略**

Sentinel不会像Hystrix那样放过⼀个请求尝试⾃我修复，就是明明确确按照时间窗⼝来，熔断触发后， 时间窗⼝内拒绝请求，时间窗口后就恢复。

* RT（平均响应时间 ）

  当 1s 内持续进⼊ >=5 个请求，平均响应时间超过阈值（以 ms 为单位），那么在接下的时间窗⼝ （以 s 为单位）之内，对这个⽅法的调⽤都会⾃动地熔断（抛出 DegradeException）。注意 Sentinel 默认统计的 RT 上限是 4900 ms，超出此阈值的都会算作 4900 ms，若需要变更此上限 可以通过启动配置项 -Dcsp.sentinel.statistic.max.rt=xxx 来配置。

  ![a50931d37d15d5e5e0feb49c360cb29.png](https://i.loli.net/2020/12/08/pJMzePKlA6I8cBf.png)

* 异常比例

  当资源的每秒请求量 >= 5，并且每秒异常总数占通过量的比值超过阈值之后，资源进⼊降级状态，即在接下的时间窗口（以 s 为单位）之内，对这个方法的调用都会⾃动地返回。异常比率的阈 值范围是 [0.0, 1.0]，代表 0% - 100%。

  ![a506e5aa440a7f95a4bc51bc397e049.png](https://i.loli.net/2020/12/08/xOce7w869JInMNu.png)

* 异常数

  当资源近 1 分钟的异常数目超过阈值之后会进⾏熔断。注意由于统计时间窗口是分钟级别的，若 timeWindow 小于 60s，则结束熔断状态后仍可能再进⼊熔断状态。
  时间窗口 >= 60s

  ![d373afab0152ed39247327aa9b37b77.png](https://i.loli.net/2020/12/08/8dBy5CMtrcx32ZL.png)

## Sentinel 其他模块

参考 [wiki](https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E9%A1%B5)

## Sentinel 自定义兜底逻辑

@SentinelResource注解类似于Hystrix中的@HystrixCommand注解
@SentinelResource注解中有两个属性需要我们进⾏区分，blockHandler属性⽤来指定不满⾜Sentinel 规则的降级兜底⽅法，fallback属性⽤于指定Java运⾏时异常兜底⽅法

1. 在API接口资源处配置

```java
/**
     * @SentinelResource
     *  value： 定义资源名
     *  blockHandlerClass：指定Sentinel规则异常兜底逻辑所在的class类
     *  blockHandler：指定Sentinel规则异常兜底逻辑具体哪个方法
     *  fallbackClass：指定Java运⾏时异常兜底逻辑所在class类
     *  fallback：指定Java运⾏时异常兜底逻辑具体哪个⽅法
     * @param userId
     * @return
     */
    @GetMapping("/checkState/{userId}")
    // @SentinelResource注解类似于Hystrix中的@HystrixCommand注解
    @SentinelResource(value = "findResumeOpenState",blockHandlerClass = SentinelHandlersClass.class,
            blockHandler = "handleException",fallbackClass = SentinelHandlersClass.class,fallback = "handleError")
    public Integer findResumeOpenState(@PathVariable Long userId) {
        // 模拟降级：
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        // 模拟降级：异常比例
        //int i = 1/0;
        Integer defaultResumeState = resumeServiceFeignClient.findDefaultResumeState(userId);
        return defaultResumeState;
    }
```

2. 自定义兜底类

```java
public class SentinelHandlersClass {

    // 整体要求和当时Hystrix一样，这里还需要在形参中添加BlockException参数，用于接收异常
    // 注意：方法是静态的
    public static Integer handleException(Long userId, BlockException blockException) {
        return -100;
    }

    public static Integer handleError(Long userId) {
        return -500;
    }

}
```

## 基于 Nacos 实现 Sentinel 规则持久化

⽬前，Sentinel Dashboard中添加的规则数据存储在内存，微服务停掉规则数据就消失，在生产环境下不合适。我们可以将Sentinel规则数据持久化到Nacos配置中心，让微服务从Nacos获取规则数据。

![9b52707e5d0b1d6eeffd197582cd4e4.png](https://i.loli.net/2020/12/08/G1tWbASXYkInVlw.png)

1. 自动投递微服务的pom.xml中添加依赖

```xml
<!-- Sentinel支持采用 Nacos 作为规则配置数据源，引入该适配依赖 -->
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-datasource-nacos</artifactId>
        </dependency>
```

2. ⾃动投递微服务的application.yml中配置Nacos数据源

```yaml
spring:
  application:
    name: service-autodeliver
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
    sentinel:
      transport:
        dashboard: 127.0.0.1:8080 # sentinel dashboard/console 地址
        port: 8719   #  sentinel会在该端口启动http server，那么这样的话，控制台定义的一些限流等规则才能发送传递过来，
                      #如果8719端口被占用，那么会依次+1
        # Sentinel Nacos数据源配置，Nacos中的规则会⾃动同步到sentinel控制台的流控规则中
      datasource:
        #此处的flow为自定义数据源名
        flow:
          nacos:
            server-addr: ${spring.cloud.nacos.discovery.server-addr}
            data-id: ${spring.application.name}-flow-rules
            groupId: DEFAULT_GROUP
            data-type: json
            rule-type: flow # 类型来自RuleType类
        degrade:
          nacos:
            server-addr: ${spring.cloud.nacos.discovery.server-addr}
            data-id: ${spring.application.name}-degrade-rules
            groupId: DEFAULT_GROUP
            data-type: json 
            rule-type: degrade # 类型来⾃RuleType类
```

3. Nacos Server中添加对应规则配置集（public命名空间—>DEFAULT_GROUP中添加） 

   **新建流控规则配置集service-autodeliver-flow-rules**

   ```json
   [ {
   "resource":"findResumeOpenState", 
   "limitApp":"default", 
   "grade":1, 
   "count":1, 
   "strategy":0, 
   "controlBehavior":0, 
   "clusterMode":false
   } ]
   ```

   所有属性来自源码FlowRule类

   * resource：资源名称
   * limitApp：来源应⽤
   * grade：阈值类型 0 线程数 1 QPS
   * count：单机阈值
   * strategy：流控模式，0 直接 1 关联 2 链路
   * controlBehavior：流控效果，0 快速失败 1 Warm Up 2 排队等待
   * clusterMode：true/false 是否集群

   **新建降级规则配置集 service-autodeliver-degrade-rules**

   ```json
   [ {
   "resource":"findResumeOpenState", 
   "grade":2, 
   "count":1, 
   "timeWindow":5
   } ]
   ```

   所有属性来⾃源码DegradeRule类：

   * resource：资源名称
   * grade：降级策略 0 RT 1 异常⽐例 2 异常数
   * count：阈值
   * timeWindow：时间窗

   **注意**

   1. ⼀个资源可以同时有多个限流规则和降级规则，所以配置集中是⼀个json数组
   2. Sentinel控制台中修改规则，仅是内存中生效，不会修改Nacos中的配置值，重启后恢复原来的值； Nacos控制台中修改规则，不仅内存中生效，Nacos中持久化规则也⽣效，重启后规则依然保持