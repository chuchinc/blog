---
title: "SCA核心组件 Dubbo"
date: 2020-10-17T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Dubbo"]
---

*本文源代码下载：[spring-cloud-dubbo.zip](/file/springcloud/spring-cloud-dubbo.zip)*

## Nacos + Sentinel + Dubbo 整合

改造“⾃动投递微服务”和“简历微服务”，删除OpenFeign 和 Ribbon，使⽤Dubbo RPC 和 Dubbo LB ⾸先，需要删除或者注释掉父工程中的热部署依赖

```xml
<!--    <dependency>-->
<!--      <groupId>org.springframework.boot</groupId>-->
<!--      <artifactId>spring-boot-devtools</artifactId>-->
<!--      <optional>true</optional>-->
<!--    </dependency>-->
```

## 服务提供者改造

1. 新建service-resume-dubbo-api模块,新增接口

```java
public interface ResumeService {

    Integer findDefaultResumeByUserId(Long userId);
}
```

2. 改造服务提供者（简历微服务）
   * pom文件添加spring cloud + dubbo整合的依赖，同时添加dubbo服务接口⼯程依赖
   * 删除原有ResumeService接⼝，引⼊dubbo服务接⼝⼯程中的ResumeService接口，适当调整代码，在service的实现类上添加dubbo的@Service注解
   * application.yml或者bootstrap.yml配置⽂件中添加dubbo配置
   * 服务发布后，查看nacos控制台注册信息，从元数据中可以看出,是dubbo注 册上来的

```xml
<dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-dubbo</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-apache-dubbo-adapter</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.chuchin</groupId>
            <artifactId>service-dubbo-api</artifactId>
            <version>1.0-SNAPSHOT</version>
</dependency>
```

```java
import cn.chuchin.pojo.Resume;
import cn.chuchin.dao.ResumeDao;
import cn.chuchin.service.ResumeService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeDao resumeDao;

    @Override
    public Integer findDefaultResumeByUserId(Long userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        // 查询默认简历
        resume.setIsDefault(1);
        Example<Resume> example = Example.of(resume);
        return resumeDao.findOne(example).get().getIsOpenResume();
    }
}
```

```yaml
server:
  port: 8084
dubbo:
  scan:
    # dubbo 服务扫描基准包
    base-packages: cn.chuchin.service.impl
  protocol:
    # dubbo 协议
    name: dubbo
    # dubbo 协议端⼝（ -1 表示⾃增端⼝，从 20880 开始）
    port: -1
  registry:
    # 挂载到 Spring Cloud 的注册中⼼
    address: spring-cloud://localhost
spring:
  application:
    name: service-resume
  main:
    #Spring Boot 2.1 需要设置
    allow-bean-definition-overriding: true
```

![20201209-143730-0927.png](https://gitee.com/chuchin/img/raw/master/20201209-143730-0927.png)

## 服务消费者工程改造

1. 改造服务消费者⼯程—>⾃动投递微服务
   * pom.xml中删除OpenFeign相关内容
   
   * application.yml配置⽂件中删除和Feign、Ribbon相关的内容；代码中删除Feign客户端内容；
   
   * pom.xml添加内容和服务提供者⼀样
   
   * application.yml配置⽂件中添加dubbo相关内容
   
     ```yaml
     dubbo:
       registry:
         # 挂载到 Spring Cloud 的注册中⼼
         address: spring-cloud://localhost
       cloud:
         subscribed-services: service-resume
     spring:
       main:
         #Spring Boot 2.1 需要设置
         allow-bean-definition-overriding: true
     ```
   
   * controller代码改造，其他不变
   
     ```java
     @RestController
     @RequestMapping("/autodeliver")
     public class AutodeliverController {
     
     	//引入dubbo的Reference
         @Reference
         private ResumeService resumeService;
     
     
         @GetMapping("/checkState/{userId}")
         @SentinelResource(value = "findResumeOpenState",blockHandlerClass = SentinelHandlersClass.class,
                 blockHandler = "handleException",fallbackClass = SentinelHandlersClass.class,fallback = "handleError")
         public Integer findResumeOpenState(@PathVariable Long userId) {
             return resumeService.findDefaultResumeByUserId(userId);
         }
     
     }
     ```
   
2. 测试

```
GET http://localhost:8099/autodeliver/checkState/1545136
Accept: application/json

###

##返回
3
```

