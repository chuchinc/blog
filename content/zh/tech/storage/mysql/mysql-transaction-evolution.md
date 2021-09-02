---
title: "MySQL 事务 控制演进"
date: 2020-09-10T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

## 并发事务

事务并发处理可能会带来一些问题，比如：更新丢失、脏读、不可重复读、幻读等。

* **更新丢失**

  当两个或多个事务更新同一行记录，会产生更新丢失现象。可以分为回滚覆盖和提交覆盖。

  * 回滚覆盖：一个事务回滚操作，把其他事务已提交的数据给覆盖了。
  * 提交覆盖：一个事务提交操作，把其他事务已提交的数据给覆盖了。

* **脏读**

  一个事务读取到了另一个事务修改但未提交的数据。

* **不可重复读**

  一个事务中多次读取同一行记录不一致，后面读取的跟前面读取的不一致

* **幻读**

  一个事务中多次按相同条件查询，结果不一致。后续查询的结果和面前查询结果不同，多了或少了 几行记录。

## 排队

最简单的方法，就是完全顺序执行所有事务的数据库操作，不需要加锁，简单的说就是全局排队。序列 化执行所有的事务单元，数据库某个时刻只处理一个事务操作，特点是强一致性，处理性能低。

![20201211-110833-0651.png](https://gitee.com/chuchin/img/raw/master/20201211-110833-0651.png)

## 排他锁

引入锁之后就可以支持并发处理事务，如果事务之间涉及到相同的数据项时，会使用排他锁，或叫互斥锁，先进入的事务独占数据项以后，其他事务被阻塞，等待前面的事务释放锁。

![20201211-114734-0843.png](https://gitee.com/chuchin/img/raw/master/20201211-114734-0843.png)

注意，在整个事务1结束之前，锁是不会被释放的，所以，事务2必须等到事务1结束之后开始。

## 读写锁

读和写操作：读读、写写、读写、写读。

读写锁就是进一步细化锁的颗粒度，区分读操作和写操作，让读和读之间不加锁，这样下面的两个事务就可以同时被执行了。

![20201211-115938-0570.png](https://gitee.com/chuchin/img/raw/master/20201211-115938-0570.png)

读写锁，可以让读和读并行，而读和写、写和读、写和写这几种之间还是要加排他锁。

## MVCC

多版本控制MVCC，也就是Copy on Write的思想。MVCC除了支持读和读并行，还支持读和写、写和读的并行，但为了保证一致性，写和写是无法并行的。

![20201211-131340-0387.png](https://gitee.com/chuchin/img/raw/master/20201211-131340-0387.png)

在事务1开始写操作的时候会copy一个记录的副本，其他事务读操作会读取这个记录副本，因此不会影 响其他事务对此记录的读取，实现写和读并行。

### MVCC 概念

MVCC（Multi Version Concurrency Control）被称为多版本控制，是指在数据库中为了实现高并发的数据访问，对数据进行多版本处理，并通过事务的可见性来保证事务能看到自己应该看到的数据版本。 多版本控制很巧妙地将稀缺资源的独占互斥转换为并发，大大提高了数据库的吞吐量及读写性能。

如何生成的多版本？每次事务修改操作之前，都会在Undo日志中记录修改之前的数据状态和事务号， 该备份记录可以用于其他事务的读取，也可以进行必要时的数据回滚。

### MVCC实现原理

MVCC最大的好处是读不加锁，读写不冲突。在读多写少的系统应用中，读写不冲突是非常重要的，极大的提升系统的并发性能，这也是为什么现阶段几乎所有的关系型数据库都支持 MVCC 的原因，不过目前MVCC只在 Read Commited 和 Repeatable Read 两种隔离级别下工作。

在 MVCC 并发控制中，读操作可以分为两类: 快照读（Snapshot Read）与当前读 （Current Read）。

* 快照读：读取的是记录的快照版本（有可能是历史版本），不用加锁。（select）
* 当前读：读取的是记录的最新版本，并且当前读返回的记录，都会加锁，保证其他事务不会再并发修改这条记录。（select... for update 或lock in share mode，insert/delete/update）

为了让大家更直观地理解 MVCC 的实现原理，举一个记录更新的案例来讲解 MVCC 中多版本的实现。

假设 F1～F6 是表中字段的名字，1～6 是其对应的数据。后面三个隐含字段分别对应该行的隐含ID、事务号和回滚指针，如下图所示。

![20201211-154036-0025.png](https://gitee.com/chuchin/img/raw/master/20201211-154036-0025.png)

具体的更新过程如下：

假如一条数据是刚 INSERT 的，DB_ROW_ID 为 1，其他两个字段为空。当事务 1 更改该行的数据值 时，会进行如下操作，如下图所示。

![20201211-155740-0162.png](https://gitee.com/chuchin/img/raw/master/20201211-155740-0162.png)

MVCC已经实现了读读、读写、写读并发处理，如果想进一步解决写写冲突，可以采用下面两种方案：

* 乐观锁
* 悲观锁



​    