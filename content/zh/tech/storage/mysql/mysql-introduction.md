---
title: "MySQL 简述"
date: 2020-09-01T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

## 基础

MySQL软件下载和安装（建议版本5.7.28）熟悉MySQL工具和基本SQL操作，熟悉主键、外键、非空、唯一等约束，熟悉索引、事务概念和基本使用

Window : MySQL WorkBench, Navicat, SQLyog,HeidiSQL,MySQL Front 

Linux：MySQL WorkBeanch, Navicat 

Mac：Navicat、Sequel Pro 

## 主要知识

* MySQL架构原理和存储机制

  MySQL体系结构（内存结构、磁盘结构）、SQL运行机制、存储引擎、Undo/Redo Log等等

* MySQL索引存储机制和工作原理

  索引存储结构、索引查询原理、索引分析和优化、查询优化等

* MySQL事务和锁工作原理

  事务隔离级别、事务并发处理、锁机制和实战等

* MySQL集群架构及相关原理

  集群架构设计理念、主从架构、双主架构、分库分表等

* 互联网海量数据处理实战

  ShardingSphere、MyCat中间实战操作，分库分表实战

* MySQL第三方工具实战

  同步工具、运维工具、监控工具等

## MySQL 起源与分支

MySQL 是最流行的关系型数据库软件之一，由于其体积小、速度快、开源免费、简单易用、维护成本 低等，在集群架构中易于扩展、高可用，因此深受开发者和企业的欢迎。

![ee883c40ba5ab4e8f958438884e6cc9.png](https://i.loli.net/2020/12/09/UXn29Hc1fDMtQFh.png)

Oracle和MySQL是世界市场占比最高的两种数据库。

IOE：IBM的服务器，Oracle数据库，EMC存储设备。都是有钱的公司产品采购，例如银行、电信、石 油、证券等大企业。 

Oracle：垄断，有钱的大企业采用，互联网企业之外使用第一。 

MySQL：互联网高速发展，互联网企业使用第一。

MySQL发展历程如下：

![f9f5cf1aaa435e94fed925c91fe4477.png](https://i.loli.net/2020/12/09/ejZ5r3f9z2JD4kC.png)

MySQL主流分支如下图所示：

![8f28f4d84967d6ac60033c6c1015ffe.png](https://i.loli.net/2020/12/09/I1lLiuVxGaWfbCH.png)

MySQL从最初的1.0、3.1到后来的8.0，发生了各种各样的变化。被Oracle收购后，MySQL的版本演化 出了多个分支，除了需要付费的MySQL企业版本，还有很多MySQL社区版本。还有一条分支非常流行 的开源分支版本叫Percona Server，它是MySQL的技术支持公司Percona推出的，也是在实际工作中经 常碰到的。Percona Server在MySQL官方版本的基础上做了一些补丁和优化，同时推出了一些工具。另 外一个非常不错的版本叫MariaDB，它是MySQL的公司被Oracle收购后，MySQL的创始人Monty先生，按原来的思路重新写的一套新数据库，同时也把 InnoDB 引擎作为主要存储引擎，也算 MySQL 的分支。

## MySQL 应用架构的演变

下面是网站在不同的并发访问量级和数据量级下，MySQL应用架构的演变过程

用户请求--》 应用层 --》服务层 --》存储层

* **架构V1.0 - 单机单库**

  一个简单的小型网站或者应用背后的架构可以非常简单, 数据存储只需要一个MySQL Instance就能满足数据读取和写入需求（这里忽略掉了数据备份的实例），处于这个阶段的系统，一般会把所有的信息存到一个MySQL Instance里面。

  V1.0 瓶颈：

  * 数据量太大，超出一台服务器承受
  * 读写操作量太大，超出一台服务器承受
  * 一台服务器挂了，应用也会挂掉（可用性差）

* **架构V2.0 - 主从架构**

  V2.0架构主要解决架构V1.0下的高可用和读扩展问题，通过给Instance挂载从库解决读取的压力， 主库宕机也可以通过主从切换保障高可用。在MySQL的场景下就是通过主从结构（双主结构也属 于特殊的主从），主库抗写压力，通过从库来分担读压力，对于写少读多的应用，V2.0主从架构 完全能够胜任。

  ![80618c47e94bc815cdf29daa5cb6de8.png](https://i.loli.net/2020/12/09/jnxG3gMTO954Umk.png)

  V2.0瓶颈：

  * 数据量太大，超出一台服务器承受
  * 写操作太大，超出一台M服务器承受

* **架构V3.0 - 分库分表**

  对于V1.0和V2.0遇到写入瓶颈和存储瓶颈时，可以通过水平拆分来解决，水平拆分和垂直拆分有较大区别，垂直拆分拆完的结果，每一个实例都是拥有全部数据的，而水平拆分之后，任何实例都只有全量的1/n的数据。以下图所示，将Userinfo拆分为3个Sharding，每个Sharding持有总量的 1/3数据，3个Sharding数据的总和等于一份完整数据

  ![d867c01780b42c1d1a4a8e32b112658.png](https://i.loli.net/2020/12/09/GyQP3rufHLoRsv4.png)

  数据如何路由成为一个关键问题， 一般可以采用范围拆分，List拆分、Hash拆分等。 如何保持数据的一致性也是个难题。

* **架构V4.0 - 云数据库**

  云数据库（云计算）现在是各大IT公司内部作为节约成本的一个突破口，对于数据存储的MySQL 来说，如何让其成为一个saas（Software as a Service）是关键点。MySQL作为一个saas服务， 服务提供商负责解决可配置性，可扩展性，多用户存储结构设计等这些疑难问题

  ![b32bf33a20cdf9563052b858bec041d.png](https://i.loli.net/2020/12/09/VORnjgoZkJyaWtQ.png)