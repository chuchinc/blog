---
title: "链路追踪 Sleuth+Zipkin"
date: 2020-10-13T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Sleuth","Zipkin"]
---

*本文源代码下载：[spring-cloud-sleuth+zipkin.zip](/file/springcloud/spring-cloud-sleuth+zipkin.zip)*

## 分布式链路追踪技术适用场景

### 场景描述

为了支撑⽇益增⻓的庞大业务量，我们会使⽤微服务架构设计我们的系统，使得我们的系统不仅能够通过集群部署抵挡流量的冲击，⼜能根据业务进行灵活的扩展。

那么，在微服务架构下，⼀次请求少则经过三四次服务调⽤完成，多则跨越几十个甚⾄是上百个服务节点。那么问题接踵而来：

1. 如何动态展示服务的调用链路？（比如A服务调用了哪些服务，依赖关系）
2. 如何分析服务调⽤链路中的瓶颈节点并对其进行调优？(⽐如A—>B—>C，C服务处理时间特别长)
3. 如何快速进行服务链路的故障发现？

### 分布式链路追踪技术

如果我们在⼀个请求的调用处理过程中，在各个链路节点都能够记录下⽇志，并最终将⽇志进行集中可视化展示，那么我们想监控调用链路中的⼀些指标就有希望了~~~⽐如，请求到达哪个服务实 例？请求被处理的状态怎样？处理耗时怎样？这些都能够分析出来了。

分布式环境下基于这种想法实现的监控技术就是就是分布式链路追踪（全链路追踪）。

### 技术方案

分布式链路追踪技术已然成熟，产品也不少，国内外都有，比如：

* Spring Cloud Sleuth + Twitter Zipkin
* 阿⾥巴巴的“鹰眼”
* ⼤众点评的“CAT”
* 美团的“Mtrace”
* 京东的“Hydra”
* 新浪的“Watchman”

另外还有最近也被提到很多的 Apache Skywalking

## 分布式链路追踪技术核心思想

本质：记录日志，作为⼀个完整的技术，分布式链路追踪也有自己的理论和概念

**Trace**：服务追踪的追踪单元是从客户发起请求（request）抵达被追踪系统的边界开始，到被追踪系统 向客户返回响应（response）为止的过程

**Trace ID**：了实现请求跟踪，当请求发送到分布式系统的入口端点时，只需要服务跟踪框架为该请求 创建⼀个唯⼀的跟踪标识Trace ID，同时在分布式系统内部流转的时候，框架始终保持该唯⼀标识，直到返回给请求方

⼀个Trace由⼀个或者多个Span组成，每⼀个Span都有⼀个SpanId，Span中会记录TraceId，同时还有⼀个叫做ParentId，指向了另外⼀个Span的SpanId，表明父子关系，其实本质表达了依赖关系

**Span ID**：为了统计各处理单元的时间延迟，当请求到达各个服务组件时，也是通过⼀个唯⼀标识Span ID来标记它的开始，具体过程以及结束。对每⼀个Span来说，它必须有开始和结束两个节点，通过记录 开始Span和结束Span的时间戳，就能统计出该Span的时间延迟，除了时间戳记录之外，它还可以包含 ⼀些其他元数据，比如时间名称、请求信息等。

每⼀个Span都会有⼀个唯⼀跟踪标识 Span ID,若⼲个有序的 span 就组成了⼀个 trace。

Span可以认为是⼀个⽇志数据结构，在⼀些特殊的时机点会记录了⼀些⽇志信息，比如有时间戳、 spanId、TraceId，parentIde等，Span中也抽象出了另外⼀个概念，叫做事件，核⼼事件如下：

* CS ：client send/start 客户端/消费者发出⼀个请求，描述的是⼀个span开始
* SR: server received/start 服务端/⽣产者接收请求 SR-CS属于请求发送的网络延迟
* SS: server send/finish 服务端/⽣产者发送应答 SS-SR属于服务端消耗时间
* CR：client received/finished 客户端/消费者接收应答 CR-SS表示回复需要的时间(响应的网络延迟)

**Spring Cloud Sleuth** （追踪服务框架）可以追踪服务之间的调⽤，Sleuth可以记录⼀个服务请求经过哪些服务、服务处理时长等，根据这些，我们能够理清各微服务间的调⽤关系及进⾏问题追踪分析。

* 耗时分析：通过 Sleuth 了解采样请求的耗时，分析服务性能问题（哪些服务调用比较耗时）
* 链路优化：发现频繁调⽤的服务，针对性优化等

Sleuth就是通过记录⽇志的⽅式来记录踪迹数据的

我们往往把Spring Cloud Sleuth 和 Zipkin ⼀起使⽤，把 Sleuth 的数据信息发送给 Zipkin 进行聚合，利⽤ Zipkin 存储并展示数据。

## Sleuth追踪日志

1. 每一个需要被追踪的微服务工程都需要引入

```xml
<!--链路追踪-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>
```

2. 每一个微服务都修改application.yml配置文件，添加日志级别

```yaml
#分布式链路追踪 
logging: 
  level:
	org.springframework.web.servlet.DispatcherServlet: debug
	org.springframework.cloud.sleuth: debug
```

请求到来时，我们在控制台可以观察到 Sleuth 输出的⽇志（全局 TraceId、SpanId等）。

这样的⽇志⾸先不容易阅读观察，另外⽇志分散在各个微服务服务器上，接下来我们使⽤Zipkin统⼀聚合轨迹⽇志并进行存储展示。

## Zipkin统一聚合日志

结合 Zipkin 展示追踪数据

Zipkin 包括Zipkin Server和 Zipkin Client两部分，Zipkin Server是⼀个单独的服务，Zipkin Client就是具体的微服务

1. Zipkin Server 构建，新建子模块 cloud-zipkin-server-9411

```xml
<dependencies>
<!--    zipkin server-->
    <dependency>
      <groupId>io.zipkin.java</groupId>
      <artifactId>zipkin-server</artifactId>
      <version>2.12.3</version>
      <exclusions>
<!--        排除掉log4j的传递依赖，避免和Springboot依赖的日志组件冲突-->
        <exclusion>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-log4j2</artifactId>
        </exclusion>
      </exclusions>
    </dependency>

<!--    zipkin server ui 界面依赖坐标-->
    <dependency>
      <groupId>io.zipkin.java</groupId>
      <artifactId>zipkin-autoconfigure-ui</artifactId>
      <version>2.12.3</version>
    </dependency>
</dependencies>
```

```java
@SpringBootApplication
@EnableZipkinServer
public class ZipkinServerApplication9411 {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication9411.class, args);
    }
}
```

```yaml
server:
  port: 9411
management:
  metrics:
    web:
      server:
        auto-time-requests: false #关闭自动检测请求
```

2. Zipkin Client 构建（在具体微服务中修改）

```xml
<dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

```xml
spring:
  zipkin:
    base-url: http://127.0.0.1:9411
    sender:
      # web 客户端将踪迹日志数据通过网络请求的方式传送到服务端，另外还有配置
      # kafka/rabbit 客户端将踪迹日志数据传递到mq进行中转
      type: web
  sleuth:
    sampler:
      # 采样率 1 代表100%全部采集 ，默认0.1 代表10% 的请求踪迹数据会被采集
      # 生产环境下，请求量⾮常⼤，没有必要所有请求的踪迹数据都采集分析，对于网络包括
      # server端压⼒都是⽐较⼤的，可以配置采样率采集⼀定比例的请求的踪迹数据进⾏分析即可
      probability: 1
```

​		另外，对于log日志，依然开启debug状态，Zipkin Server 页⾯⽅便我们查看服务调⽤依赖关系及⼀些性能指标和异常信息

3. 追踪数据持久化到MySQL

   mysql中创建数据库zipkin，并执行SQL语句（官方提供）[zipkin.sql](/file/springcloud/zipkin.sql)

   ```xml
       <dependency>
         <groupId>io.zipkin.java</groupId>
         <artifactId>zipkin-autoconfigure-storage-mysql</artifactId>
         <version>2.12.3</version>
       </dependency>
       <dependency>
         <groupId>mysql</groupId>
         <artifactId>mysql-connector-java</artifactId>
       </dependency>
       <dependency>
         <groupId>com.alibaba</groupId>
         <artifactId>druid-spring-boot-starter</artifactId>
         <version>1.1.10</version>
       </dependency>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-tx</artifactId>
       </dependency>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-jdbc</artifactId>
       </dependency>
   ```

   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/zipkin? useUnicode=true&characterEncoding=utf8&useSSL=false&allowMultiQueries=true
       username: root
       password: 123456
       druid:
         initial-size: 10
         min-idle: 10
         max-active: 30
         max-wait: 50000
   #指定zipkin持久化介质为MySQL
   zipkin:
     storage:
       type: mysql
   ```

   启动类注入事务管理器

   ```java
   	@Bean
       public PlatformTransactionManager txManager(DataSource dataSource) {
           return new DataSourceTransactionManager(dataSource);
       }
   ```

   访问 [http://127.0.0.1:9411/zipkin/](http://127.0.0.1:9411/zipkin/)

   