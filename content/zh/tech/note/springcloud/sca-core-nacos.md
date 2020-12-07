---
title: "SCA核心组件 Nacos"
date: 2020-10-15T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Nacos"]
---

*本文源代码下载：[spring-cloud-nacos.zip](/file/springcloud/spring-cloud-nacos.zip)*

## Nacos 介绍

Nacos （Dynamic Naming and Configuration Service）是阿里巴巴开源的⼀个针对微服务架构中服务发现、配置管理和服务管理平台。

Nacos就是注册中心+配置中心的组合（Nacos=Eureka+Config+Bus）

官网 [home (nacos.io)](https://nacos.io/zh-cn/)           下载地址 [alibaba/nacos](https://github.com/alibaba/Nacos)

**Nacos功能特性**

* 服务发现与健康检查
* 动态配置管理
* 动态DNS服务
* 服务和元数据管理（管理平台的⻆度，nacos也有⼀个ui页面，可以看到注册的服务及其实例信息 （元数据信息）等），动态的服务权重调整、动态服务优雅下线，都可以去做

## Nacos 单例服务部署

1. 下载解压安装包，执行命令启动（nacos-server-1.3.1.tar.gz）

   Linux/Unix/Mac

```shell
sh startup.sh -m standalone
```

​	如果您使用的是ubuntu系统，或者运行脚本报错提示[[符号找不到，可尝试如下运行：

```shell
bash startup.sh -m standalone
```

​	Windows

```powershell
cmd startup.cmd -m standalone
```

2. 访问nacos管理界面：[http://127.0.0.1:8848/nacos/#/login](http://127.0.0.1:8848/nacos/#/login)（默认端口8848，账号和密码 nacos/nacos）

## Nacos 服务注册中心

#### 服务提供者注册到Nacos（改造简历微服务）

1. 在父pom引入SCA依赖

```xml
<dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>2.1.0.RELEASE</version>
        <scope>import</scope>
      </dependency>
    </dependencies>
</dependencyManagement>
```

2. 在服务提供者工程中引入nacos客户端依赖（注释eureka客户端）

```xml
<dependencies>
        <!--nacos service discovery client依赖-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <!--nacos config client 依赖-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
</dependencies>
```

3. application.yml修改，添加nacos配置信息

```yaml
server:
  port: 8082
spring:
  application:
    name: service-resume
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/springcloud?useUnicode=true&characterEncoding=utf8
    username: root
    password: 123456
  jpa:
    database: MySQL
    show-sql: true
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl  #避免将驼峰命名转换为下划线命名
  # nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: 172.20.158.74:8848
management:
  endpoints:
    web:
      exposure:
        include: "*"

```

4. 启动简历微服务，观察nacos控制台

保护阈值：可以设置为0-1之间的浮点数，它其实是⼀个比例值（当前服务健康实例数/当前服务总实例数）

场景：
⼀般流程下，nacos是服务注册中心，服务消费者要从nacos获取某⼀个服务的可用实例信息，对于服务实例有健康/不健康状态之分，nacos在返回给消费者实例信息的时候，会返回健康实例。这个时候在⼀些⾼并发、⼤流量场景下会存在⼀定的问题
如果服务A有100个实例，98个实例都不健康了，只有2个实例是健康的，如果nacos只返回这两个健康实例的信息的话，那么后续消费者的请求将全部被分配到这两个实例，流量洪峰到来，2个健康的实例也扛不住了，整个服务A 就扛不住，上游的微服务也会导致崩溃产生雪崩效应。
保护阈值的意义在于
当服务A健康实例数/总实例数 < 保护阈值 的时候，说明健康实例真的不多了，这个时候保护阈值会被触 发（状态true）
nacos将会把该服务所有的实例信息（健康的+不健康的）全部提供给消费者，消费者可能访问到不健康 的实例，请求失败，但这样也比造成雪崩要好，牺牲了⼀些请求，保证了整个系统的⼀个可⽤。 注意：阿里内部在使⽤nacos的时候，也经常调整这个保护阈值参数。

#### 服务消费者从Nacos获取服务提供者（改造自动投递微服务）

1. 同服务提供者

2. 测试