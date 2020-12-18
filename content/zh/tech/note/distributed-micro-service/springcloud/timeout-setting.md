---
title: "各组件超时时间设置"
date: 2020-10-12T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud"]
---

在SpringCloud中，应⽤的组件较多，只要涉及通信，就有可能会发⽣请求超时。那么如何设置超时时间？ 在 Spring Cloud 中，超时时间只需要重点关注 Ribbon 和 Hystrix 即可。

## Ribbon 设置

如果采⽤的是服务发现⽅式，就可以通过服务名去进⾏转发，需要配置Ribbon的超时。 Ribbon的超时可以配置全局的ribbon.ReadTimeout和ribbon.ConnectTimeout。也可以在前⾯指定服务名，为每个服务单独配置，比如 user-service.ribbon.ReadTimeout。

其次是Hystrix的超时配置，**Hystrix的超时时间要⼤于Ribbon的超时时间**，因为Hystrix将请求包装了起来，特别需要注意的是，如果Ribbon开启了重试机制，⽐如重试3 次，Ribbon 的超时为 1 秒，那么 Hystrix 的超时时间应该⼤于 3 秒，否则就会出现 Ribbon 还在重试中，而 Hystrix 已经超时的现象。

## Hystrix 设置

Hystrix全局超时配置就可以⽤default来代替具体的command名称。 hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=3000 如果想对具体的 command 进行配置，那么就需要知道 command 名称的⽣成规则，才能准确的配置。

如果我们使⽤ @HystrixCommand 的话，可以⾃定义 commandKey。如果使⽤FeignClient的话，可以 为FeignClient来指定超时时间： hystrix.command.UserRemoteClient.execution.isolation.thread.timeoutInMilliseconds = 3000

如果想对FeignClient中的某个接⼝设置单独的超时，可以在FeignClient名称后加上具体的⽅法： hystrix.command.UserRemoteClient#getUser(Long).execution.isolation.thread.timeoutInMilliseconds = 3000

## Feign 设置

Feign本身也有超时时间的设置，如果此时设置了Ribbon的时间就以Ribbon的时间为准，如果没设置Ribbon的时间但配置了Feign的时间，就以Feign的时间为准。Feign的时间同样也配置了连接超时 时间（feign.client.config.服务名称.connectTimeout）和读取超时时间（feign.client.config.服务名 称.readTimeout）。