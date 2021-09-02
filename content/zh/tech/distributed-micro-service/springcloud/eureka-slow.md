---
title: "Eureka 服务发现时间慢"
date: 2020-10-11T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","Eureka"]
---

问题场景上线⼀个新的服务实例，但是服务消费者无感知，过了⼀段时间才知道 某⼀个服务实例下线了，服务消费者⽆感知，仍然向这个服务实例在发起请求
这其实就是服务发现的⼀个问题，当我们需要调用服务实例时，信息是从注册中心Eureka获取的，然后通过Ribbon选择⼀个服务实例发起调⽤，如果出现调⽤不到或者下线后还可以调⽤的问题，原因肯定是服务实例的信息更新不及时导致的。

Eureka 服务发现慢的原因主要有两个，⼀部分是因为服务缓存导致的，另⼀部分是因为客户端缓存导致的。

## 服务端缓存

服务注册到注册中⼼后，服务实例信息是存储在注册表中的，也就是内存中。但Eureka为了提⾼响应速 度，在内部做了优化，加⼊了两层的缓存结构，将Client需要的实例信息，直接缓存起来，获取的时候 直接从缓存中拿数据然后响应给 Client。

第⼀层缓存是**readOnlyCacheMap**，readOnlyCacheMap是 采⽤ConcurrentHashMap来存储数据的，主要负责定时与readWriteCacheMap进⾏数据同步，默认同 步时间为 30 秒⼀次。

第⼆层缓存是**readWriteCacheMap**，readWriteCacheMap采⽤Guava来实现缓存。缓存过期时间默认 为180秒，当服务下线、过期、注册、状态变更等操作都会清除此缓存中的数据。

Client获取服务实例数据时，会先从⼀级缓存中获取，如果⼀级缓存中不存在，再从⼆级缓存中获取， 如果⼆级缓存也不存在，会触发缓存的加载，从存储层拉取数据到缓存中，然后再返回给 Client。

Eureka 之所以设计⼆级缓存机制，也是为了提⾼ Eureka Server 的响应速度，缺点是缓存会导致 Client 获取不到最新的服务实例信息，然后导致⽆法快速发现新的服务和已下线的服务。 了解了服务端的实现后，想要解决这个问题就变得很简单了，我们可以缩短只读缓存的更新时间 （eureka.server.response-cache-update-interval-ms）让服务发现变得更加及时，或者直接将只读缓 存关闭（eureka.server.use-read-only-response-cache=false），多级缓存也导致C层⾯（数据⼀致 性）很薄弱。 Eureka Server 中会有定时任务去检测失效的服务，将服务实例信息从注册表中移除，也可以将这个失 效检测的时间缩短，这样服务下线后就能够及时从注册表中清除。

## 客户端缓存

客户端缓存 客户端缓存主要分为两块内容，⼀块是 Eureka Client 缓存，⼀块是 Ribbon 缓存。

**Eureka Client缓存**

Eureka Client 负责跟Eureka Server 进行交互，在Eureka Client中的 com.netflix.discovery.DiscoveryClient.initScheduledTasks()方法中，初始化了一个CacheRefreshThread 定时任务专门用来拉取 Eureka Server 的实例信息到本地。所以我们需要缩短这个定时拉取服务信息的时间间隔（eureka.client.registryFetchIntervalSeconds） 来快速发现新的服务。

**Ribbon缓存**

Ribbon会从EurekaClient中获取服务信息，ServerListUpdater是Ribbon中负责服务实例 更新的组件，默认的实现是PollingServerListUpdater，通过线程定时去更新实例信息。定时刷新的时间间隔默认是30秒，当服务停⽌或者上线后，这边最快也需要30秒才能将实例信息更新成最新的。我们 可以将这个时间调短⼀点，比如 3 秒。

刷新间隔的参数是通过 getRefreshIntervalMs ⽅法来获取的，⽅法中的逻辑也是从 Ribbon 的配置中进 ⾏取值的。

将这些服务端缓存和客户端缓存的时间全部缩短后，跟默认的配置时间相比，快了很多。我们通过调整参数的⽅式来尽量加快服务发现的速度，但是还是不能完全解决报错的问题，间隔时间设置为3秒，也 还是会有间隔。所以我们⼀般都会开启重试功能，当路由的服务出现问题时，可以重试到另⼀个服务来 保证这次请求的成功。