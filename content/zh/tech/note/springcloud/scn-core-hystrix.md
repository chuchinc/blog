---
title: "SCN核心组件 Hystrix"
date: 2020-10-06T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Hystrix"]
---

*本文源代码下载：[spring-cloud-hystrix.zip](/file/springcloud/spring-cloud-hystrix.zip)*

## 微服务中的雪崩效应

## 雪崩效应解决方法

## Hystrix简介

## Hystrix熔断应用

1. 消费者工程中引入Hystrix依赖：

```xml
<!--    熔断器Hystrix-->
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
```

2. 消费者工程启动类中添加熔断器开启注解

```java
@EnableCircuitBreaker //开启熔断
```

3. 定义服务降级处理⽅法，并在业务⽅法上使⽤@HystrixCommand的fallbackMethod属性关联到 服务降级处理⽅法

```java
       /**
        * 提供者模拟处理超时，调用方法添加Hystrix控制
        * @param userId
        * @return
        */
       // 使用@HystrixCommand注解进行熔断控制
       @HystrixCommand(
               // 线程池标识，要保持唯一，不唯一的话就共用了
               threadPoolKey = "findResumeOpenStateTimeout",
               // 线程池细节属性配置
               threadPoolProperties = {
                       @HystrixProperty(name="coreSize",value = "1"), // 线程数
                       @HystrixProperty(name="maxQueueSize",value="20") // 等待队列长度
               },
               // commandProperties熔断的一些细节属性配置
               commandProperties = {
                       // 每一个属性都是一个HystrixProperty
                       @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="2000")
               }
       )
       @GetMapping("/checkStateTimeout/{userId}")
       public Integer findResumeOpenStateTimeout(@PathVariable Long userId) {
           // 使用ribbon不需要我们自己获取服务实例然后选择一个那么去访问了（自己的负载均衡）
           String url = "http://service-resume/resume/openstate/" + userId;  // 指定服务名
           Integer forObject = restTemplate.getForObject(url, Integer.class);
           return forObject;
       }
       
       @HystrixCommand(
               // 线程池标识，要保持唯一，不唯一的话就共用了
               threadPoolKey = "findResumeOpenStateTimeoutFallback",
               // 线程池细节属性配置
               threadPoolProperties = {
                       @HystrixProperty(name="coreSize",value = "2"), // 线程数
                       @HystrixProperty(name="maxQueueSize",value="20") // 等待队列长度
               },
               // commandProperties熔断的一些细节属性配置
               commandProperties = {
                       // 每一个属性都是一个HystrixProperty
                       @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="2000"),
                       // hystrix高级配置，定制工作过程细节
                       // 统计时间窗口定义
                       @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "8000"),
                       // 统计时间窗口内的最小请求数
                       @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "2"),
                       // 统计时间窗口内的错误数量百分比阈值
                       @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
                       // 自我修复时的活动窗口长度
                       @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "3000")
               },
               fallbackMethod = "myFallBack"  // 回退方法
       )
       @GetMapping("/checkStateTimeoutFallback/{userId}")
       public Integer findResumeOpenStateTimeoutFallback(@PathVariable Long userId) {
           // 使用ribbon不需要我们自己获取服务实例然后选择一个那么去访问了（自己的负载均衡）
           String url = "http://service-resume/resume/openstate/" + userId;  // 指定服务名
           Integer forObject = restTemplate.getForObject(url, Integer.class);
           return forObject;
       }
       /*
           定义回退方法，返回预设默认值
           注意：该方法形参和返回值与原始方法保持一致
        */
       public Integer myFallBack(Long userId) {
           return -123333; // 兜底数据
       }
```

   注意：降级（兜底）方法必须和被降级方法相同的方法签名（相同参数列表，相同返回值），可以在类上使用@DefaultProperties注解统一指定整个类中公用的降级（兜底）方法

   访问接口，此时能正常访问，返回结果

   ```
   GET http://localhost:8090/autodeliver/checkStateTimeout/1545136
   Accept: application/json
   
   ###
   
   GET http://localhost:8090/autodeliver/checkStateTimeoutFallback/1545136
   Accept: application/json
   
   ###
   ```

4. 服务提供者模拟请求超时（线程休眠3s），只修改8080实例，8081不修改，对比观察

```java
   @RestController
   @RequestMapping("/resume")
   public class ResumeController {
   
       @Autowired
       private ResumeService resumeService;
   
       @GetMapping("/openstate/{userId}")
       public Integer findDefaultResumeByUserId(@PathVariable Long userId) {
           try {
               Thread.sleep(3000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           Resume resume = resumeService.findDefaultResumeByUserId(userId);
           System.out.println(resume.getIsOpenResume());
           return resume.getIsOpenResume();
       }
   }
```

    因为我们已经使⽤了Ribbon负载（轮询），所以我们在请求的时候，⼀次熔断降级，⼀次正常返 回

## Hystrix舱壁模式（线程池隔离策略）

Hystrix舱壁模式程序修改：

```java
// (findResumeOpenStateTimeout)线程池细节属性配置
            threadPoolProperties = {
                    @HystrixProperty(name="coreSize",value = "1"), // 线程数
                    @HystrixProperty(name="maxQueueSize",value="20") // 等待队列长度
            },
// (findResumeOpenStateTimeoutFallback)线程池细节属性配置
            threadPoolProperties = {
                    @HystrixProperty(name="coreSize",value = "2"), // 线程数
                    @HystrixProperty(name="maxQueueSize",value="20") // 等待队列长度
            },
```

先查看AutoDeliverApplication8090进程的pid：

```shell
λ jps
17760 Launcher
5380 RemoteMavenServer36
12616 RemoteMavenServer36
14120 AutoDeliverApplication8090
2504 RemoteMavenServer36
17100 RemoteMavenServer36
3852
18448 ResumeApplication8081
17172 CloudEurekaApplication8761
4724 CloudEurekaApplication8762
5748 ResumeApplication8080
7576 RemoteMavenServer36
8152 Jps
```

通过jstack命令查看线程情况，和我们程序设置的相符

```shell
λ jstack 14120 | grep hystrix
"hystrix-findResumeOpenStateTimeout-1" #125 daemon prio=5 os_prio=0 cpu=109.38ms elapsed=770.29s tid=0x0000023807011800 nid=0x1044 waiting on condition  [0x000000c44adff000]
"hystrix-findResumeOpenStateTimeoutFallback-1" #134 daemon prio=5 os_prio=0 cpu=15.63ms elapsed=758.60s tid=0x0000023804bdb000 nid=0x1f58 waiting on condition  [0x000000c44b6fe000]
"hystrix-findResumeOpenStateTimeoutFallback-2" #137 daemon prio=5 os_prio=0 cpu=15.63ms elapsed=754.55s tid=0x0000023804bdf000 nid=0x2164 waiting on condition  [0x000000c44b9fe000]
```

## Hystrix工作流与高级应用

1. 当调用出现问题，开启一个时间窗（10s）
2. 在这个时间窗内，统计调用次数是否达到最小请求数？
   * 如果没有达到，则重置统计信息，回到第1步
   * 如果达到了，则统计失败的请求数占所有请求数的百分⽐，是否达到阈值？ 如果达到，则跳闸（不再请求对应服务） 如果没有达到，则重置统计信息，回到第1步
3. 如果跳闸，则会开启⼀个活动窗⼝（默认5s），每隔5s，Hystrix会让⼀个请求通过,到达那个问题服 务，看 是否调⽤成功，如果成功，重置断路器回到第1步，如果失败，回到第3步

```java
/**
* 8秒钟内，请求次数达到2个，并且失败率在50%以上，就跳闸 * 跳闸后活动窗⼝设置为3s 
*/
@HystrixCommand(
commandProperties = {
                    // 每一个属性都是一个HystrixProperty          @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="2000"),
                    // hystrix高级配置，定制工作过程细节
                    // 统计时间窗口定义
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds",value = "8000"),
                    // 统计时间窗口内的最小请求数
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "2"),
                    // 统计时间窗口内的错误数量百分比阈值
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "50"),
                    // 自我修复时的活动窗口长度
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "3000")
            }
)
```

通过注解进行的配置也可以配置在配置文件中

```xml
#配置熔断策略
hystrix:
  command:
    default:
      circuitBreaker: # 强制打开熔断器，如果该属性设置为true，强制断路器进⼊打开状态，将会拒绝所有的请求。 默认false关闭的 
        forceOpen: false # 触发熔断错误⽐例阈值，默认值50% 
        errorThresholdPercentage: 50 # 熔断后休眠时⻓，默认值5秒 
        sleepWindowInMilliseconds: 3000 # 熔断触发最⼩请求次数，默认值是20 
        requestVolumeThreshold: 2 
      execution: 
        isolation: 
          thread: # 熔断超时设置，默认为1秒 
            timeoutInMilliseconds: 2000
```

基于springboot的健康检查观察跳闸状态（⾃动投递微服务暴露健康检查细节）

```xml
# springboot中暴露健康检查等断点接口
management:
  endpoints:
    web:
      exposure:
        include: "*"
  #暴露健康接口的细节
  endpoint:
    health:
      show-details: always
```

访问健康检查接口：[http://localhost:8090/actuator/health](http://localhost:8090/actuator/health)

```json
"hystrix":{"status":"UP"}
```

跳闸状态

```json
"hystrix":{
    "status": "CIRCUIT_OPEN",
    "detail": {
        "openCircuitBreakers": [
            "AutodeliverController::findResumeOpenStateTimeoutFallback"
        ]
    }
}
```

窗口内自我修复

```json
"hystrix":{"status":"UP"}
```

## Hystrix Dashboard断路监控仪表盘

正常状态是UP，跳闸是⼀种状态CIRCUIT_OPEN，可以通过/health查看，前提是⼯程中需要引⼊ SpringBoot的actuator（健康监控），它提供了很多监控所需的接⼝，可以对应⽤系统进⾏配置查看、 相关功能统计等。
已经统⼀添加在⽗⼯程中

```xml
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

如果我们想看到Hystrix相关数据，⽐如有多少请求、多少成功、多少失败、多少降级等，那么引⼊ SpringBoot健康监控之后，访问/actuator/hystrix.stream接⼝可以获取到监控的⽂字信息，但是不直观，所以Hystrix官⽅还提供了基于图形化的DashBoard（仪表板）监控平 台。Hystrix仪表板可以显示 每个断路器（被@HystrixCommand注解的⽅法）的状态。

1. 新建一个监控服务工程cloud-hystrix-dashboard-9000，导入依赖坐标

```xml
   <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
```

2. 启动类添加@EnableHystrixDashboard激活仪表盘

```java
   @SpringBootApplication
   @EnableHystrixDashboard
   public class HystrixDashboardApplication9000 {
   
       public static void main(String[] args) {
           SpringApplication.run(HystrixDashboardApplication9000.class, args);
       }
   }
```

3. application.yml

```xml
   server:
     port: 9000
   spring:
     application:
       name: cloud-hystrix-dashboard
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

4. 在被监测的微服务中注册监控servlet（⾃动投递微服务，监控数据就是来⾃于这个微服务）

```java
   /**
        * 在被监控的微服务中注册一个serlvet，后期我们就是通过访问这个servlet来获取该服务的Hystrix监控数据的
        * 前提：被监控的微服务需要引入springboot的actuator功能
        * @return
        */
       @Bean
       public ServletRegistrationBean getServlet(){
           HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
           ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
           registrationBean.setLoadOnStartup(1);
           registrationBean.addUrlMappings("/actuator/hystrix.stream");
           registrationBean.setName("HystrixMetricsStreamServlet");
           return registrationBean;
       }
```

   被监控微服务发布之后，可以直接访问监控servlet，但是得到的数据并不直观，后期可以结合仪表盘更 友好的展示

5. 访问Dashboard地址: [http://localhost:9000/hystrix](http://localhost:9000/hystrix)

   输⼊监控的微服务端点地址，展示详细信息：[http://localhost:8090/actuator/hystrix.stream](http://localhost:8090/actuat or/hystrix.stream)

   访问接口，查看dashboard

   百分⽐，10s内错误请求百分⽐

   实心圆：

   * ⼤⼩：代表请求流量的⼤⼩，流量越⼤球越⼤
   * 颜⾊：代表请求处理的健康状态，从绿⾊到红⾊递减，绿⾊代表健康，红⾊就代表很不健康

   曲线波动图： 记录了2分钟内该⽅法上流量的变化波动图，判断流量上升或者下降的趋势

## Hystrix Turbine聚合监控

之前，我们针对的是⼀个微服务实例的Hystrix数据查询分析，在微服务架构下，⼀个微服务的实例往往是多个（集群化）比如自动投递微服务实例1(hystrix) ip1:port1/actuator/hystrix.stream，实例2(hystrix) ip2:port2/actuator/hystrix.stream，实例3(hystrix) ip3:port3/actuator/hystrix.stream，按照已有的⽅法，我们就可以结合dashboard仪表盘每次输⼊⼀个监控数据流url，进去查看
⼿⼯操作能否被⾃动功能替代？Hystrix Turbine聚合（聚合各个实例上的hystrix监控数据）监控 Turbine（涡轮）

思考：微服务架构下，⼀个微服务往往部署多个实例，如果每次只能查看单个实例的监控，就需要经常 切换很不⽅便，在这样的场景下，我们可以使⽤ Hystrix Turbine 进⾏聚合监控，它可以把相关微服务 的监控数据聚合在⼀起，便于查看。

1. 新建项目cloud-hystrix-turbine-9001，引入依赖坐标

```xml
   <dependencies>
       <!--hystrix turbine聚合监控-->
       <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
       </dependency>
       <!--引⼊eureka客户端的两个原因 
   	1、微服务架构下的服务都尽量注册到服务中⼼去，便于统⼀管理
       2、后续在当前turbine项⽬中我们需要配置turbine聚合的服务，⽐如，我们希望聚合 lagou-service-autodeliver这个服务的各个实例的hystrix数据流，那随后
       我们就需要在application.yml⽂件中配置这个服务名，那么turbine获取服务下具
       体实例的数据流的
       时候需要ip和端⼝等实例信息，那么怎么根据服务名称获取到这些信息呢？ 当然可以从eureka服务注册中⼼获取
       -->
       <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       </dependency>
     </dependencies>
```

2. 将需要进行Hystrix监控的多个微服务配置起来，在工程application.yml中开启Turbine及进⾏相关配置

```xml
   server:
     port: 9001
   spring:
     application:
       name: cloud-hystrix-turbine
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
   turbine:
     #appCofing配置需要聚合的服务名称，⽐如这⾥聚合⾃动投递微服务的hystrix监控数据
     # 如果要聚合多个微服务的监控数据，那么可以使⽤英⽂逗号拼接，⽐如 a,b,c
     appConfig: service-autodeliver
     # 集群默认名称
     clusterNameExpression: "'default'"
```

3. 在当前项⽬启动类上添加注解@EnableTurbine，开启仪表盘以及Turbine聚合

```java
   @SpringBootApplication
   @EnableDiscoveryClient
   @EnableTurbine //开启聚合功能
   public class HystrixTurbineApplication9001 {
   
       public static void main(String[] args) {
           SpringApplication.run(HystrixTurbineApplication9001.class, args);
       }
   }
```

4. 浏览器访问Turbine项目: [localhost:9001/turbine.stream](http://localhost:9001/turbine.stream)，就可以看到监控数据，通过dashboard的⻚⾯查看数据更直观，把刚才的地址输⼊[Hystrix Dashboard](http://localhost:9000/hystrix/) 的地址栏

