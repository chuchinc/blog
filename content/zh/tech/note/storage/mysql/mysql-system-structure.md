---
title: "MySQL 架构原理 体系架构"
date: 2020-09-02T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

![20201209-140825-0791.png](https://gitee.com/chuchin/img/raw/master/20201209-140825-0791.png)

MySQL Server架构自顶向下大致可以分网络连接层、服务层、存储引擎层和系统文件层。

 ## 网络连接层

客户端连接器（Client Connectors）：提供与MySQL服务器建立的支持。目前几乎支持所有主流 的服务端编程技术，例如常见的 Java、C、Python、.NET等，它们通过各自API技术与MySQL建立连接。

## 服务层（MySQL Server）

服务层是MySQL Server的核心，主要包含系统管理和控制工具、连接池、SQL接口、解析器、查询优化器和缓存六个部分。

* **连接池（Connection Pool）**：负责存储和管理客户端与数据库的连接，一个线程负责管理一个连接。

* **系统管理和控制工具（Management Services & Utilities）**：例如备份恢复、安全管理、集群管理等。

* **SQL接口（SQL Interface）**：用于接受客户端发送的各种SQL命令，并且返回用户需要查询的结果。比如DML、DDL、存储过程、视图、触发器等。

* **解析器（Parser）**：负责将请求的SQL解析生成一个"解析树"。然后根据一些MySQL规则进一步检查解析树是否合法。

* **查询优化器（Optimizer）**：当“解析树”通过解析器语法检查后，将交由优化器将其转化成执行计划，然后与存储引擎交互。

  ```sql
  select uid,name from user where gender=1;
  ```

  选取--》投影--》联接 策略 

  1. select先根据where语句进行选取，并不是查询出全部数据再过滤
  2. select查询根据uid和name进行属性投影，并不是取出所有字段
  3. 将前面选取和投影联接起来最终生成查询结果

* **缓存（Cache&Buffer）**：缓存机制是由一系列小缓存组成的。比如表缓存，记录缓存，权限缓存，引擎缓存等。如果查询缓存有命中的查询结果，查询语句就可以直接去查询缓存中取数据。

## 存储引擎层（Pluggable Storage Engines）

存储引擎负责MySQL中数据的存储与提取，与底层系统文件进行交互。MySQL存储引擎是插件式的， 服务器中的查询执行引擎通过接口与存储引擎进行通信，接口屏蔽了不同存储引擎之间的差异 。现在有 很多种存储引擎，各有各的特点，最常见的是MyISAM和InnoDB。

## 系统文件层（File System）

该层负责将数据库的数据和日志存储在文件系统之上，并完成与存储引擎的交互，是文件的物理存储层。主要包含日志文件，数据文件，配置文件，pid 文件，socket 文件等。

* 日志文件

  * 错误日志（Error log）

    默认开启，show variables like '%log_error%'

  * 通用查询日志（General query log）

    记录一般查询语句，show variables like '%general%';

  * 二进制日志（binary log）

    记录了对MySQL数据库执行的更改操作，并且记录了语句的发生时间、执行时长；但是它不 记录select、show等不修改数据库的SQL。主要用于数据库恢复和主从复制。

    show variables like '%log_bin%'; //是否开启

    show variables like '%binlog%'; //参数查看

    show binary logs;//查看日志文件

  * 慢查询日志（Slow query log）

    记录所有执行时间超时的查询SQL，默认是10秒。

    show variables like '%slow_query%'; //是否开启 

    show variables like '%long_query_time%'; //时长

* 配置文件

  用于存放MySQL所有的配置信息文件，比如my.cnf、my.ini等。

* 数据文件

  * db.opt 文件：记录这个库的默认使用的字符集和校验规则。
  * frm 文件：存储与表相关的元数据（meta）信息，包括表结构的定义信息等，每一张表都会 有一个frm 文件。
  * MYD 文件：MyISAM 存储引擎专用，存放 MyISAM 表的数据（data)，每一张表都会有一个 .MYD 文件。
  * MYI 文件：MyISAM 存储引擎专用，存放 MyISAM 表的索引相关信息，每一张 MyISAM 表对 应一个 .MYI 文件。
  * ibd文件和 IBDATA 文件：存放 InnoDB 的数据文件（包括索引）。InnoDB 存储引擎有两种 表空间方式：独享表空间和共享表空间。独享表空间使用 .ibd 文件来存放数据，且每一张 InnoDB 表对应一个 .ibd 文件。共享表空间使用 .ibdata 文件，所有表共同使用一个（或多 个，自行配置）.ibdata 文件。
  * ibdata1 文件：系统表空间数据文件，存储表元数据、Undo日志等 。
  * ib_logfile0、ib_logfile1 文件：Redo log 日志文件。

* pid 文件

  pid 文件是 mysqld 应用程序在 Unix/Linux 环境下的一个进程文件，和许多其他 Unix/Linux 服务 端程序一样，它存放着自己的进程 id。

* socket 文件

  socket 文件也是在 Unix/Linux 环境下才有的，用户在 Unix/Linux 环境下客户端连接可以不通过 TCP/IP 网络而直接使用 Unix Socket 来连接 MySQL。
