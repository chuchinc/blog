---
title: "MySQL 集群 主从模式"
date: 2020-09-14T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

## 适用场景

MySQL主从模式是指数据可以从一个MySQL数据库服务器主节点复制到一个或多个从节点。MySQL 默认采用异步复制方式，这样从节点不用一直访问主服务器来更新自己的数据，从节点可以复制主数据库中的所有数据库，或者特定的数据库，或者特定的表。

![20201214-143242-0197.png](https://gitee.com/chuchin/img/raw/master/20201214-143242-0197.png)

mysql主从复制用途：

* 实时灾备，用于故障切换（高可用） 
* 读写分离，提供查询服务（读扩展） 
* 数据备份，避免影响业务（高可用）

主从部署必要条件：

* 从库服务器能连通主库
* 主库开启binlog日志（设置log-bin参数）
* 主从server-id不同

## 实现原理

### 主从复制

下图是主从复制的原理图。

![20201214-144044-0899.png](https://gitee.com/chuchin/img/raw/master/20201214-144044-0899.png)

主从复制整体分为以下三个步骤：

* 主库将数据库的变更操作记录到Binlog日志文件中
* 从库读取主库中的Binlog日志文件信息写入到从库的Relay Log中继日志中
* 从库读取中继日志信息在从库中进行Replay,更新从库数据信息

在上述三个过程中，涉及了Master的BinlogDump Thread和Slave的I/O Thread、SQL Thread，它们的作用如下：

* Master服务器对数据库更改操作记录在Binlog中，BinlogDump Thread接到写入请求后，读取 Binlog信息推送给Slave的I/O Thread。
* Slave的I/O Thread将读取到的Binlog信息写入到本地Relay Log中。
* Slave的SQL Thread检测到Relay Log的变更请求，解析relay log中内容在从库上执行。

上述过程都是异步操作，俗称异步复制，存在数据延迟现象。

下图是异步复制的时序图。

![20201214-140947-0844.png](https://gitee.com/chuchin/img/raw/master/20201214-140947-0844.png)

mysql主从复制存在的问题：

* 主库宕机后，数据可能丢失
* 从库只有一个SQL Thread，主库写压力大，复制很可能延时

解决方法：

* 半同步复制---解决数据丢失的问题
* 并行复制----解决从库复制延迟的问题

### 半同步复制

为了提升数据安全，MySQL让Master在某一个时间点等待Slave节点的 ACK（Acknowledge character）消息，接收到ACK消息后才进行事务提交，这也是半同步复制的基础，MySQL从5.5版本开 始引入了半同步复制机制来降低数据丢失的概率。

介绍半同步复制之前先快速过一下 MySQL 事务写入碰到主从复制时的完整过程，主库事务写入分为 4 个步骤：

* InnoDB Redo File Write (Prepare Write)
* Binlog File Flush & Sync to Binlog File
* nnoDB Redo File Commit（Commit Write）
* Send Binlog to Slave

当Master不需要关注Slave是否接受到Binlog Event时，即为传统的主从复制。 

当Master需要在第三步等待Slave返回ACK时，即为 after-commit，半同步复制（MySQL 5.5引入）。 

当Master需要在第二步等待 Slave 返回 ACK 时，即为 after-sync，增强半同步（MySQL 5.7引入）。

下图是 MySQL 官方对于半同步复制的时序图，主库等待从库写入 relay log 并返回 ACK 后才进行 Engine Commit。

![20201214-145050-0441.png](https://gitee.com/chuchin/img/raw/master/20201214-145050-0441.png)

## 并行复制

MySQL的主从复制延迟一直是受开发者最为关注的问题之一，MySQL从5.6版本开始追加了并行复制功 能，目的就是为了改善复制延迟问题，并行复制称为enhanced multi-threaded slave（简称MTS）。

在从库中有两个线程IO Thread和SQL Thread，都是单线程模式工作，因此有了延迟问题，我们可以采用多线程机制来加强，减少从库复制延迟。（IO Thread多线程意义不大，主要指的是SQL Thread多线 程）

在MySQL的5.6、5.7、8.0版本上，都是基于上述SQL Thread多线程思想，不断优化，减少复制延迟。

### MySQL 5.6并行复制原理

MySQL 5.6版本也支持所谓的并行复制，但是其并行只是基于库的。如果用户的MySQL数据库中是多个 库，对于从库复制的速度的确可以有比较大的帮助。

![20201214-144858-0090.png](https://gitee.com/chuchin/img/raw/master/20201214-144858-0090.png)

基于库的并行复制，实现相对简单，使用也相对简单些。基于库的并行复制遇到单库多表使用场景就发 挥不出优势了，另外对事务并行处理的执行顺序也是个大问题。

### MySQL 5.7并行复制原理

MySQL 5.7是基于组提交的并行复制，MySQL 5.7才可称为真正的并行复制，这其中最为主要的原因就 是slave服务器的回放与master服务器是一致的，即master服务器上是怎么并行执行的slave上就怎样进行并行回放。不再有库的并行复制限制。

**MySQL 5.7中组提交的并行复制究竟是如何实现的？**

MySQL 5.7是通过对事务进行分组，当事务提交时，它们将在单个操作中写入到二进制日志中。如果多个事务能同时提交成功，那么它们意味着没有冲突，因此可以在Slave上并行执行，所以通过在主库上 的二进制日志中添加组提交信息。

MySQL 5.7的并行复制基于一个前提，即所有已经处于prepare阶段的事务，都是可以并行提交的。这 些当然也可以在从库中并行提交，因为处理这个阶段的事务都是没有冲突的。在一个组里提交的事务， 一定不会修改同一行。这是一种新的并行复制思路，完全摆脱了原来一直致力于为了防止冲突而做的分 发算法，等待策略等复杂的而又效率底下的工作。

InnoDB事务提交采用的是两阶段提交模式。一个阶段是prepare，另一个是commit。

为了兼容MySQL 5.6基于库的并行复制，5.7引入了新的变量slave-parallel-type，其可以配置的值有： DATABASE（默认值，基于库的并行复制方式）、LOGICAL_CLOCK（基于组提交的并行复制方式）。

**那么如何知道事务是否在同一组中，生成的Binlog内容如何告诉Slave哪些事务是可以并行复制的？**

在MySQL 5.7版本中，其设计方式是将组提交的信息存放在GTID中。为了避免用户没有开启GTID功能 （gtid_mode=OFF），MySQL 5.7又引入了称之为Anonymous_Gtid的二进制日志event类型 ANONYMOUS_GTID_LOG_EVENT。

通过mysqlbinlog工具分析binlog日志，就可以发现组提交的内部信息。

![20201214-151801-0426.png](https://gitee.com/chuchin/img/raw/master/20201214-151801-0426.png)

可以发现MySQL 5.7二进制日志较之原来的二进制日志内容多了last_committed和 sequence_number，last_committed表示事务提交的时候，上次事务提交的编号，如果事务具有相同 的last_committed，表示这些事务都在一组内，可以进行并行的回放。

### MySQL8.0 并行复制

MySQL8.0 是基于write-set的并行复制。MySQL会有一个集合变量来存储事务修改的记录信息（主键哈希值），所有已经提交的事务所修改的主键值经过hash后都会与那个变量的集合进行对比，来判断改行是否与其冲突，并以此来确定依赖关系，没有冲突即可并行。这样的粒度，就到了 row级别了，此时并行的粒度更加精细，并行的速度会更快。

### 并行复制配置与调优

* binlog_transaction_dependency_history_size

  用于控制集合变量的大小。

* binlog_transaction_depandency_tracking

  用于控制binlog文件中事务之间的依赖关系，即last_committed值。

  * COMMIT_ORDERE: 基于组提交机制
  * WRITESET: 基于写集合机制
  * WRITESET_SESSION: 基于写集合，比writeset多了一个约束，同一个session中的事务 last_committed按先后顺序递增

* transaction_write_set_extraction

  用于控制事务的检测算法，参数值为：OFF、 XXHASH64、MURMUR32

* master_info_repository

  开启MTS功能后，务必将参数master_info_repostitory设置为TABLE，这样性能可以有50%~80% 的提升。这是因为并行复制开启后对于元master.info这个文件的更新将会大幅提升，资源的竞争 也会变大。

* slave_parallel_workers

  若将slave_parallel_workers设置为0，则MySQL 5.7退化为原单线程复制，但将 slave_parallel_workers设置为1，则SQL线程功能转化为coordinator线程，但是只有1个worker 线程进行回放，也是单线程复制。然而，这两种性能却又有一些的区别，因为多了一次 coordinator线程的转发，因此slave_parallel_workers=1的性能反而比0还要差。

* slave_preserve_commit_order

  MySQL 5.7后的MTS可以实现更小粒度的并行复制，但需要将slave_parallel_type设置为 LOGICAL_CLOCK，但仅仅设置为LOGICAL_CLOCK也会存在问题，因为此时在slave上应用事务的 顺序是无序的，和relay log中记录的事务顺序不一样，这样数据一致性是无法保证的，为了保证事 务是按照relay log中记录的顺序来回放，就需要开启参数slave_preserve_commit_order。

要开启enhanced multi-threaded slave其实很简单，只需根据如下设置：

```
slave-parallel-type=LOGICAL_CLOCK 
slave-parallel-workers=16 
slave_pending_jobs_size_max = 2147483648 
slave_preserve_commit_order=1 
master_info_repository=TABLE 
relay_log_info_repository=TABLE 
relay_log_recovery=ON
```

### 并行复制监控

在使用了MTS后，复制的监控依旧可以通过SHOW SLAVE STATUS\G，但是MySQL 5.7在 performance_schema库中提供了很多元数据表，可以更详细的监控并行复制过程。

![20201214-154010-0347.png](https://gitee.com/chuchin/img/raw/master/20201214-154010-0347.png)

通过replication_applier_status_by_worker可以看到worker进程的工作情况：

![20201214-153311-0519.png](https://gitee.com/chuchin/img/raw/master/20201214-153311-0519.png)

最后，如果MySQL 5.7要使用MTS功能，建议使用新版本，最少升级到5.7.19版本，修复了很多Bug。

## 读写分离

### 读写分离引入时机

大多数互联网业务中，往往读多写少，这时候数据库的读会首先成为数据库的瓶颈。如果我们已经优化 了SQL，但是读依旧还是瓶颈时，这时就可以选择“读写分离”架构了。
读写分离首先需要将数据库分为主从库，一个主库用于写数据，多个从库完成读数据的操作，主从库之间通过主从复制机制进行数据的同步，如图所示。

![20201214-155612-0612.png](https://gitee.com/chuchin/img/raw/master/20201214-155612-0612.png)

在应用中可以在从库追加多个索引来优化查询，主库这些索引可以不加，用于提升写效率。

读写分离架构也能够消除读写锁冲突从而提升数据库的读写性能。使用读写分离架构需要注意：**主从同步延迟和读写分配机制问题**

### 主从同步延迟

使用读写分离架构时，数据库主从同步具有延迟性，数据一致性会有影响，对于一些实时性要求比较高的操作，可以采用以下解决方案。

* 写后立刻读

  在写入数据库后，某个时间段内读操作就去主库，之后读操作访问从库。

* 二次查询

  先去从库读取数据，找不到时就去主库进行数据读取。该操作容易将读压力返还给主库，为了避免恶意攻击，建议对数据库访问API操作进行封装，有利于安全和低耦合。

* 根据业务特殊处理

  根据业务特点和重要程度进行调整，比如重要的，实时性要求高的业务数据读写可以放在主库。对于次要的业务，实时性要求不高可以进行读写分离，查询时去从库查询。

### 读写分离落地

**读写路由分配机制**是实现读写分离架构最关键的一个环节，就是控制何时去主库写，何时去从库读。目 前较为常见的实现方案分为以下两种：

* 基于编程和配置实现（应用端）

  程序员在代码中封装数据库的操作，代码中可以根据操作类型进行路由分配，增删改时操作主库， 查询时操作从库。这类方法也是目前生产环境下应用最广泛的。优点是实现简单，因为程序在代码 中实现，不需要增加额外的硬件开支，缺点是需要开发人员来实现，运维人员无从下手，如果其中 一个数据库宕机了，就需要修改配置重启项目。

* 基于服务器端代理实现（服务器端）

  ![20201214-153520-0432.png](https://gitee.com/chuchin/img/raw/master/20201214-153520-0432.png)

  中间件代理一般介于应用服务器和数据库服务器之间，从图中可以看到，应用服务器并不直接进入到master数据库或者slave数据库，而是进入MySQL proxy代理服务器。代理服务器接收到应用服务器的请求后，先进行判断然后转发到后端master和slave数据库。

目前有很多性能不错的数据库中间件，常用的有MySQL Proxy、MyCat以及Shardingsphere等等。

* MySQL Proxy：是官方提供的MySQL中间件产品可以实现负载平衡、读写分离等。
* MyCat：MyCat是一款基于阿里开源产品Cobar而研发的，基于 Java 语言编写的开源数据库中间件。
* ShardingSphere：ShardingSphere是一套开源的分布式数据库中间件解决方案，它由ShardingJDBC、Sharding-Proxy和Sharding-Sidecar（计划中）这3款相互独立的产品组成。已经在2020 年4月16日从Apache孵化器毕业，成为Apache顶级项目。
* Atlas：Atlas是由 Qihoo 360公司Web平台部基础架构团队开发维护的一个数据库中间件。
* Amoeba：变形虫，该开源框架于2008年开始发布一款 Amoeba for MySQL软件。