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


