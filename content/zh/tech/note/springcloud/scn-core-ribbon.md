---
title: "SCN核心组件 Ribbon"
date: 2020-10-05T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Ribbon"]
---

*本文源代码下载：[spring-cloud-ribbon.zip](/file/springcloud/spring-cloud-ribbon.zip)*

## 关于负载均衡

## Ribbon应用

不需要引入额外的jar坐标，因为在服务消费者中引入的eureka-client，它会引入Ribbon相关jar

打开消费者模块，代码中使用：

```java
@Bean
    //Ribbon负载均衡
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
```

新增服务提供者api，返回当前实例的端口号，便于观察负载情况：

```java
@RestController
@RequestMapping("/server")
public class ServerPortController {

    @Value("${server.port}")
    private Integer port;

    @GetMapping("/port")
    public Integer getServerPort() {
        return port;
    }

}
```

新增消费者api：

```java
@RestController
@RequestMapping("/server")
public class ServerPortController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/port")
    public Integer getPort() {
        //直接配置服务名即可由Ribbon完成负载均衡
        Integer port = restTemplate
                .getForObject("http://service-resume/server/port", Integer.class);
        return port;
    }
}
```

http文件新增：

```java
GET http://localhost:8090/server/port
Accept: application/json

###
```

请求多次，端口值发生变化，即负载均衡生效

## Ribbon负载均衡策略

Ribbon内置了多种负载均衡策略，顶级接口为com.netflix.loadbalancer.IRule

| 负载均衡策略                                 | 描述                                                         |
| -------------------------------------------- | ------------------------------------------------------------ |
| RoundRobinRule：轮询 策略                    | 默认超过10次获取到的server都不可⽤，会返回⼀个空的server     |
| RandomRule：随机策略                         | 如果随机到的server为null或者不可⽤的话，会while不停的循环选 取 |
| RetryRule：重试策略                          | ⼀定时限内循环重试。默认继承RoundRobinRule，也⽀持⾃定义 注⼊，RetryRule会在每次选取之后，对选举的server进⾏判断， 是否为null，是否alive，并且在500ms内会不停的选取判断。⽽ RoundRobinRule失效的策略是超过10次，RandomRule是没有失 效时间的概念，只要serverList没都挂。 |
| BestAvailableRule：最⼩ 连接数策略           | 遍历serverList，选取出可⽤的且连接数最⼩的⼀个server。该算 法⾥⾯有⼀个LoadBalancerStats的成员变量，会存储所有server 的运⾏状况和连接数。如果选取到的server为null，那么会调⽤ RoundRobinRule重新选取。1（1） 2（1） 3（1） |
| AvailabilityFilteringRule： 可⽤过滤策略     | 扩展了轮询策略，会先通过默认的轮询选取⼀个server，再去判断 该server是否超时可⽤，当前连接数是否超限，都成功再返回。 |
| ZoneAvoidanceRule：区 域权衡策略（默认策略） | 扩展了轮询策略，继承了2个过滤器：ZoneAvoidancePredicate和 AvailabilityPredicate，除了过滤超时和链接数过多的server，还会 过滤掉不符合要求的zone区域⾥⾯的所有节点，AWS --ZONE 在⼀ 个区域/机房内的服务实例中轮询 |

**修改负载均衡策略**

```java
#针对的被调用方微服务名称,不加就是全局生效
service-resume:
  ribbon:
	#负载策略调整
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule 
```

