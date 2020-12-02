---
title: "SCN核心组件 Feign"
date: 2020-10-07T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Feign"]
---

*本文源代码下载：[spring-cloud-feign.zip](/file/springcloud/spring-cloud-feign.zip)*

## 引言

服务消费者调用服务提供者的时候使用RestTemplate技术，存在不便之处，拼接URL和restTemplate.getForObject这两处代码都比较模板化，那么能不能不让我们来写这种模板化的东西，另外来说，拼接url存在硬编码，还比较容易出错

## Feign 简介

Feign是Netflix开发的⼀个轻量级RESTful的HTTP服务客户端（⽤它来发起请求，远程调⽤的），是以 Java接⼝注解的⽅式调⽤Http请求，而不用像Java中通过封装HTTP请求报⽂的⽅式直接调⽤，Feign被广泛应⽤在Spring Cloud 的解决⽅案中。

类似于Dubbo，服务消费者拿到服务提供者的接⼝，然后像调用本地方法一样去调用，实际发出的是远程请求。

* Feign可帮助我们更加便捷，优雅的调⽤HTTP API：不需要我们去拼接url然后呢调⽤ restTemplate的api，在SpringCloud中，使⽤Feign非常简单，创建⼀个接口（在消费者--服务调用方这⼀端），并在接口上添加⼀些注解，代码就完成了
* SpringCloud对Feign进行了增强，是Feign支持SpringMVC注解（OpenFeign）

**本质：封装了Http调用流程，更符合面向接口编程的习惯，类似Dubbo的服务调用**

## Feign 配置应用

在服务调用工程（消费）创建接口（添加注解）

（效果）Feign = RestTemplate + Ribbon + Hystrix

1. 复制service-autodeliver-8090模块到新建的service-autodeliver-8091模块

2. 服务消费者⼯程（⾃动投递微服务8091）中引⼊Feign依赖（或者⽗类⼯程）

   ```xml
   <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ```

3. 服务消费者⼯程（⾃动投递微服务）启动类使⽤注解@EnableFeignClients添加Feign⽀持

   ```java
   @SpringBootApplication
   @EnableDiscoveryClient //开启服务发现
   @EnableFeignClients //开启feign
   public class AutoDeliverApplication8091 {
       public static void main(String[] args) {
           SpringApplication.run(AutoDeliverApplication8091.class, args);
       }
   }
   ```

   此时去掉Hystrix熔断的⽀持注解@EnableCircuitBreaker

4. 创建Feign接口

   ```java
   //调用的服务名称，和服务提供者yml文件中spring.application.name保持一致
   @FeignClient(name = "service-resume")
   public interface ResumeFeignClient {
       
       /**
        * 调用请求路径
        * @param userId
        * @return
        */
       @RequestMapping(value = "/resume/openstate/{userId}", method = RequestMethod.GET)
       Integer findResumeOpenState(@PathVariable(value = "userId") Long userId);
   
   }
   ```

   **注意**

   * @FeignClient注解的name属性⽤于指定要调⽤的服务提供者名称，和服务提供者yml⽂件中 spring.application.name保持⼀致
   * 接口中的⽅法，就好比是远程服务提供者Controller中的Hander⽅法（只不过如同本地调 ⽤了），那么在进行参数绑定时，可以使⽤@PathVariable、@RequestParam、 @RequestHeader等，这也是OpenFeign对SpringMVC注解的⽀持，但是需要注意value必须设置，否则会抛出异常

5. 使用接口方法完成远程调用（注入接口即可，实际注入的是接口的实现）

   ```java
   @Slf4j
   @RestController
   @RequestMapping("/autodeliver")
   public class AutoDeliverController {
   
       @Autowired
       private ResumeFeignClient resumeFeignClient;
   
       @GetMapping("/checkState/{userId}")
       public Integer findResumeOpenState(@PathVariable Long userId) {
           return resumeFeignClient.findResumeOpenState(userId);
       }
   }
   ```

6. 启动项目，然后在test.http访问

   ```xml
   GET http://localhost:8091/autodeliver/checkState/1545136
   Accept: application/json
   
   ###
   ```

## Feign 对负载均衡的支持

Feign 本身已经集成了Ribbon依赖和⾃动配置，因此我们不需要额外引⼊依赖，可以通过 ribbon.xx 来 进 ⾏全局配置,也可以通过服务名.ribbon.xx 来对指定服务进⾏细节配置配置（参考之前，此处略）

Feign默认的请求处理超时时⻓1s，有时候我们的业务确实执⾏的需要⼀定时间，那么这个时候，我们 就需要调整请求处理超时时⻓，Feign⾃⼰有超时设置，如果配置Ribbon的超时，则会以Ribbon的为准

**Ribbon设置**

```xml
#针对被调用方微服务名称，不加就是全局生效
service-resume:
  ribbon:
    #请求连接超时时间
    #ConnectTimeout: 2000 
    #请求处理超时时间 
    #ReadTimeout: 5000 
    #对所有操作都进⾏重试 
    OkToRetryOnAllOperations: true 
    ####根据如上配置，当访问到故障请求的时候，它会再尝试访问⼀次当前实例（次数由 MaxAutoRetries配置）， 
    ####如果不⾏，就换⼀个实例进⾏访问，如果还不行，再换⼀次实例访问（更换次数由 MaxAutoRetriesNextServer配置）， 
    ####如果依然不⾏，返回失败信息。 
    MaxAutoRetries: 0 #对当前选中实例重试次数，不包括第⼀次调⽤ 
    MaxAutoRetriesNextServer: 0 #切换实例的重试次数 
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule #负载策略调整
```

## Feign对熔断器支持

1. 在Feign客户端⼯程配置⽂件（application.yml）中开启Feign对熔断器的⽀持

   ```xml
   #开启Feign的熔断功能
   feign:
     hystrix:
       enabled: true
   ```

   Feign的超时时⻓设置那其实就上⾯Ribbon的超时时⻓设置 

   Hystrix超时设置（就按照之前Hystrix设置的⽅式就OK了）

   注意：

   * 开启Hystrix之后，Feign中的⽅法都会被进⾏统一管理了，⼀旦出现问题就进⼊对应的回退逻辑处理
   * 针对超时这⼀点，当前有两个超时时间设置（Feign/hystrix），熔断的时候是根据这两个时间的最小值来进⾏的，即处理时⻓超过最短的那个超时时间了就熔断进⼊回退降级逻辑

   ```xml
   hystrix:
     command:
       default:
         execution:
           isolation:
             thread:
               timeoutInMilliseconds: 1000  #超时时长
   ```

2. 自定义FallBack处理类（需要实现FeignClient接口）

   ```java
   @Component
   public class ResumeFallback implements ResumeFeignClient{
   
       /**
        * 调用请求路径
        */
       @Override
       public Integer findResumeOpenState(Long userId) {
           return -10086;
       }
   }
   ```

3. 在@FeignClient注解中关联自定义处理类

   ```java
   // 使⽤fallback的时候，类上的 @RequestMapping的url前缀限定，改成配置在@FeignClient的path属性中 //@RequestMapping("/resume")
   @FeignClient(name = "service-resume", fallback = ResumeFallback.class, path = "/resume")
   public interface ResumeFeignClient {
       /**
        * 调用请求路径
        * @param userId
        * @return
        */
       @RequestMapping(value = "/openstate/{userId}", method = RequestMethod.GET)
       Integer findResumeOpenState(@PathVariable(value = "userId") Long userId);
   }
   ```

## Feign 对请求压缩和响应压缩的支持

Feign ⽀持对请求和响应进⾏GZIP压缩，以减少通信过程中的性能损耗。通过下⾯的参数即可开启请求与响应的压缩功能：

```xml
feign:
  compression:
    request:
      #开启压缩请求
      enabled: true 
      #设置压缩的数据类，此处也是默认值
      mime-types: text/html,application/xml,application/json 
    response:
      enabled: true
```

## Feign 的日志级别配置

Feign是http请求客户端，类似于咱们的浏览器，它在请求和接收响应的时候，可以打印出比较详细的 ⼀些⽇志信息（响应头，状态码等等）

如果我们想看到Feign请求时的日志，我们可以进行配置，默认情况下Feign的日志没有开启

1. 开启Feign日志级别功能和级别

   ```java
   @Configuration
   public class FeignConfig {
   
       /**
        * // Feign的⽇志级别（Feign请求过程信息） 
        * // NONE：默认的，不显示任何⽇志----性能最好 
        * // BASIC：仅记录请求⽅法、URL、响应状态码以及执⾏时间----⽣产问题追踪 
        * // HEADERS：在BASIC级别的基础上，记录请求和响应的header 
        * // FULL：记录请求和响应的header、body和元数据----适⽤于开发及测试环境定位问题
        * @return
        */
       @Bean
       Logger.Level feignLevel() {
           return Level.FULL;
       }
   }
   ```

2. 配置log日志级别为debug

   ```xml
   logging:
     level:
       #Feign日志只会对日志级别为debug的作出反应
       cn.chuchin.controller.service.ResumeFeignClient: debug
   ```

   