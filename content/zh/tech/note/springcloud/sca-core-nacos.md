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

   ![416096ac8f092aecc838e9ba19c8f0b.png](https://i.loli.net/2020/12/08/PO1D4kaj5BwGUJd.png)

## Nacos 服务注册中心

### 服务提供者注册到Nacos（改造简历微服务）

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

![f44766641a5f24046c6bf1cb48c455b.png](https://i.loli.net/2020/12/08/qQyseC68bEUzTZP.png)

![001b1f493a6794824cf269953a5c151.png](https://i.loli.net/2020/12/08/BLvZGC1VJYNzq75.png)

保护阈值：可以设置为0-1之间的浮点数，它其实是⼀个比例值（当前服务健康实例数/当前服务总实例数）

场景：
⼀般流程下，nacos是服务注册中心，服务消费者要从nacos获取某⼀个服务的可用实例信息，对于服务实例有健康/不健康状态之分，nacos在返回给消费者实例信息的时候，会返回健康实例。这个时候在⼀些⾼并发、⼤流量场景下会存在⼀定的问题
如果服务A有100个实例，98个实例都不健康了，只有2个实例是健康的，如果nacos只返回这两个健康实例的信息的话，那么后续消费者的请求将全部被分配到这两个实例，流量洪峰到来，2个健康的实例也扛不住了，整个服务A 就扛不住，上游的微服务也会导致崩溃产生雪崩效应。
保护阈值的意义在于
当服务A健康实例数/总实例数 < 保护阈值 的时候，说明健康实例真的不多了，这个时候保护阈值会被触 发（状态true）
nacos将会把该服务所有的实例信息（健康的+不健康的）全部提供给消费者，消费者可能访问到不健康 的实例，请求失败，但这样也比造成雪崩要好，牺牲了⼀些请求，保证了整个系统的⼀个可⽤。 注意：阿里内部在使⽤nacos的时候，也经常调整这个保护阈值参数。

### 服务消费者从Nacos获取服务提供者（改造自动投递微服务）

1. 同服务提供者
2. 测试

### 负载均衡

Nacos客户端引⼊的时候，会关联引⼊Ribbon的依赖包，我们使用OpenFiegn的时候也会引⼊Ribbon 的依赖，Ribbon包括Hystrix都按原来⽅式进⾏配置即可
此处，我们将简历微服务，又启动了⼀个8083端⼝，注册到Nacos上，便于测试负载均衡，我们通过后台也可以看出。

### Nacos 数据模型（领域模型）

Namespace命名空间、Group分组、集群这些都是为了进行归类管理，把服务和配置文件进行归类， 归类之后就可以实现⼀定的效果，比如隔离

比如，对于服务来说，不同命名空间中的服务不能够互相访问调用

![6d492cfd1e863cbf83507a8ac010506.png](https://i.loli.net/2020/12/08/R9XC6Odv2Sz3hnq.png)

**Namespace**：命名空间，对不同的环境进⾏隔离，比如隔离开发环境、测试环境和⽣产环境

**Group**：分组，将若干个服务或者若干个配置集归为⼀组，通常习惯⼀个系统归为⼀个组

**Service**：某一个服务，比如简历服务

**DataId**：配置集或者可以认为是⼀个配置文件

Namespace + Group + Service 如同 Maven 中的GAV坐标，GAV坐标是为了锁定Jar，⼆这⾥是为了 锁定服务

Namespace + Group + DataId 如同 Maven 中的GAV坐标，GAV坐标是为了锁定Jar，⼆这⾥是为了 锁定配置文件

**最佳实践**

Nacos抽象出了Namespace、Group、Service、DataId等概念，具体代表什么取决于怎么用（非常灵活），推荐用法如下

| 概念      | 描述                                              |
| --------- | ------------------------------------------------- |
| Namespace | 代表不同的环境，如开发dev、测试test、生产环境prod |
| Group     | 代表某项目                                        |
| Service   | 某个项目中具体XXX服务                             |
| DataId    | 某个项目中的XXX配置文件                           |

**Nacos服务的分级模型**

![1a8500bc6177bade9cf7610b9dcdbd4.png](https://i.loli.net/2020/12/08/X1EMWpbhaY8VQlP.png)

### Nacos Server 数据持久化

Nacos 默认使用嵌入式数据库进⾏数据存储，它⽀持改为外部Mysql存储

1. 新建数据库 nacos_config，数据库初始化脚本⽂件 ${nacoshome}/conf/nacos-mysql.sql
2. 修改${nacoshome}/conf/application.properties，增加Mysql数据源配置

```xml

#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos-config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user=root
db.password=123456
```

### Nacos Server 集群

1. 安装3个或3个以上的Nacos，复制解压后的nacos⽂件夹，分别命名为nacos-01、nacos-02、nacos-03

2. 修改配置文件

   * 同⼀台机器模拟，将上述三个⽂件夹中application.properties中的server.port分别改为 8848、8849、8850

     同时给当前实例节点绑定ip，因为服务器可能绑定多个ip

     ```yaml
     nacos.inetutils.ip-address=127.0.0.1
     ```

   * 复制⼀份conf/cluster.conf.example⽂件，命名为cluster.conf 在配置⽂件中设置集群中每⼀个节点的信息

     ```yaml
     # 集群节点配置 
     127.0.0.1:8848 
     127.0.0.1:8849 
     127.0.0.1:8850
     ```

3. 分别启动每一个实例（可以批处理脚本完成）

```shell
sh startup.sh -m cluster
```

4. 服务注册

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
```

## Nacos 配置中心

之前：Spring Cloud Config + Bus

1. Github 上添加配置⽂件
2. 创建Config Server 配置中⼼—>从Github上去下载配置信息
3. 具体的微服务(最终使用配置信息的)中配置Config Client—> ConfigServer获取配置信息

有nacos之后，分布式配置就简单很多

Github不需要了（配置信息直接配置在Nacos server中），Bus也不需要了(依然可以完成动态刷新)

1. Nacos Server 添加配置集

   新建命名空间--->新建命名空间下的配置

![3867e6821808191d99e2179ccc4cf06.png](https://i.loli.net/2020/12/08/8zakO4IgmJeAV63.png)

Nacos 服务端已经搭建完毕，那么我们可以在我们的微服务中开启 Nacos 配置管理

2. 添加依赖

```xml
<!--nacos config client 依赖-->
<dependency>
     <groupId>com.alibaba.cloud</groupId>
     <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

3. 微服务中如何锁定 Nacos Server 中的配置文件（dataId）

通过 Namespace + Group + dataId 来锁定配置⽂件，Namespace不指定就默认public，Group不指定 就默认 DEFAULT_GROUP

dataId的完整格式如下：

```yaml
${prefix}-${spring.profile.active}.${file-extension}
```

prefix 默认为 spring.application.name 的值，也可以通过配置项 spring.cloud.nacos.config.prefix来配置。

spring.profile.active 即为当前环境对应的 profile。 注意：当 spring.profile.active 为空时，对应的连接符 - 也将不存在，dataId 的拼接格式变成 ${prefix}.${fileextension}

file-exetension 为配置内容的数据格式，可以通过配置项 spring.cloud.nacos.config.file-extension 来配置。⽬前只⽀持 properties 和 yaml 类型。

```yaml
spring:
  # nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
      config:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
        namespace: 0ac49e33-bc71-47d2-9320-17789cb4adf6
        group: DEFAULT_GROUP
        file-extension: yaml
```

4. 通过 Spring Cloud 原⽣注解 @RefreshScope 实现配置自动更新

```java
@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${chuchin.test}")
    private String test;

    @GetMapping("/viewConfig")
    public String viewConfig() {
        return test;
    }
}
```

​	一个微服务希望从配置中心Nacos Server 中获取多个dataId的配置信息，拓展多个配置：

```yaml
spring:
  # nacos配置
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
      config:
        server-addr: 127.0.0.1:8848,127.0.0.1:8849,127.0.0.1:8850
        namespace: 0ac49e33-bc71-47d2-9320-17789cb4adf6
        group: DEFAULT_GROUP
        file-extension: yaml
        # 根据规则拼接出来的dataId效果：service-resume.yml
        ext-config[0]:
          data-id: abc.yml
          group: DEFAULT_GROUP
          refresh: true
        ext-config[1]:
          data-id: def.yml
          group: DEFAULT_GROUP
          refresh: true
```

优先级：根据规则⽣成的dataId > 扩展的dataId（对于扩展的dataId，[n] n越⼤优先级越⾼）

