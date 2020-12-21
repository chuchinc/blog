---
title: "SCN核心组件 Eureka"
date: 2020-10-04T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Eureka"]
---

*本文源代码下载：[spring-cloud-eureka.zip](/file/springcloud/spring-cloud-eureka.zip)*

## 关于注册中心

对于任何一个

**注意：服务注册中心本质上是为了解耦服务提供者和服务消费者。**

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


### Eureka 细节详解

#### Eureka 元数据详解

Eureka的元数据有两种：标准元数据和自定义元数据

**标准元数据：** 

**自定义元数据：**

在程序中使用DiscoveryClient获取指定微服务的所有元数据

```java
@SpringBootTest(classes = {AutoDeliverApplication8090.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class MetaDataTest {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Test
    public void test() {
        //获取服务实例
        List<ServiceInstance> instances = discoveryClient.getInstances("service-resume");
        //打印元数据信息
        for (ServiceInstance instance : instances) {
            System.out.println(instance.toString());
        }
    }

}
```

debug查看获取到的实例对象，查看元数据

#### Eureka 客户端

服务提供者（也就是Eureka客户端）要向EurekaServer注册服务，并完成服务续约等工作

##### 服务注册（服务提供者）

1. 当我们导入了eureka-client依赖坐标，配置Eureka服务注册中心地址
2. 服务在启动时会向注册中心发起注册请求，携带服务元数据信息
3. Eureka注册中心会把服务的信息保存在Map中

##### 服务续约（服务消费者）

服务每隔30秒会向注册中心续约（心跳）一次，如果没有续约，租约在90秒后到期，然后服务会被失效。每隔30秒的续约操作叫做心跳检测

往往不需要我们调整这两个配置：

```xml
#向Eureka服务中心集群注册服务
eureka:
	instance:
		#服务续约间隔时间，默认30秒
		lease-renewal-interval-in-seconds: 30
		#租约到期，服务失效时间，默认90秒，服务超过90秒没有发送心跳，EurekaServer会将服务从列表移除
		lease-expiration-duration-in-seconds: 90
```

##### 获取服务列表（服务消费者）

每隔30秒服务会从注册中心拉取一份服务列表，这个时间可以通过配置修改。往往不需要我们调整

```java
#向Eureka服务中心集群注册服务
eureka:
	client:
		#每隔多少秒拉取一次服务列表
		registry-fetch-interval-seconds: 30
```

1. 服务消费者启动时，从EurekaServer服务列表获取只读备份数据，缓存到本地
2. 每隔30秒，会重新获取并更新数据
3. 每隔30秒的时间可以通过配置修改

#### Eureka服务端

##### 服务下线

1. 当服务正常关闭操作时，会发送服务下线的REST请求给EurekaServer
2. 服务中心接受到请求后，会将该服务置为下线状态

##### 失效剔除

Eureka Server会定时（间隔值是eureka.server.eviction-interval-timer-in-ms，默认60s）进⾏检查， 如果发现实例在在⼀定时间（此值由客户端设置的eureka.instance.lease-expiration-duration-inseconds定义，默认值为90s）内没有收到⼼跳，则会注销此实例

##### 自我保护

服务提供者 —> 注册中⼼
定期的续约（服务提供者和注册中⼼通信），假如服务提供者和注册中⼼之间的⽹络有点问题，不代表 服务提供者不可⽤，不代表服务消费者⽆法访问服务提供者

如果在15分钟内超过85%的客户端节点都没有正常的⼼跳，那么Eureka就认为客户端与注册中⼼出现了 ⽹络故障，Eureka Server⾃动进⼊⾃我保护机制。

官方对自我保护机制的定义是：

```
自我保护模式正是一种针对网络异常波动的安全保护措施，使用自我保护模式能使Eureka集群更加的健壮、稳定的运行。
```

**为什么会有自我保护机制？**

默认情况下，如果Eureka Server在⼀定时间内（默认90秒）没有接收到某个微服务实例的⼼跳， Eureka Server将会移除该实例。但是当⽹络分区故障发⽣时，微服务与Eureka Server之间⽆法正常通 信，⽽微服务本身是正常运⾏的，此时不应该移除这个微服务，所以引⼊了⾃我保护机制。

服务中⼼⻚⾯会显示如下提示信息:

```
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY’RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.
```

**当处于自我保护模式时**

1. 不会剔除任何服务实例（可能是服务提供者和EurekaServer之间的网络问题），保证了大多数服务依然可用

2. Eureka Server工程中通过Server⼯程中通过eureka.server.enable-self-preservation配置可⽤关停⾃我保护，默认 值是打开

   ```xml
   eureka: server:
   enable-self-preservation: false # 关闭⾃我保护模式（缺省为打开）
   ```

   建议在生产环境打开自我保护机制