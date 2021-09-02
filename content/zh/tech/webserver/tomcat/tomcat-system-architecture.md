---
title: "Tomcat 系统架构与原理"
date: 2020-08-01T10:21:43+08:00
draft: false
tags: ["Tomcat"]
---

b/s（浏览器/服务器模式） 浏览器是客户端（发送http请求） ———> 服务器端

## 浏览器访问服务器的流程

http请求的处理过程

![20201222-132859-0659.png](https://gitee.com/chuchin/img/raw/master/20201222-132859-0659.png)

注意：浏览器访问服务器使⽤的是Http协议，Http是应⽤层协议，⽤于定义数据通信的格式，具体的数 据传输使⽤的是TCP/IP协议

## Tomcat 系统总体架构

### Tomcat 请求处理大致过程

**Tomcat是⼀个Http服务器（能够接收并且处理http请求，所以tomcat是⼀个http服务器）**

我们使⽤浏览器向某⼀个⽹站发起请求，发出的是Http请求，那么在远程，Http服务器接收到这个请求之后，会调⽤具体的程序（Java类）进⾏处理，往往不同的请求由不同的Java类完成处理。

![20201222-145903-0536.png](https://gitee.com/chuchin/img/raw/master/20201222-145903-0536.png)

![20201222-142605-0166.png](https://gitee.com/chuchin/img/raw/master/20201222-142605-0166.png)

HTTP 服务器接收到请求之后把请求交给Servlet容器来处理，Servlet 容器通过Servlet接⼝调⽤业务类。**Servlet接口和Servlet容器这⼀整套内容叫作Servlet规范。**

注意：Tomcat既按照Servlet规范的要求去实现了Servlet容器，同时它也具有HTTP服务器的功能。

Tomcat的两个重要身份

1. http服务器
2. Tomcat是一个Servlet容器

### Tomcat Servlet容器处理流程

当⽤户请求某个URL资源时

1. HTTP服务器会把请求信息使⽤ServletRequest对象封装起来
2. 进⼀步去调⽤Servlet容器中某个具体的Servlet
3. 在 2 中，Servlet容器拿到请求后，根据URL和Servlet的映射关系，找到相应的Servlet
4. 如果Servlet还没有被加载，就⽤反射机制创建这个Servlet，并调⽤Servlet的init⽅法来完成初始化
5. 接着调⽤这个具体Servlet的service⽅法来处理请求，请求处理结果使⽤ServletResponse对象封装
6. 把ServletResponse对象返回给HTTP服务器，HTTP服务器会把响应发送给客户端

![20201222-140411-0225.png](https://gitee.com/chuchin/img/raw/master/20201222-140411-0225.png)

### Tomcat 系统总体架构

通过上面的讲解，我们发现tomcat有两个非常重要的功能需要完成

1. 和客户端浏览器进行交互，进⾏socket通信，将字节流和Request/Response等对象进行转换
2. Servlet容器处理业务逻辑

![20201222-142513-0071.png](https://gitee.com/chuchin/img/raw/master/20201222-142513-0071.png)

Tomcat 设计了两个核心组件连接器（Connector）和容器（Container）来完成 Tomcat 的两⼤核心功能。

**连接器，负责对外交流**： 处理Socket连接，负责网络字节流与Request和Response对象的转化

**容器，负责内部处理：**加载和管理Servlet，以及具体处理Request请求

## Tomcat 连接器组件 Coyote

### Coyote 简介

Coyote 是Tomcat 中连接器的组件名称 , 是对外的接⼝。客户端通过Coyote与服务器建⽴连接、发送请求并接受响应 。

1. Coyote 封装了底层的⽹络通信（Socket 请求及响应处理）
2. Coyote 使Catalina 容器（容器组件）与具体的请求协议及IO操作⽅式完全解耦
3. Coyote 将Socket 输⼊转换封装为 Request 对象，进⼀步封装后交由Catalina 容器进⾏处理，处 理请求完成后, Catalina 通过Coyote 提供的Response 对象将结果写⼊输出流
4. Coyote 负责的是具体协议（应⽤层）和 IO（传输层）相关内容

![20201222-145717-0353.png](https://gitee.com/chuchin/img/raw/master/20201222-145717-0353.png)

Tomcat Coyote ⽀持的 IO模型与协议

Tomcat⽀持多种应⽤层协议和I/O模型，如下：

![20201222-145418-0813.png](https://gitee.com/chuchin/img/raw/master/20201222-145418-0813.png)

在 8.0 之前 ，Tomcat 默认采⽤的I/O⽅式为 BIO，之后改为 NIO。 ⽆论 NIO、NIO2 还是 APR， 在性能⽅⾯均优于以往的BIO。 如果采⽤APR， 甚⾄可以达到 Apache HTTP Server 的影响性能。

### Coyote 的内部组件及流程

![20201222-142920-0774.png](https://gitee.com/chuchin/img/raw/master/20201222-142920-0774.png)

Coyote 组件及作⽤

![20201222-143521-0766.png](https://gitee.com/chuchin/img/raw/master/20201222-143521-0766.png)

## Tomcat Servlet 容器 Catalina

### Tomcat 模块分层结构图及Catalina位置

Tomcat是⼀个由⼀系列可配置（conf/server.xml）的组件构成的Web容器，⽽Catalina是Tomcat的 servlet容器。

从另⼀个⻆度来说，Tomcat 本质上就是⼀款 Servlet 容器， 因为 Catalina 才是 Tomcat 的核心， 其他模块都是为Catalina 提供⽀撑的。 比如： 通过 Coyote 模块提供链接通信，Jasper 模块提供 JSP 引 擎，Naming 提供JNDI 服务，Juli 提供日志服务。

![20201222-143323-0964.png](https://gitee.com/chuchin/img/raw/master/20201222-143323-0964.png)

### Servlet 容器 Catalina 的结构

Tomcat（我们往往有⼀个认识，Tomcat就是⼀个Catalina的实例，因为Catalina是Tomcat的核心）

Tomcat/Catalina实例

![20201222-145224-0505.png](https://gitee.com/chuchin/img/raw/master/20201222-145224-0505.png)

其实，可以认为整个Tomcat就是⼀个Catalina实例，Tomcat 启动的时候会初始化这个实例，Catalina 实例通过加载server.xml完成其他实例的创建，创建并管理⼀个Server，Server创建并管理多个服务， 每个服务又可以有多个Connector和⼀个Container。

⼀个Catalina实例（容器）

⼀个 Server实例（容器）

多个Service实例（容器）

每⼀个Service实例下可以有多个Connector实例和⼀个Container实例

* Catalina

  负责解析Tomcat的配置⽂件（server.xml） , 以此来创建服务器Server组件并进⾏管理

* Server

  服务器表示整个Catalina Servlet容器以及其它组件，负责组装并启动Servlet引擎,Tomcat连接 器。Server通过实现Lifecycle接口，提供了⼀种优雅的启动和关闭整个系统的方式

* Service

  服务是Server内部的组件，⼀个Server包含多个Service。它将若⼲个Connector组件绑定到⼀个 Container

* Container

  容器，负责处理用户的servlet请求，并返回对象给web用户的模块

### Container 组件的具体结构

Container组件下有⼏种具体的组件，分别是Engine、Host、Context和Wrapper。这4种组件（容器） 是父子关系。Tomcat通过⼀种分层的架构，使得Servlet容器具有很好的灵活性。

* Engine

  表示整个Catalina的Servlet引擎，⽤来管理多个虚拟站点，⼀个Service最多只能有⼀个Engine， 但是⼀个引擎可包含多个Host

* Host

  代表⼀个虚拟主机，或者说⼀个站点，可以给Tomcat配置多个虚拟主机地址，⽽⼀个虚拟主机下可包含多个Context

* Context

  表示⼀个Web应⽤程序， ⼀个Web应⽤可包含多个Wrapper

* Wrapper

  表示⼀个Servlet，Wrapper 作为容器中的最底层，不能包含⼦容器

**上述组件的配置其实就体现在conf/server.xml中。**