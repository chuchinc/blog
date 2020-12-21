---
title: "案例分析引入"
date: 2020-10-03T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud"]
---

## 案例说明

## 案例数据库环境准备

数据库使用MySQL 5.7.X，数据库初始化：[SQL文件](/file/springcloud/springcloud.sql)

## 案例工程环境准备

父工程 spring-cloud-parent，新建maven工程，[pom文件](/file/springcloud/parent-pom.xml)

## 案例核心微服务开发及通信调用

### 简历微服务

* 新建子模块 service-common

  [pom.xml](/file/springcloud/service-common-pom.xml)     [Resume.java](/file/springcloud/Resume.java)

* 新建子模块 service-resume-8080

  [pom.xml](/file/springcloud/service-common-pom.xml)     [ResumeController.java](/file/springcloud/ResumeController.java)   [ResumeDao.java](/file/springcloud/ResumeDao.java)   [ResumeService.java](/file/springcloud/ResumeService.java)    [ResumeServiceImpl.java](/file/springcloud/ResumeServiceImpl.java)    [ResumeApplication8080.java](/file/springcloud/ResumeApplication8080.java)  [application.yml](/file/springcloud/resume-application.yml) 

### 自动投递微服务

* 新建子模块 service-autodeliver-8090

  无需引入其他依赖

  application.yml

  ```xml
  server:
    port: 8090
  ```

  AutoDeliverApplication.java

  ```java
  @SpringBootApplication
  public class AutoDeliverApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(AutoDeliverApplication.class, args);
      }
  
      @Bean
      public RestTemplate getRestTemplate() {
          return new RestTemplate();
      }
  }
  ```

  AutoDeliverController.java

  ```java
  @Slf4j
  @RestController
  @RequestMapping("/autodeliver")
  public class AutoDeliverController {
  
      @Autowired
      private RestTemplate restTemplate;
  
      @GetMapping("/checkState/{userId}")
      public Integer findResumeOpenState(@PathVariable Long userId) {
          Integer forObject = restTemplate
                  .getForObject("http://localhost:8080/resume/openstate/" + userId, Integer.class);
          log.info("调用简历微服务，获取用户： " + userId + " 默认简历当前状态为: " + forObject);
          return forObject;
      }
  
  }
  ```

## 案例代码问题分析

我们在⾃动投递微服务中使⽤RestTemplate调⽤简历微服务的简历状态接⼝时（Restful API 接⼝）。 在微服务分布式集群环境下会存在什么问题呢？怎么解决？

存在的问题：

* 在服务消费者中，我们把url地址硬编码到代码中，不⽅便后期维护。
* 服务提供者只有⼀个服务，即便服务提供者形成集群，服务消费者还需要⾃⼰实现负载均衡。
* 在服务消费者中，不清楚服务提供者的状态。
* 服务消费者调⽤服务提供者时候，如果出现故障能否及时发现不向⽤户抛出异常⻚⾯？
* RestTemplate这种请求调⽤⽅式是否还有优化空间？能不能类似于Dubbo那样玩？
* 这么多的微服务统⼀认证如何实现？
* 配置⽂件每次都修改好多个很麻烦！？
* ....

上述分析出的问题，其实就是微服务架构中必然⾯临的⼀些问题：

* 服务管理：⾃动注册与发现、状态监管
* 服务负载均衡
* 熔断
* 远程过程调⽤
* ⽹关拦截、路由转发
* 统⼀认证
* 集中式配置管理，配置信息实时⾃动更新