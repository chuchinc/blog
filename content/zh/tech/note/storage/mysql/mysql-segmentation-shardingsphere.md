---
title: "MySQL ShardingSphere实战"
date: 2020-09-18T10:21:43+08:00
draft: false
tags: ["MySQL","ShardingSphere"]
---

## ShardingSphere 介绍

Apache ShardingSphere是一款开源的分布式数据库中间件组成的生态圈。它由Sharding-JDBC、 Sharding-Proxy和Sharding-Sidecar（规划中）这3款相互独立的产品组成。 他们均提供标准化的数据 分片、分布式事务和数据库治理功能，可适用于如Java同构、异构语言、容器、云原生等各种多样化的 应用场景。

ShardingSphere项目状态如下：

![20201223-143757-0407.png](https://gitee.com/chuchin/img/raw/master/20201223-143757-0407.png)

ShardingSphere定位为关系型数据库中间件，旨在充分合理地在分布式的场景下利用关系型数据库的 计算和存储能力，而并非实现一个全新的关系型数据库。

![20201223-152714-0083.png](https://gitee.com/chuchin/img/raw/master/20201223-152714-0083.png)

* Sharding-JDBC：被定位为轻量级Java框架，在Java的JDBC层提供的额外服务，以jar包形式使用。
* Sharding-Proxy：被定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
* Sharding-Sidecar：被定位为Kubernetes或Mesos的云原生数据库代理，以DaemonSet的形式代理所有对数据库的访问。

![20201223-152516-0853.png](https://gitee.com/chuchin/img/raw/master/20201223-152516-0853.png)

Sharding-JDBC、Sharding-Proxy和Sharding-Sidecar三者区别如下：

![20201223-152917-0922.png](https://gitee.com/chuchin/img/raw/master/20201223-152917-0922.png)

ShardingSphere安装包下载：https://shardingsphere.apache.org/document/current/cn/downloads/

![20201223-150926-0344.png](https://gitee.com/chuchin/img/raw/master/20201223-150926-0344.png)

使用Git下载工程：git clone https://github.com/apache/incubator-shardingsphere.git

## Sharding-JDBC

Sharding-JDBC定位为轻量级Java框架，在Java的JDBC层提供的额外服务。 它使用客户端直连数据库， 以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM 框架的使用。

* 适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使 用JDBC。
* 基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
* 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![20201223-150453-0387.png](https://gitee.com/chuchin/img/raw/master/20201223-150453-0387.png)

Sharding-JDBC主要功能：

* 数据分片
  * 分库、分表
  * 读写分离
  * 分片策略
  * 分布式主键
* 分布式事务
  * 标准化的事务接口
  * XA强一致性事务
  * 柔性事务
* 数据库治理
  * 配置动态化
  * 编排和治理
  * 数据脱敏
  * 可视化链路追踪

**Sharding-JDBC 内部结构：**

![1608710403623](C:\Users\ChuChinRCC\AppData\Roaming\Typora\typora-user-images\1608710403623.png)

* 图中黄色部分表示的是Sharding-JDBC的入口API，采用工厂方法的形式提供。 目前有 ShardingDataSourceFactory和MasterSlaveDataSourceFactory两个工厂类。
  * ShardingDataSourceFactory支持分库分表、读写分离操作
  * MasterSlaveDataSourceFactory支持读写分离操作
* 图中蓝色部分表示的是Sharding-JDBC的配置对象，提供灵活多变的配置方式。 ShardingRuleConfiguration是分库分表配置的核心和入口，它可以包含多个 TableRuleConfiguration和MasterSlaveRuleConfiguration。
  * TableRuleConfiguration封装的是表的分片配置信息，有5种配置形式对应不同的 Configuration类型。
  * MasterSlaveRuleConfiguration封装的是读写分离配置信息。
* 图中红色部分表示的是内部对象，由Sharding-JDBC内部使用，应用开发者无需关注。ShardingJDBC通过ShardingRuleConfiguration和MasterSlaveRuleConfiguration生成真正供 ShardingDataSource和MasterSlaveDataSource使用的规则对象。ShardingDataSource和 MasterSlaveDataSource实现了DataSource接口，是JDBC的完整实现方案。

**Sharding-JDBC初始化流程：**

* 根据配置的信息生成Configuration对象
* 通过Factory会将Configuration对象转化为Rule对象
* 通过Factory会将Rule对象与DataSource对象封装
* Sharding-JDBC使用DataSource进行分库分表和读写分离操作

**Sharding-JDBC 使用过程：**

* 引入maven依赖

  ```xml
  <dependency> 
      <groupId>org.apache.shardingsphere</groupId> 
      <artifactId>sharding-jdbc-core</artifactId> 
      <version>${latest.release.version}</version>
  </dependency>
  ```

  注意: 请将${latest.release.version}更改为实际的版本号。

* 规则配置

  Sharding-JDBC可以通过Java，YAML，Spring命名空间和Spring Boot Starter四种方式配置，开 发者可根据场景选择适合的配置方式。

* 创建DataSource

  通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，然后即可通过 DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。

  ```java
  DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, properties);
  ```

## 数据分片剖析实战

### 核心概念

* 表概念

  * 真实表

    数据库中真实存在的物理表。例如b_order0、b_order1

  * 逻辑表

    在分片之后，同一类表结构的名称（总成）。例如b_order。

  * 数据节点

    在分片之后，由数据源和数据表组成。例如ds0.b_order1

  * 绑定表

    指的是分片规则一致的关系表（主表、子表），例如b_order和b_order_item，均按照 order_id分片，则此两个表互为绑定表关系。绑定表之间的多表关联查询不会出现笛卡尔积关联，可以提升关联查询效率。

    ```sql
    b_order：b_order0、b_order1 b_order_item：b_order_item0、b_order_item1
    select * from b_order o join b_order_item i on(o.order_id=i.order_id) where o.order_id in (10,11);
    ```

    如果不配置绑定表关系，采用笛卡尔积关联，会生成4个SQL

    ```sql
    select * from b_order0 o join b_order_item0 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    select * from b_order0 o join b_order_item1 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    select * from b_order1 o join b_order_item0 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    select * from b_order1 o join b_order_item1 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    ```

    如果配置绑定表关系，生成2个SQL

    ```sql
    select * from b_order0 o join b_order_item0 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    select * from b_order1 o join b_order_item1 i on(o.order_id=i.order_id) where o.order_id in (10,11);
    ```

  * 广播表

    在使用中，有些表没必要做分片，例如字典表、省份信息等，因为他们数据量不大，而且这 种表可能需要与海量数据的表进行关联查询。广播表会在不同的数据节点上进行存储，存储 的表结构和数据完全相同。

* 分片算法（ShardingAlgorithm）

  由于分片算法和业务实现紧密相关，因此并未提供内置分片算法，而是通过分片策略将各种场景提 炼出来，提供更高层级的抽象，并提供接口让应用开发者自行实现分片算法。目前提供4种分片算法。

  * 精确分片算法PreciseShardingAlgorithm

    用于处理使用单一键作为分片键的=与IN进行分片的场景。

  * 范围分片算法RangeShardingAlgorithm

    用于处理使用单一键作为分片键的BETWEEN AND、>、<、>=、<=进行分片的场景。

  * 复合分片算法ComplexKeysShardingAlgorithm

    用于处理使用多键作为分片键进行分片的场景，多个分片键的逻辑较复杂，需要应用开发者 自行处理其中的复杂度。

  * Hint分片算法HintShardingAlgorithm

    用于处理使用Hint行分片的场景。对于分片字段非SQL决定，而由其他外置条件决定的场景，可使用SQL Hint灵活的注入分片字段。例：内部系统，按照员工登录主键分库，而数据 库中并无此字段。SQL Hint支持通过Java API和SQL注释两种方式使用。

* 分片策略（ShardingStrategy）

  分片策略包含分片键和分片算法，真正可用于分片操作的是分片键 + 分片算法，也就是分片策 略。目前提供5种分片策略。

  * 标准分片策略StandardShardingStrategy

    只支持单分片键，提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。 提供PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法。 PreciseShardingAlgorithm是必选的，RangeShardingAlgorithm是可选的。但是SQL中使用 了范围操作，如果不配置RangeShardingAlgorithm会采用全库路由扫描，效率低。

  * 复合分片策略ComplexShardingStrategy

    支持多分片键。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。由 于多分片键之间的关系复杂，因此并未进行过多的封装，而是直接将分片键值组合以及分片 操作符透传至分片算法，完全由应用开发者实现，提供最大的灵活度。

  * 行表达式分片策略InlineShardingStrategy

    只支持单分片键。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，对于 简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发。如: t_user_$-> {u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

  * Hint分片策略HintShardingStrategy

    通过Hint指定分片值而非从SQL中提取分片值的方式进行分片的策略。

  * 不分片策略NoneShardingStrategy

    不分片的策略。

* 分片策略配置

  对于分片策略存有数据源分片策略和表分片策略两种维度，两种策略的API完全相同。

  * 数据源分片策略

    用于配置数据被分配的目标数据源。

  * 表分片策略

    用于配置数据被分配的目标表，由于表存在与数据源内，所以表分片策略是依赖数据源分片策略结果的。

### 流程剖析

ShardingSphere 3个产品的数据分片功能主要流程是完全一致的，如下图所示。

![20201223-170402-0515.png](https://gitee.com/chuchin/img/raw/master/20201223-170402-0515.png)

* SQL解析

  SQL解析分为词法解析和语法解析。 先通过词法解析器将SQL拆分为一个个不可再分的单词。再使 用语法解析器对SQL进行理解，并最终提炼出解析上下文。

  Sharding-JDBC采用不同的解析器对SQL进行解析，解析器类型如下：

  * MySQL解析器
  * Oracle解析器
  * SQLServer解析器
  * PostgreSQL解析器
  * 默认SQL解析器

* 查询优化

  负责合并和优化分片条件，如OR等。

* SQL路由

  根据解析上下文匹配用户配置的分片策略，并生成路由路径。目前支持分片路由和广播路由。

* SQL改写

  将SQL改写为在真实数据库中可以正确执行的语句。SQL改写分为正确性改写和优化改写。

* SQL执行

  通过多线程执行器异步执行SQL。

* 结果归并

  将多个执行结果集归并以便于通过统一的JDBC接口输出。结果归并包括流式归并、内存归并和使用装饰者模式的追加归并这几种方式。

### SQL使用规范

### 其他功能