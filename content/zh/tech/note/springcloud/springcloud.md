---
title: "SCN核心组件 Eureka"
date: 2020-10-04T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Eureka"]
---

## Eureka 服务注册中心

## 关于注册中心

### 注册中心一般原理

### 主流注册中心对比

## 服务注册中心组件 Eureka

## Eureka 应用及高可用集群

### 搭建单例Eureka Server服务注册中心

1. 新建子模块 cloud-eureka-server-8761

2. 父工程中引入依赖管理，指定版本为Greenwich.RELEASE

   ```xml
   <dependencyManagement>
       <dependencies>
         <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-dependencies</artifactId>
           <version>Greenwich.RELEASE</version>
           <type>pom</type>
           <scope>import</scope>
         </dependency>
       </dependencies>
     </dependencyManagement>
   ```

   并手动引入jaxb的jar，因为JDK9之后默认没有加载该模块，EurekaServer使用到

   ```xml
   <!--引⼊Jaxb，开始-->
       <dependency>
         <groupId>com.sun.xml.bind</groupId>
         <artifactId>jaxb-core</artifactId>
         <version>2.2.11</version>
       </dependency>
       <dependency>
         <groupId>javax.xml.bind</groupId>
         <artifactId>jaxb-api</artifactId>
       </dependency>
       <dependency>
         <groupId>com.sun.xml.bind</groupId>
         <artifactId>jaxb-impl</artifactId>
         <version>2.2.11</version>
       </dependency>
       <dependency>
         <groupId>org.glassfish.jaxb</groupId>
         <artifactId>jaxb-runtime</artifactId>
         <version>2.2.10-b140310.1920</version>
       </dependency>
       <dependency>
         <groupId>javax.activation</groupId>
         <artifactId>activation</artifactId>
         <version>1.1.1</version>
       </dependency> <!--引⼊Jaxb，结束-->
   ```

3. 在子模块cloud-eureka-server-8761引入：

   ```xml
    <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
       </dependency>
   ```

4. application.yml:

   ```yaml
   #Eureka server服务端⼝
   server:
     port: 8761
   spring:
     application:
       name: cloud-eureka-server # 应⽤名称，会在Eureka中作为服务的id标识（serviceId）
   eureka:
     instance:
       hostname: localhost
     client:
       service-url: # 客户端与EurekaServer交互的地址，如果是集群，也需要写其它Server的地址
         defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
       register-with-eureka: false # ⾃⼰就是服务不需要注册⾃⼰
       fetch-registry: false #⾃⼰就是服务不需要从Eureka Server获取服务信息,默认为true，置为false
   ```

5. 启动类CloudEurekaApplication8761.java

   ```java
   @SpringBootApplication
   @EnableEurekaServer
   public class CloudEurekaApplication8761 {
   
       public static void main(String[] args) {
           SpringApplication.run(CloudEurekaApplication8761.class, args);
       }
   }
   ```

6. 执行启动类，访问[Eureka](http://127.0.0.1:8761/)

### 搭建Eureka Server HA 高可用集群

1. 修改本机hosts文件，增加条目

   ```
   127.0.0.1 a.eureka.server
   127.0.0.1 b.eureka.server
   ```

2. 复制cloud-eureka-server-8761，创建新子模块cloud-eureka-server-8762，修改主函数名

   其配置文件分别对应改为：

   ```xml
   #Eureka server服务端⼝
   server:
     port: 8761
   spring:
     application:
       name: cloud-eureka-server # 应⽤名称，会在Eureka中作为服务的id标识（serviceId）
   eureka:
     instance:
       hostname: a.eureka.server
     client:
       register-with-eureka: true # 需要注册⾃⼰
       fetch-registry: true #从Eureka Server获取服务信息,默认为true
       service-url: # 客户端与EurekaServer交互的地址，如果是集群，也需要写其它Server的地址
         defaultZone: http://b.eureka.server:8762/eureka/
   ```

   ```xml
   #Eureka server服务端⼝
   server:
     port: 8762
   spring:
     application:
       name: cloud-eureka-server # 应⽤名称，会在Eureka中作为服务的id标识（serviceId）
   eureka:
     instance:
       hostname: b.eureka.server
     client:
       register-with-eureka: true # 需要注册⾃⼰
       fetch-registry: true #从Eureka Server获取服务信息,默认为true
       service-url: # 客户端与EurekaServer交互的地址，如果是集群，也需要写其它Server的地址
         defaultZone: http://a.eureka.server:8761/eureka/
   ```

3. 启动工程，访问[Eureka8761](http://127.0.0.1:8761/)[Eureka8762](http://127.0.0.1:8762/)

#### 微服务提供者-->注册到Eureka Server集群

1. 在service-resume-8080模块基础上引入eureka client依赖

   ```xml
   <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

2. 配置application.yml，增添eureka相关配置

   ```xml
   eureka:
     client:
       service-url:
         #eureka server的路径
         defaultZone: http://a.eureka.server:8761/eureka/,http://b.eureka.server:8762/eureka/
     instance:
       #使⽤ip注册，否则会使⽤主机名注册了（此处考虑到对⽼版本的兼容，新版本经过实验都是ip）
       prefer-ip-address: true
       #⾃定义实例显示格式，加上版本号，便于多版本管理，注意是ip-address，早期版本是ipAddress
       instance-id: ${spring.cloud.client.ipaddress}:${spring.application.name}:${server.port}:@project.version@
   ```

3. 复制service-resume-8080，创建新子模块service-resume-8081，修改pom文件和入口类名，启动应用

#### 微服务消费者-->注册到Eureka Server集群

1. 在service-autodeliver-8090模块基础上引入eureka client依赖

   ```xml
   <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

2. 配置application.yml，增添eureka相关配置

   ```xml
   eureka:
     client:
       service-url:
         defaultZone: http://a.eureka.server:8761/eureka/,http://b.eureka.server:8762/eureka/
     instance:
       #使⽤ip注册，否则会使⽤主机名注册了（此处考虑到对⽼版本的兼容，新版本经过实验都是ip）
       prefer-ip-address: true
       #⾃定义实例显示格式，加上版本号，便于多版本管理，注意是ip-address，早期版本是ipAddress
       instance-id: ${spring.cloud.client.ipaddress}:${spring.application.name}:${server.port}:@project.version@
   spring:
     application:
       name: service-autodeliver
   ```

3. 在启动类添加注解@EnableDiscoveryClient，开启服务发现

4. 服务消费者调用服务提供者（通过Eureka）

   ```java
   @Slf4j
   @RestController
   @RequestMapping("/autodeliver")
   public class AutoDeliverController {
   
       @Autowired
       private RestTemplate restTemplate;
   
       @Autowired
       private DiscoveryClient discoveryClient;
   
       @GetMapping("/checkState/{userId}")
       public Integer findResumeOpenState(@PathVariable Long userId) {
           //获取Eureka中注册的实例列表
           List<ServiceInstance> instances = discoveryClient.getInstances("service-resume");
           //获取实例, 此处不考虑负载，就拿第一个
           ServiceInstance serviceInstance = instances.get(0);
           //根据实例的信息拼接请求地址
           String host = serviceInstance.getHost();
           int port = serviceInstance.getPort();
           String url = "http://" + host + ":" + port + "/resume/openstate/" + userId;
           //消费者直接调用提供者
           Integer forObject = restTemplate
                   .getForObject(url, Integer.class);
           log.info("调用简历微服务，获取用户： " + userId + " 默认简历当前状态为: " + forObject);
           return forObject;
       }
   }
   ```

5. 请求消费者，在工程根目录创建文件eureka.http，内容为

   ```
   GET http://localhost:8090/autodeliver/checkState/1545136
   Accept: application/json
   
   ###
   ```

   点击请求，查看结果

   截至当前的源代码下载，[spring-cloud-eureka.zip](/file/springcloud/spring-cloud-eureka.zip)

### Eureka 细节详解

#### Eureka 元数据详解

#### Eureka 客户端详解

