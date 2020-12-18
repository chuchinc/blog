---
title: "SCN核心组件 Config+Bus"
date: 2020-10-09T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Config","Bus"]
---

*本文源代码下载：[spring-cloud-config+bus.zip](/file/springcloud/spring-cloud-config+bus.zip)*

## 分布式配置中心应用场景

## Spring Cloud Config

### Config 简介

### Config 分布式配置应用

**说明：Config Server是集中式的配置服务，用于集中管理应⽤程序各个环境下的配置。 默认使用Git储存配置⽂件内容，也可以SVN。**

比如，我们要对“简历微服务”的application.yml进⾏管理（区分开发环境、测试环境、⽣产环境）

1. 上传码云，创建项目cloud-config-repo

2. 上传yml配置文件，命名规则如下：

   {application}-{profile}.yml 或者 {application}-{profile}.properties 其中，application为应⽤名称，profile指的是环境（⽤于区分开发环境，测试环境、⽣产环境等）
   示例：lagou-service-resume-dev.yml、lagou-service-resume-test.yml、lagou-service-resumeprod.yml

   仓库文件路径为 /config/XXXXX.yml

3. 构建Config Server 统一配置中心，新建子模块 cloud-config-server-9006

```xml
<parent>
    <artifactId>spring-cloud</artifactId>
    <groupId>cn.chuchin</groupId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <modelVersion>4.0.0</modelVersion>

  <artifactId>cloud-config-server-9006</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
  </dependencies>
```

4. 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigServer
public class CloudConfig9006 {

    public static void main(String[] args) {
        SpringApplication.run(CloudConfig9006.class, args);
    }
}
```

5. 配置

```xml
server:
  port: 9006
#注册到Eureka服务中心
eureka:
  client:
    service-url:
      # 注册到集群，就把多个Eurekaserver地址使用逗号连接起来即可；注册到单实例（非集群模式），那就写一个就ok
      defaultZone: http://a.eureka.server:8761/eureka,http://b.eureka.server:8762/eureka
  instance:
    prefer-ip-address: true  #服务实例中显示ip，而不是显示主机名（兼容老的eureka版本）
    # 实例名称： 192.168.1.103:lagou-service-resume:8080，我们可以自定义它
    instance-id: ${spring.cloud.client.ip-address}:${spring.application.name}:${server.port}:@project.version@
spring:
  application:
    name: cloud-config-server
  cloud:
    config:
      server:
        git:
          uri: https://gitee.com/chuchin/cloud-config-repo.git #配置git服务地址
          username: XXXXXX #配置git用户名
          password: XXXXXX #配置git密码
		  # 配置文件路径，仓库目录为根路径
          search-paths:
            - config
      # 读取分支
      label: master
# springboot中暴露健康检查等断点接口
management:
  endpoints:
    web:
      exposure:
        include: "*"
  # 暴露健康接口的细节
  endpoint:
    health:
      show-details: always
```

测试访问：[localhost:9006/master/service-resume-dev.yml](http://localhost:9006/master/service-resume-dev.yml)，可以查看到配置内容

6. 构建Client客户端（在已有简历微服务基础上），添加依赖

```xml
spring:
  cloud:
    config:
      name: service-resume
      profile: dev
      label: master
      uri: http://localhost:9006
```

### Config配置手动刷新

不⽤重启微服务，只需要⼿动的做⼀些其他的操作（访问⼀个地址/refresh）刷新，之后再访问即可
此时，客户端取到了配置中⼼的值，但当我们修改GitHub上⾯的值时，服务端（Config Server）能实 时获取最新的值，但客户端（Config Client）读的是缓存，⽆法实时获取最新值。Spring Cloud已 经为 我们解决了这个问题，那就是客户端使⽤post去触发refresh，获取最新数据。

1. Client客户端添加依赖springboot-starter-actuator（已添加）
2. Client客户端bootstrap.yml中添加配置（暴露通信端点）

```xml
management:
  endpoints:
    web:
      exposure:
        include: refresh

#也可以暴露所有的端口
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

3. Client客户端使用带配置信息的类上添加@RefreshScope

4. 手动向Client客户端发起POST请求，[localhost:8080/actuator/refresh](http://localhost:8080/actuator/refresh)刷新配置信息

   **手动刷新方式避免了服务重启（流程：Git改配置-->for循环脚本手动刷新每个微服务）**

   那么，可否使用广播机制，一次通知，处处生效，方便大范围配置刷新

### Config配置自动更新

实现一次通知处处生效，在做分布式配置，可以用zk（存储+通知），zk中数据变更，可以通知各个监听的客户端，客户 端收到通知之后可以做出相应的操作（内存级别的数据直接⽣效，对于数据库连接信息、连接池等信息变化更新的，那么会在通知逻辑中进⾏处理，比如重新初始化连接池）。在微服务架构中，我们可以结合消息总线（Bus）实现分布式配置的自动更新（Spring Cloud + Spring Cloud Bus）

#### 消息总线Bus

所谓消息总线Bus，即我们经常会使用MQ消息代理构建⼀个共⽤的Topic，通过这个Topic连接各个微服务实例，MQ⼴播的消息会被所有在注册中⼼的微服务实例监听和消费。**换言之就是通过⼀个主题连接 各个微服务，打通脉络。**

Spring Cloud Bus（基于MQ的，⽀持RabbitMq/Kafka） 是Spring Cloud中的消息总线⽅案，Spring Cloud Config + Spring Cloud Bus 结合可以实现配置信息的自动更新。

#### Spring Cloud Config + Spring Cloud Bus 实现自动刷新

MQ消息代理，我们还选择使⽤RabbitMQ，ConfigServer和ConfigClient都添加都消息总线的⽀持以及 与RabbitMq的连接信息

1. Config Sever 服务端添加消息总线支持

```xml
<dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

2. ConfigServer 添加配置

```xml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```

3. 微服务暴露端口

```xml
management:
  endpoints:
    web:
      exposure:
        include: bus-refresh
#建议暴露所有端口
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

4. 重启各个服务之后，更改配置，向配置中心服务端发送get请求[localhost:9006/actuator/bus-refresh](http://localhost:9006/actuator/bus-refresh)，各个客户端配置即可自动刷新

   在广播模式下实现了⼀次请求，处处更新，如果我只想定向更新呢？ 在发起刷新请求的时候[localhost:9006/actuator/bus-refresh/service-resume:8080](http://localhost:9006/actuator/bus-refresh/service-resume:8080)即为最后⾯跟上要定向刷新的实例的服务名:端⼝号即可