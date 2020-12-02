---
title: "SCN核心组件 GateWay"
date: 2020-10-08T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","GateWay"]
---

*本文源代码下载：[spring-cloud-gateway.zip](/file/springcloud/spring-cloud-gateway.zip)*

## GateWay 简介

网关（翻译过来就叫做GateWay）：微服务架构中的重要组成部分。局域网中就有⽹关这个概念，局域⽹接收或者发送数据出去通过这个网关，比如⽤Vmware虚拟机软件 搭建虚拟机集群的时候，往往我们需要选择IP段中的⼀个IP作为网关地址。

Spring Cloud GateWay是Spring Cloud的⼀个全新项⽬，⽬标是取代Netflix Zuul，它基于 Spring5.0+SpringBoot2.0+WebFlux（基于⾼性能的Reactor模式响应式通信框架Netty，异步非阻塞模型）等技术开发，性能⾼于Zuul，官方测试，GateWay是Zuul的1.6倍，旨在为微服务架构提供⼀种简 单有效的统⼀的API路由管理⽅式。
Spring Cloud GateWay不仅提供统⼀的路由方式（反向代理）并且基于 Filter(定义过滤器对请求过滤， 完成⼀些功能) 链的⽅式提供了网关基本的功能，例如：鉴权、流量控制、熔断、路径重写、⽇志监控 等。

## GateWay 核心概念

## GateWay 工作过程

## GateWay 应用

使⽤⽹关对⾃动投递微服务进行代理（添加在它的上游，相当于隐藏了具体微服务的信息，对外暴露的是网关）

1. 创建工程cloud-gateway-9002导入依赖

   *[pom.xml](/file/springcloud/gateway-pom.xml)*

   不要引入web模块，需要引入web-flux模块

2. application.yml配置

   ```
   server:
     port: 9002
   eureka:
     client:
       serviceUrl: # eureka server的路径
         defaultZone: http://a.eureka.server:8761/eureka/,http://b.eureka.server:8762/eureka/ #把 eureka 集群中的所有 url 都填写了进来，也可以只写一台，因为各个 eureka server 可以同步注册表
     instance:
       #使用ip注册，否则会使用主机名注册了（此处考虑到对老版本的兼容，新版本经过实验都是ip）
       prefer-ip-address: true
       #自定义实例显示格式，加上版本号，便于多版本管理，注意是ip-address，早期版本是ipAddress
       instance-id: ${spring.cloud.client.ip-address}:${spring.application.name}:${server.port}:@project.version@
   spring:
     application:
       name: cloud-gateway
     cloud:
       gateway:
         routes: # 路由可以有多个
           - id: service-autodeliver-router # 我们自定义的路由 ID，保持唯一
             uri: http://127.0.0.1:8091/autodeliver  # 目标服务地址  自动投递微服务（部署多实例）  动态路由：uri配置的应该是一个服务名称，而不应该是一个具体的服务实例的地址                                                                # gateway网关从服务注册中心获取实例信息然后负载后路由
             predicates:                                         # 断言：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默 认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。
               - Path=/autodeliver/**
           - id: service-resume-router      # 我们自定义的路由 ID，保持唯一
             uri: http://127.0.0.1:8081/resume       # 目标服务地址
             predicates:                                         # 断言：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默 认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。
               - Path=/resume/**
   
   ```

3. 发送http请求，test.http添加：

   ```
   GET http://localhost:9002/autodeliver/checkState/1545136
   Accept: application/json
   
   ###
   
   GET http://localhost:9002/resume/openstate/1545136
   Accept: application/json
   
   ###
   ```

   ```
   GET http://localhost:9002/autodeliver/checkState/1545136
   
   HTTP/1.1 200 OK
   transfer-encoding: chunked
   Content-Type: application/json;charset=UTF-8
   Date: Wed, 02 Dec 2020 07:46:40 GMT
   
   3
   
   Response code: 200 (OK); Time: 87ms; Content length: 1 bytes
   
   GET http://localhost:9002/resume/openstate/1545136
   
   HTTP/1.1 200 OK
   transfer-encoding: chunked
   Content-Type: application/json;charset=UTF-8
   Date: Wed, 02 Dec 2020 07:46:55 GMT
   
   3
   
   Response code: 200 (OK); Time: 95ms; Content length: 1 bytes
   ```

## GateWay 路由规则

Spring Cloud GateWay 帮我们内置了很多 Predicates功能，实现了各种路由匹配规则（通过 Header、请求参数等作为条件）匹配到对应的路由。

**RoutePredicateFactory路由断言工厂**

| 断言类型                  | 解释                                              |
| ------------------------- | ------------------------------------------------- |
| DateTime 时间类断言       | 根据请求时间在配置时间之前/之后/之间              |
| Cookie 类断言             | 指定Cookie正则匹配指定值                          |
| Header 请求头断言         | 指定Header正则匹配指定值/请求头中是否包含某个属性 |
| Host 请求主机断言         | 请求Host匹配指定值                                |
| Methond 请求方式类断言    | 请求Method匹配指定请求方式                        |
| Path 请求路径类断言       | 请求路径正则匹配指定值                            |
| QueryParam 请求参数类断言 | 查询参数正则匹配指定值                            |
| RemoteAddr远程地址类断言  | 请求远程地址匹配指定值                            |

时间后匹配

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: after_route
          uri: https://example.org
          predicates:
          	- After=2017-01-20T17:42:47.789-07:00[America/Denver]
```

时间点前匹配

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: before_route
          uri: https://example.org
          predicates:
          	- Before=2017-01-20T17:42:47.789-07:00[America/Denver]
```

时间区间匹配

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: between_route
          uri: https://example.org
          predicates:
          	- Between=2017-01-20T17:42:47.789-07:00[America/Denver], 2017-0121T17:42:47.789-07:00[America/Denver]
```

指定Cookie正则匹配指定值

```yam
spring:
  cloud:
    gateway:
      routes:
        - id: cookie_route
          uri: https://example.org
          predicates:
          	- Cookie=chocolate, ch.p
```

指定Header正则匹配指定值

```yam
spring:
  cloud:
    gateway:
      routes:
        - id: header_route
          uri: https://example.org
          predicates:
          	- Header=X-Request-Id, \d+
```

请求Host匹配指定值

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: host_route
          uri: https://example.org
          predicates:
          	- Host=**.somehost.org,**.anotherhost.org
```

请求Method匹配指定请求⽅式

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: method_route
          uri: https://example.org
          predicates:
          	- Method=GET,POST
```

请求路径正则匹配

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: path_route
          uri: https://example.org
          predicates:
          	- Path=/red/{segment},/blue/{segment}
```

请求包含某参数

```yam
spring:
  cloud:
    gateway:
      routes:
        - id: query_route
          uri: https://example.org
          predicates:
          	- Query=green
```

请求包含某参数并且参数值匹配正则表达式

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: query_route
          uri: https://example.org
          predicates:
          	- Query=red, gree.
```

远程地址匹配

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: remoteaddr_route
          uri: https://example.org
          predicates:
          	- RemoteAddr=192.168.1.1/24
```

## GateWay 动态路由

GateWay⽀持⾃动从注册中⼼中获取服务列表并访问，即所谓的动态路由，实现步骤如下

1. pom.xml中添加注册中⼼客户端依赖（因为要获取注册中⼼服务列表，eureka客户端已经引入）

2. 动态路由配置（修改部分）

   ```
   uri: lb://service-autodeliver
   ```

   ```
   uri: lb://service-resume
   ```

   

**注意：动态路由设置时，uri以 lb: //开头（lb代表从注册中心获取服务），后⾯是需要转发到的服务名称**

## GateWay 过滤器

**简介**

从过滤器⽣命周期（影响时机点）的⻆度来说，主要有两个pre和post：

| 生命周期时机点 | 作用                                                         |
| -------------- | ------------------------------------------------------------ |
| pre            | 这种过滤器在请求被路由之前调⽤。我们可利⽤这种过滤器实现身份验证、在集群中 选择 请求的微服务、记录调试信息等。 |
| post           | 这种过滤器在路由到微服务以后执⾏。这种过滤器可⽤来为响应添加标准的 HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。 |

从过滤器类型的⻆度，Spring Cloud GateWay的过滤器分为GateWayFilter和GlobalFilter两种

| 过滤器类型    | 影响范围             |
| ------------- | -------------------- |
| GateWayFilter | 应⽤到单个路由路由上 |
| GlobalFilter  | 应⽤到所有的路由上   |

如Gateway Filter可以去掉url中的占位后转发路由，比如

```yaml
predicates: 
	- Path=/resume/** 
	filters: 
	- StripPrefix=1 # 可以去掉resume之后转发
```

**注意：GlobalFilter全局过滤器是程序员使用⽐较多的过滤器，主要介绍这部分**

**自定义全局过滤器实现IP访问控制（黑白名单）**

请求过来时，判断发送请求的客户端的ip，如果在⿊名单中，拒绝访问

⾃定义GateWay全局过滤器时，我们实现Global Filter接⼝即可，通过全局过滤器可以实现⿊⽩名单、 限流等功能。

```java
/**
 * @Description 自定义全局过滤器，会对所有路由生效
 */
@Slf4j
@Component
public class BlackListFilter implements GlobalFilter, Ordered {

    // 模拟⿊名单（实际可以去数据库或者redis中查询）
    private static List<String> blackList = new ArrayList<>();

    static {
        blackList.add("127.0.0.1");
        // 模拟本机地址
    }

    /**
     * 过滤器方法核心
     * @param exchange 封装了request和response对象上下文
     * @param chain 网关过滤链（包含全局过滤器和单路由过滤器）
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取客户端ip，判断是否在黑名单中，在就拒绝，不在就放行
        //从上下文去除request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //从request对象获取客户端ip
        String clientIp = request.getRemoteAddress().getHostString();
        log.warn(clientIp);
        //去黑名单查询
        if (blackList.contains(clientIp)) {
            // 决绝访问，返回
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 状态码
            log.debug("=====>IP:" + clientIp + " 在⿊名单中，将被拒绝访问！");
            String data = "Request be denied!";
            DataBuffer wrap = response.bufferFactory().wrap(data.getBytes());
            return response.writeWith(Mono.just(wrap));
        }
        // 合法请求，放⾏，执⾏后续的过滤器
        return chain.filter(exchange);
    }

    /**
     * 返回值表示当前过滤器的顺序(优先级)，数值越⼩，优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
```

## GateWay 高可用

⽹关作为⾮常核⼼的⼀个部件，如果挂掉，那么所有请求都可能⽆法路由处理，因此我们需要做 GateWay的高可用。

GateWay的⾼可⽤很简单：可以启动多个GateWay实例来实现⾼可⽤，在GateWay的上游使⽤Nginx 等负载均衡设备进⾏负载转发以达到⾼可⽤的⽬的。

启动多个GateWay实例（假如说两个，⼀个端⼝9002，⼀个端⼝9003），剩下的就是使⽤Nginx等完 成负载代理即可。示例如下：

```
#配置多个GateWay实例
upstream gateway {
	server 127.0.0.1:9002
	server 127.0.0.1:9003
}
location / {
	proxy_pass http://gateway
}
```

