---
title: "SCN核心组件 Stream"
date: 2020-10-10T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Stream"]
---

*本文源代码下载：[spring-cloud-stream.zip](/file/springcloud/spring-cloud-stream.zip)*

Spring Cloud Stream 消息驱动组件帮助我们更快速，更⽅便，更友好的去构建消息驱动微服务的。 当时定时任务和消息驱动的⼀个对比。（消息驱动：基于消息机制做⼀些事情） MQ：消息队列/消息中间件/消息代理，产品有很多，ActiveMQ RabbitMQ RocketMQ Kafka

## Stream 解决的痛点问题

MQ消息中间件⼴泛应⽤在应⽤解耦合、异步消息处理、流量削峰等场景中。

不同的MQ消息中间件内部机制包括使⽤⽅式都会有所不同，比如RabbitMQ中有Exchange（交换机/交换器）这⼀概念，kafka有Topic、Partition分区这些概念，MQ消息中间件的差异性不利于我们上层的开发应用，当我们的系统希望从原有的RabbitMQ切换到Kafka时，我们会发现⽐较困难，很多要操作可能重来，**因为应⽤程序和具体的某⼀款MQ消息中间件耦合在⼀起了**。

Spring Cloud Stream进⾏了很好的上层抽象，可以让我们与具体消息中间件解耦合，屏蔽掉了底层具体MQ消息中间件的细节差异，就像Hibernate屏蔽掉了具体数据库（Mysql/Oracle⼀样）。如此⼀来，我们学习、开发、维护MQ都会变得轻松。目前Spring Cloud Stream⽀持RabbitMQ和Kafka。

本质：**屏蔽掉了底层不同MQ消息中间件之间的差异，统⼀了MQ的编程模型，降低了学习、开发、维护MQ的成本**

## Stream 重要概念

Spring Cloud Stream 是⼀个构建消息驱动微服务的框架。应⽤程序通过inputs（相当于消息消费者 consumer）或者outputs（相当于消息⽣产者producer）来与Spring Cloud Stream中的binder对象交互，而Binder对象是⽤来屏蔽底层MQ细节的，它负责与具体的消息中间件交互。

**对于我们来说，只需要知道如何使⽤Spring Cloud Stream与Binder对象交互即可**

**Binder绑定器**

Binder绑定器是Spring Cloud Stream 中非常核心的概念，就是通过它来屏蔽底层不同MQ消息中间件的细节差异，当需要更换为其他消息中间件时，我们需要做的就是更换对应的Binder绑定器而不需要修改任何应用逻辑（Binder绑定器的实现是框架内置的，Spring Cloud Stream⽬前⽀持Rabbit、Kafka两种消息队列）

## 传统MQ模型与Stream消息驱动模型

## Steam 消息通信方式及编程模型

### Stream 消息通信方式

Stream中的消息通信方式遵循了发布—订阅模式。

在Spring Cloud Stream中的消息通信方式遵循了发布-订阅模式，当⼀条消息被投递到消息中间件之后，它会通过共享的 Topic 主题进行广播，消息消费者在订阅的主题中收到它并触发⾃身的业务逻辑处理。这⾥所提到的 Topic 主题是Spring Cloud Stream中的⼀个抽象概念，⽤来代表发布共享消息给消 费者的地方。在不同的消息中间件中， Topic 可能对应着不同的概念，比如：在RabbitMQ中的它对应 了Exchange、在Kakfa中则对应了Kafka中的Topic。

### Stream 编程注解

**如下的注解无非在做⼀件事，把我们结构图中那些组成部分上下关联起来，打通通道（这样的话生产者 的message数据才能进⼊mq，mq中数据才能进⼊消费者工程）**

| 注解                                                     | 描述                                                       |
| -------------------------------------------------------- | ---------------------------------------------------------- |
| @Input（在消费者工程中使用）                             | 注解标识输入通道，通过该输入通道接收到的消息进入应用程序   |
| @Output（在⽣产者⼯程中使⽤）                            | 注解标识输出通道，发布的消息将通过该通道离开应用程序       |
| @StreamListener（在消费者工程中使用，监听message的到来） | 监听队列，⽤于消费者的队列的消息的接收 （有消息监听.....） |
| @EnableBinding                                           | 把Channel和Exchange（对于RabbitMQ）绑定在⼀起              |

接下来，创建三个工程（基于RabbitMQ，安装过程略 [Messaging that just works — RabbitMQ](https://www.rabbitmq.com/)）

* cloud-stream-producer-9090 生产者端发消息
* cloud-stream-consumer-9091 消费者端接收信息
* cloud-stream-consumer-9092 消费者端接收信息

### 开发生产者端

1. 新建子模块：cloud-stream-producer-9090
2. 添加依赖

```xml
 <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    </dependency>
  </dependencies>
```

3. application.yml 配置

```yaml
server:
  port: 9090
#注册到Eureka服务中心
eureka:
  client:
    service-url:
      # 注册到集群，就把多个Eurekaserver地址使用逗号连接起来即可；注册到单实例（非集群模式），那就写一个就ok
      defaultZone: http://a.eureka.server:8761/eureka,http://b.eureka.server:8762/eureka
  instance:
    prefer-ip-address: true  #服务实例中显示ip，而不是显示主机名（兼容老的eureka版本）
    # 实例名称： 192.168.1.103:lagou-service-resume:8080，我们可以自定义它
    instance-id: ${spring.cloud.client.ip-address}:${spring.application.name}:${server.port}:@project.version@
spring:
  application:
    name: cloud-stream-producer
  cloud:
    stream:
      binders: #绑定mq的信息，此处是RabbitMQ
        edmRabbitBinder: #给binder自定义的名成，用于后面的关联
          type: rabbit #mq类型
          environment: # MQ环境配置（用户名、密码等）
            spring:
              rabbitmq:
                host: 172.30.193.64
                port: 5672
                username: admin
                password: 111111
      bindings: # 关联整合通道和binder对象
        output: # output是我们定义的通道名称，此处不能乱改
          destination: lagouExchange # 要使用的Exchange名称（消息队列主题名称）
          content-type: text/plain # application/json # 消息类型设置，比如json
          binder: edmRabbitBinder # 关联MQ服务
```

4. 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class StreamProducerApplication9090 {

    public static void main(String[] args) {
        SpringApplication.run(StreamProducerApplication9090.class, args);
    }

}
```

5. 业务类开发

```java
public interface IMessageProducer {

    void sendMessage(String content);

}
```

```java
//Source.class里面就是对输出通道的定义（这是Spring Cloud Stream 内置的通道封装）
@EnableBinding(Source.class)
public class MessageProducerImpl implements IMessageProducer{

    // 将MessageChannel的封装对象Source注⼊到这⾥使⽤
    @Autowired
    private Source source;

    //将MessageChannel的封装对象Source注入到这里使用
    @Override
    public void sendMessage(String content) {
        // 向mq中发送消息（并不是直接操作mq，应该操作的是spring cloud stream）
        // 使⽤通道向外发出消息(指的是Source⾥⾯的output通道)
        source.output().send(MessageBuilder.withPayload(content).build());
    }
}
```

6. 定时任务发送消息

```java
#启动类加上注解开启
@EnableScheduling
```

```java
@Component
@Slf4j
public class SendScheduled {

    @Autowired
    private IMessageProducer iMessageProducer;

    @Scheduled(cron = "*/5 * * * * ?")
    public void testSendMessage() {
        iMessageProducer.sendMessage("Hello world");
        log.info("======send=======");
    }
}
```

### 开发消费端

1. 新建子模块：cloud-stream-consumer-9091，9092工程类似，仅列出需要修改部分

```yaml
server:
  port: 9091 #修改端口
spring:
  application:
    name: cloud-stream-consumer
  cloud:
    stream:
      bindind:
        input: #ouput改为input
```

2. 消息消费者监听

```java
@Slf4j
@EnableBinding(Sink.class)
public class MessageConsumerService {

    @StreamListener(Sink.INPUT)
    public void receiveMessage(Message<String> message) {
        log.info("==========接收到的消息：" + message);
    }

}
```

## 自定义消息通道

Stream 内置了两种接⼝Source和Sink分别定义了 binding 为 “input” 的输⼊流和 “output” 的输出流， 我们也可以⾃定义各种输⼊输出流（通道），但实际我们可以在我们的服务中使⽤多个binder、多个输入通道和输出通道，然⽽默认就带了⼀个input的输⼊通道和⼀个output的输出通道，怎么办？ 我们是可以⾃定义消息通道的，学着Source和Sink的样⼦，给你的通道定义个⾃⼰的名字，多个输入通道和输出通道是可以写在⼀个类中的。

定义接口

```java 
interface CustomChannel { 
    String INPUT_LOG = "inputLog"; 
    String OUTPUT_LOG = "outputLog";
    
	@Input(INPUT_LOG) 
    SubscribableChannel inputLog();
    
	@Output(OUTPUT_LOG) 
    MessageChannel outputLog();
}
```

如何使用？

1. 在 @EnableBinding 注解中，绑定⾃定义的接⼝
2. 使⽤ @StreamListener 做监听的时候，需要指定 CustomChannel.INPUT_LOG

```yaml
bindings: 
  inputLog:
    destination: aExchange 
  outputLog: 
    destination: bduExchange
```

## 消息分组

如上我们的情况，消费者端有两个（消费同⼀个MQ的同⼀个主题），但是呢我们的业务场景中希望这 个主题的⼀个Message只能被⼀个消费者端消费处理，此时我们就可以使⽤消息分组。

**解决的问题：能解决消息重复消费问题**

我们仅仅需要在服务消费者端设置 spring.cloud.stream.bindings.input.group 属性，多个消费者实例 配置为同⼀个group名称（在同⼀个group中的多个消费者只有⼀个可以获取到消息并消费）。

```yaml
bindings: # 关联整合通道和binder对象
        input: # output是我们定义的通道名称，此处不能乱改
          destination: edmExchange # 要使用的Exchange名称（消息队列主题名称）
          content-type: text/plain # application/json # 消息类型设置，比如json
          binder: edmRabbitBinder # 关联MQ服务
          group: a
```