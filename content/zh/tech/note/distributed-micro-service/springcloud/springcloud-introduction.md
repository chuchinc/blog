---
title: "Spring Cloud 简述"
date: 2020-10-02T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud"]
---

## Spring Cloud 是什么

[百度百科]Spring Cloud是⼀系列框架的有序集合。它利⽤Spring Boot的开发便利性巧妙地简化了分布 式系统基础设施的开发，如服务发现注册、配置中⼼、消息总线、负载均衡、断路器、数据监控等，都 可以⽤ Spring Boot的开发⻛格做到⼀键启动和部署。Spring Cloud并没有重复制造轮⼦，它只是将⽬ 前各家公司开发的⽐较成熟、经得起实际考验的服务框架组合起来，通过Spring Boot⻛格进⾏再封装 屏蔽掉了复杂的配置和实现原理，最终给开发者留出了⼀套简单易懂、易部署和易维护的分布式系统开发⼯具包。

Spring Cloud是⼀系列框架的有序集合（Spring Cloud是⼀个规范）

开发服务发现注册、配置中⼼、消息总线、负载均衡、断路器、数据监控等

利⽤Spring Boot的开发便利性简化了微服务架构的开发（⾃动装配）

这⾥，我们需要注意，Spring Cloud其实是⼀套规范，是⼀套⽤于构建微服务架构的规范，而不是⼀个 可以拿来即用的框架（所谓规范就是应该有哪些功能组件，然后组件之间怎么配合，共同完成什么事情）。在这个规范之下第三⽅的Netflix公司开发了⼀些组件、Spring官⽅开发了⼀些框架/组件，包括 第三⽅的阿⾥巴巴开发了⼀套框架/组件集合Spring Cloud Alibaba，这些才是Spring Cloud规范的实 现。
Netflix搞了⼀套 简称SCN Spring Cloud 吸收了Netflix公司的产品基础之上⾃⼰也搞了⼏个组件 阿⾥巴巴在之前的基础上搞出了⼀堆微服务组件,Spring Cloud Alibaba（SCA）

## Spring Cloud 解决什么问题

Spring Cloud 规范及实现意图要解决的问题其实就是微服务架构实施过程中存在的⼀些问题，⽐如微服 务架构中的服务注册发现问题、⽹络问题（⽐如熔断场景）、统⼀认证安全授权问题、负载均衡问题、 链路追踪等问题。

## Spring Cloud 架构

如前所述，Spring Cloud是⼀个微服务相关规范，这个规范意图为搭建微服务架构提供⼀站式服务，采⽤组件（框架）化机制定义⼀系列组件，各类组件针对性的处理微服务中的特定问题，这些组件共同来 构成Spring Cloud微服务技术栈。

## Spring Cloud 核心组件

Spring Cloud ⽣态圈中的组件，按照发展可以分为第⼀代 Spring Cloud组件和第⼆代 Spring Cloud组件。

![20201221-160513-0552.png](https://gitee.com/chuchin/img/raw/master/20201221-160513-0552.png)

## Spring Cloud 体系结构（组件协同工作机制）

![20201221-160714-0195.png](https://gitee.com/chuchin/img/raw/master/20201221-160714-0195.png)

Spring Cloud中的各组件协同⼯作，才能够⽀持⼀个完整的微服务架构。⽐如

* 注册中⼼负责服务的注册与发现，很好将各服务连接起来
* API⽹关负责转发所有外来的请求
* 断路器负责监控服务之间的调⽤情况，连续多次失败进行熔断保护。
* 配置中心提供了统⼀的配置信息管理服务,可以实时的通知各个服务获取最新的配置信息

## Spring Cloud 与 Dubbo 对比

Dubbo是阿⾥巴巴公司开源的⼀个⾼性能优秀的服务框架，基于RPC调⽤，对于⽬前使⽤率较⾼的 Spring Cloud Netflix来说，它是基于HTTP的，所以效率上没有Dubbo⾼，但问题在于Dubbo体系的组件不全，不能够提供⼀站式解决⽅案，⽐如服务注册与发现需要借助于Zookeeper等实现，⽽Spring Cloud Netflix则是真正的提供了⼀站式服务化解决⽅案，且有Spring⼤家族背景。

前些年，Dubbo使⽤率⾼于SpringCloud，但⽬前Spring Cloud在服务化/微服务解决⽅案中已经有了非常好的发展趋势。

## Spring Cloud 与 Spring Boot 的关系

Spring Cloud 只是利⽤了Spring Boot 的特点，让我们能够快速的实现微服务组件开发，否则不使⽤ Spring Boot的话，我们在使⽤Spring Cloud时，每⼀个组件的相关Jar包都需要我们⾃⼰导⼊配置以及 需要开发⼈员考虑兼容性等各种情况。所以Spring Boot是我们快速把Spring Cloud微服务技术应⽤起 来的⼀种⽅式。