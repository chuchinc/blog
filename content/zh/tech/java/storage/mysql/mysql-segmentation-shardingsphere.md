---
title: "MySQL ShardingSphere实战"
date: 2020-09-18T10:21:43+08:00
draft: false
tags: ["MySQL","ShardingSphere"]
---

*本文源代码下载：[mysql-example.zip](/file/mysql/mysql-example.zip)*

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

* 支持项

  * 路由至单数据节点时，目前MySQL数据库100%全兼容，其他数据库完善中。

  * 路由至多数据节点时，全面支持DQL、DML、DDL、DCL、TCL。支持分页、去重、排 序、分组、聚合、关联查询（不支持跨库关联）。以下用最为复杂的查询为例：

    ```mysql
    SELECT select_expr [, select_expr ...] FROM table_reference [, table_reference ...] [WHERE predicates]
    [GROUP BY {col_name | position} [ASC | DESC], ...] [ORDER BY {col_name | position} [ASC | DESC], ...] [LIMIT {[offset,] row_count | row_count OFFSET offset}]
    ```

* 不支持项（路由至多数据节点）

  * 不支持CASE WHEN、HAVING、UNION (ALL)

* 支持分页子查询，但其他子查询有限支持，无论嵌套多少层，只能解析至第一个包含数据表 的子查询，一旦在下层嵌套中再次找到包含数据表的子查询将直接抛出解析异常。 例如，以下子查询可以支持：

  ```mysql
  SELECT COUNT(*) FROM (SELECT * FROM b_order o)
  ```

  以下子查询不支持：

  ```mysql
  SELECT COUNT(*) FROM (SELECT * FROM b_order o WHERE o.id IN (SELECT id FROM b_order WHERE status = ?))
  ```

  简单来说，通过子查询进行非功能需求，在大部分情况下是可以支持的。比如分页、统计总 数等；而通过子查询实现业务查询当前并不能支持。

* 由于归并的限制，子查询中包含聚合函数目前无法支持。

* 不支持包含schema的SQL。因为ShardingSphere的理念是像使用一个数据源一样使用多数据源，因此对SQL的访问都是在同一个逻辑schema之上。

* 当分片键处于运算表达式或函数中的SQL时，将采用全路由的形式获取结果。

  例如下面SQL，create_time为分片键：

  ```mysql
  SELECT * FROM b_order WHERE to_date(create_time, 'yyyy-mm-dd') = '202005-05';
  ```

  由于ShardingSphere只能通过SQL字面提取用于分片的值，因此当分片键处于运算表达式 或函数中时，ShardingSphere无法提前获取分片键位于数据库中的值，从而无法计算出真正 的分片值。

不支持的SQL示例：

```mysql
INSERT INTO tbl_name (col1, col2, …) VALUES(1+2, ?, …) //VALUES语句不支持运算 表达式 INSERT INTO tbl_name (col1, col2, …) SELECT col1, col2, … FROM tbl_name WHERE col3 = ?
//INSERT .. SELECT
SELECT COUNT(col1) as count_alias FROM tbl_name GROUP BY col1 HAVING count_alias > ?
//HAVING SELECT * FROM tbl_name1 UNION SELECT * FROM tbl_name2
SELECT * FROM ds.tbl_name1 //包含schema SELECT SUM(DISTINCT col1), SUM(col1) FROM tbl_name
//UNION SELECT * FROM tbl_name1 UNION ALL SELECT * FROM tbl_name2 //UNION ALL //同时使用普通聚合函数
和DISTINCT SELECT * FROM tbl_name WHERE to_date(create_time, ‘yyyy-mm-dd’) = ? //会导致 全路由
```

* 分页查询

  完全支持MySQL和Oracle的分页查询，SQLServer由于分页查询较为复杂，仅部分支持.

  * 性能瓶颈：

    查询偏移量过大的分页会导致数据库获取数据性能低下，以MySQL为例：

    ```mysql
    SELECT * FROM b_order ORDER BY id LIMIT 1000000, 10
    ```

    这句SQL会使得MySQL在无法利用索引的情况下跳过1000000条记录后，再获取10条记录， 其性能可想而知。 而在分库分表的情况下（假设分为2个库），为了保证数据的正确性，SQL 会改写为：

    ```mysql
    SELECT * FROM b_order ORDER BY id LIMIT 0, 1000010
    ```

    即将偏移量前的记录全部取出，并仅获取排序后的最后10条记录。这会在数据库本身就执行 很慢的情况下，进一步加剧性能瓶颈。 因为原SQL仅需要传输10条记录至客户端，而改写之 后的SQL则会传输1,000,010 * 2的记录至客户端。

  * ShardingSphere的优化：

    ShardingSphere进行了以下2个方面的优化

    * 首先，采用流式处理 + 归并排序的方式来避免内存的过量占用。
    * 其次，ShardingSphere对仅落至单节点的查询进行进一步优化。

  * 分页方案优化：

    由于LIMIT并不能通过索引查询数据，因此如果可以保证ID的连续性，通过ID进行分页是比较 好的解决方案：

    ```mysql
    SELECT * FROM b_order WHERE id > 1000000 AND id <= 1000010 ORDER BY id
    ```

    或通过记录上次查询结果的最后一条记录的ID进行下一页的查询：

    ```mysql
    SELECT * FROM b_order WHERE id > 1000000 LIMIT 10
    ```

### 其他功能

**Inline行表达式**

InlineShardingStrategy：采用Inline行表达式进行分片的配置。

Inline是可以简化数据节点和分片算法配置信息。主要是解决配置简化、配置一体化。

**语法格式：**

行表达式的使用非常直观，只需要在配置中使用${ expression }或$->{ expression }标识行表达式 即可。例如：

```mysql
${begin..end} 表示范围区间 
${[unit1, unit2, unit_x]} 表示枚举值
```

行表达式中如果出现多个${}或$->{}表达式，整个表达式结果会将每个子表达式结果进行笛卡尔 (积)组合。例如，以下行表达式：

```mysql
${['online', 'offline']}_table${1..3} 
$->{['online', 'offline']}_table$->{1..3}
```

最终会解析为：

```mysql
online_table1, online_table2, online_table3, 
offline_table1, offline_table2, offline_table3
```

**数据节点配置**：

对于均匀分布的数据节点，如果数据结构如下：

```
db0
├── b_order2 
  └── b_order1
db1
├── b_order2 
  └── b_order1
```

用行表达式可以简化为：

```
db${0..1}.b_order${1..2} 
或者 
db$->{0..1}.b_order$->{1..2}
```

对于自定义的数据节点，如果数据结构如下：

```
db0
├── b_order0 
└── b_order1
db1 
├── b_order2
├── b_order3 
└── b_order4
```

用行表达式可以简化为：

```
db0.b_order${0..1},db1.b_order${2..4}
```

**分片算法配置：**

行表达式内部的表达式本质上是一段Groovy代码，可以根据分片键进行计算的方式，返回相应的 真实数据源或真实表名称。

```
ds${id % 10} 或者 ds$->{id % 10}
```

结果为：ds0、ds1、ds2... ds9

* 分布式主键

  ShardingSphere不仅提供了内置的分布式主键生成器，例如UUID、SNOWFLAKE，还抽离出分布 式主键生成器的接口，方便用户自行实现自定义的自增主键生成器。

  **内置主键生成器：**

  * UUID

    采用UUID.randomUUID()的方式产生分布式主键。

  * SNOWFLAKE

    在分片规则配置模块可配置每个表的主键生成策略，默认使用雪花算法，生成64bit的长整型数据。

  **自定义主键生成器**

  * 自定义主键类，实现ShardingKeyGenerator接口

  * 按SPI规范配置自定义主键类

    在Apache ShardingSphere中，很多功能实现类的加载方式是通过SPI注入的方式完成的。 注意：在resources目录下新建META-INF文件夹，再新建services文件夹，然后新建文件的 名字为org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator，打开文件，复制 自定义主键类全路径到文件中保存。

  * 自定义主键类应用配置

    ```
    #对应主键字段名 spring.shardingsphere.sharding.tables.t_book.key-generator.column=id #对应主键类getType返回内容 spring.shardingsphere.sharding.tables.t_book.keygenerator.type=LAGOUKEY
    ```

## 读写分离剖析实战

读写分离是通过主从的配置方式，将查询请求均匀的分散到多个数据副本，进一步的提升系统的处理能力。

![20201224-104831-0848.png](https://gitee.com/chuchin/img/raw/master/20201224-104831-0848.png)

主从架构：读写分离，目的是高可用、读写扩展。主从库内容相同，根据SQL语义进行路由。

分库分表架构：数据分片，目的读写扩展、存储扩容。库和表内容不同，根据分片配置进行路由。

将水平分片和读写分离联合使用，能够更加有效的提升系统性能， 下图展现了将分库分表与读写分离一 同使用时，应用程序与数据库集群之间的复杂拓扑关系。

![20201224-103833-0818.png](https://gitee.com/chuchin/img/raw/master/20201224-103833-0818.png)

读写分离虽然可以提升系统的吞吐量和可用性，但同时也带来了数据不一致的问题，包括多个主库之间 的数据一致性，以及主库与从库之间的数据一致性的问题。 并且，读写分离也带来了与数据分片同样的问题，它同样会使得应用开发和运维人员对数据库的操作和运维变得更加复杂。

**读写分离应用方案**

在数据量不是很多的情况下，我们可以将数据库进行读写分离，以应对高并发的需求，通过水平扩展从 库，来缓解查询的压力。如下：

![20201224-103438-0703.png](https://gitee.com/chuchin/img/raw/master/20201224-103438-0703.png)

**分表+读写分离**

在数据量达到500万的时候，这时数据量预估千万级别，我们可以将数据进行分表存储。

![20201224-104242-0690.png](https://gitee.com/chuchin/img/raw/master/20201224-104242-0690.png)

**分库分表+读写分离**

在数据量继续扩大，这时可以考虑分库分表，将数据存储在不同数据库的不同表中，如下：

![20201224-100644-0735.png](https://gitee.com/chuchin/img/raw/master/20201224-100644-0735.png)

**透明化读写分离所带来的影响，让使用方尽量像使用一个数据库一样使用主从数据库集群，是 ShardingSphere读写分离模块的主要设计目标。**

主库、从库、主从同步、负载均衡

* 核心功能
  * 提供一主多从的读写分离配置。仅支持单主库，可以支持独立使用，也可以配合分库分表使用
  * 独立使用读写分离，支持SQL透传。不需要SQL改写流程
  * 同一线程且同一数据库连接内，能保证数据一致性。如果有写入操作，后续的读操作均从主库读取。
  * 基于Hint的强制主库路由。可以强制路由走主库查询实时数据，避免主从同步数据延迟。
* 不支持项
  * 主库和从库的数据同步
  * 主库和从库的数据同步延迟
  * 主库双写或多写
  * 跨主库和从库之间的事务的数据不一致。建议在主从架构中，事务中的读写均用主库操作。

## 强制路由剖析实战

在一些应用场景中，分片条件并不存在于SQL，而存在于外部业务逻辑。因此需要提供一种通过在外部 业务代码中指定路由配置的一种方式，在ShardingSphere中叫做Hint。如果使用Hint指定了强制分片路由，那么SQL将会无视原有的分片逻辑，直接路由至指定的数据节点操作。

HintManager主要使用ThreadLocal管理分片键信息，进行hint强制路由。在代码中向HintManager添 加的配置信息只能在当前线程内有效。

**Hint使用场景：**

* 数据分片操作，如果分片键没有在SQL或数据表中，而是在业务逻辑代码中
* 读写分离操作，如果强制在主库进行某些数据操作

**Hint使用过程：**

* 编写分库或分表路由策略，实现HintShardingAlgorithm接口

  ```java
  public class MyHintShardingAlgorithm implements HintShardingAlgorithm<Integer> { @Override 
  public Collection<String> doSharding(Collection<String> collection,
  	HintShardingValue<Integer> hintShardingValue) { 
  	//添加分库或分表路由逻辑
  } }
  ```

* 在配置文件指定分库或分表策略

  ```yaml
  #强制路由库和表 spring.shardingsphere.sharding.tables.b_order.databasestrategy.hint.algorithm-class-name=com.lagou.hint.MyHintShardingAlgorithm spring.shardingsphere.sharding.tables.b_order.table-strategy.hint.algorithmclass-name=com.lagou.hint.MyHintShardingAlgorithm spring.shardingsphere.sharding.tables.b_order.actual-data-nodes=ds$-> {0..1}.b_order$->{0..1}
  ```

* 在代码执行查询前使用HintManager指定执行策略值

  ```mysql
  @Test//路由库和表 
  public void test(){ 
  HintManager hintManager = HintManager.getInstance(); 		    hintManager.addDatabaseShardingValue("b_order",1); 	hintManager.addTableShardingValue("b_order",1); List<Order> list = orderRepository.findAll(); hintManager.close(); list.forEach(o -> { System.out.println(o.getOrderId()+" "+o.getUserId()+""+o.getOrderPrice()); });
  }
  ```

  在读写分离结构中，为了避免主从同步数据延迟及时获取刚添加或更新的数据，可以采用强制路由走主库查询实时数据，使用hintManager.setMasterRouteOnly设置主库路由即可。

## 数据脱敏剖析实战

数据脱敏是指对某些敏感信息通过脱敏规则进行数据的变形，实现敏感隐私数据的可靠保护。涉及客户安全数据或者一些商业性敏感数据，如身份证号、手机号、卡号、客户号等个人信息按照规定，都需要 进行数据脱敏。

数据脱敏模块属于ShardingSphere分布式治理这一核心功能下的子功能模块。

* 在更新操作时，它通过对用户输入的SQL进行解析，并依据用户提供的脱敏配置对SQL进行改写， 从而实现对原文数据进行加密，并将密文数据存储到底层数据库。
* 在查询数据时，它又从数据库中取出密文数据，并对其解密，最终将解密后的原始数据返回给用户。

**Apache ShardingSphere自动化&透明化了数据脱敏过程，让用户无需关注数据脱敏的实现细节，像 使用普通数据那样使用脱敏数据。**

### 整体架构

ShardingSphere提供的Encrypt-JDBC和业务代码部署在一起。业务方需面向Encrypt-JDBC进行JDBC编程。

![20201224-104956-0772.png](https://gitee.com/chuchin/img/raw/master/20201224-104956-0772.png)

Encrypt-JDBC将用户发起的SQL进行拦截，并通过SQL语法解析器进行解析、理解SQL行为，再依据用 户传入的脱敏规则，找出需要脱敏的字段和所使用的加解密器对目标字段进行加解密处理后，再与底层 数据库进行交互。

### 脱敏规则

脱敏配置主要分为四部分：数据源配置，加密器配置，脱敏表配置以及查询属性配置，其详情如下图所示：

![20201224-101458-0845.png](https://gitee.com/chuchin/img/raw/master/20201224-101458-0845.png)

* 数据源配置：指DataSource的配置信息
* 加密器配置：指使用什么加密策略进行加解密。目前ShardingSphere内置了两种加解密策略： AES/MD5
* 脱敏表配置：指定哪个列用于存储密文数据（cipherColumn）、哪个列用于存储明文数据 （plainColumn）以及用户想使用哪个列进行SQL编写（logicColumn）
* 查询属性的配置：当底层数据库表里同时存储了明文数据、密文数据后，该属性开关用于决定是直 接查询数据库表里的明文数据进行返回，还是查询密文数据通过Encrypt-JDBC解密后返回。

### 脱敏处理流程

下图可以看出ShardingSphere将逻辑列与明文列和密文列进行了列名映射。

![20201224-114600-0608.png](https://gitee.com/chuchin/img/raw/master/20201224-114600-0608.png)

下方图片展示了使用Encrypt-JDBC进行增删改查时，其中的处理流程和转换逻辑，如下图所示。

![20201224-112901-0278.png](https://gitee.com/chuchin/img/raw/master/20201224-112901-0278.png)

### 加密策略解析

ShardingSphere提供了两种加密策略用于数据脱敏，该两种策略分别对应ShardingSphere的两种加解密的接口，即Encryptor和QueryAssistedEncryptor。

* Encryptor

  该解决方案通过提供encrypt(), decrypt()两种方法对需要脱敏的数据进行加解密。在用户进行 INSERT, DELETE, UPDATE时，ShardingSphere会按照用户配置，对SQL进行解析、改写、路由， 并会调用encrypt()将数据加密后存储到数据库, 而在SELECT时，则调用decrypt()方法将从数据库 中取出的脱敏数据进行逆向解密，最终将原始数据返回给用户。
  当前，ShardingSphere针对这种类型的脱敏解决方案提供了两种具体实现类，分别是MD5(不可逆)，AES(可逆)，用户只需配置即可使用这两种内置的方案。

* QueryAssistedEncryptor

  相比较于第一种脱敏方案，该方案更为安全和复杂。它的理念是：即使是相同的数据，如两个用户的密码相同，它们在数据库里存储的脱敏数据也应当是不一样的。这种理念更有利于保护用户信 息，防止撞库成功。
  它提供三种函数进行实现，分别是encrypt(), decrypt(), queryAssistedEncrypt()。在encrypt()阶 段，用户通过设置某个变动种子，例如时间戳。针对原始数据+变动种子组合的内容进行加密，就 能保证即使原始数据相同，也因为有变动种子的存在，致使加密后的脱敏数据是不一样的。在 decrypt()可依据之前规定的加密算法，利用种子数据进行解密。queryAssistedEncrypt()用于生成 辅助查询列，用于原始数据的查询过程。 当前，ShardingSphere针对这种类型的脱敏解决方案并没有提供具体实现类，却将该理念抽象成接口，提供给用户自行实现。ShardingSphere将调用用户提供的该方案的具体实现类进行数据脱敏。

## 分布式事务剖析实战

### 分布式事务理论

* CAP（强一致性）

  CAP 定理，又被叫作布鲁尔定理。对于共享数据系统，最多只能同时拥有CAP其中的两个，任意两个都有其适应的场景。

  ![20201224-114904-0483.png](https://gitee.com/chuchin/img/raw/master/20201224-114904-0483.png)

* BASE（最终一致性）

  BASE 是指基本可用（Basically Available）、软状态（ Soft State）、最终一致性（ Eventual Consistency）。它的核心思想是即使无法做到强一致性（CAP 就是强一致性），但应用可以采用 适合的方式达到最终一致性。

  * BA指的是基本业务可用性，支持分区失败；
  * S表示柔性状态，也就是允许短时间内不同步；
  * E表示最终一致性，数据最终是一致的，但是实时是不一致的。

  原子性和持久性必须从根本上保障，为了可用性、性能和服务降级的需要，只有降低一致性和隔离性的要求。BASE 解决了 CAP 理论中没有考虑到的网络延迟问题，在BASE中用软状态和最终一 致，保证了延迟后的一致性。

### 分布式事务模式

了解了分布式事务中的强一致性和最终一致性理论，下面介绍几种常见的分布式事务的解决方案。

#### 2PC模式（强一致性）

2PC是Two-Phase Commit缩写，即两阶段提交，就是将事务的提交过程分为两个阶段来进行处理。事务的发起者称协调者，事务的执行者称参与者。协调者统一协调参与者执行。

* 阶段 1：准备阶段

  协调者向所有参与者发送事务内容，询问是否可以提交事务，并等待所有参与者答复。 各参与者执行事务操作，但不提交事务，将 undo 和 redo 信息记入事务日志中。 如参与者执行成功，给协调者反馈 yes；如执行失败，给协调者反馈 no。

* 阶段 2：提交阶段

  如果协调者收到了参与者的失败消息或者超时，直接给每个参与者发送回滚(rollback)消息； 否则，发送提交(commit)消息。

2PC 方案实现起来简单，实际项目中使用比较少，主要因为以下问题：

* 性能问题：所有参与者在事务提交阶段处于同步阻塞状态，占用系统资源，容易导致性能瓶颈。
* 可靠性问题：如果协调者存在单点故障问题，如果协调者出现故障，参与者将一直处于锁定状态。
* 数据一致性问题：在阶段 2 中，如果发生局部网络问题，一部分事务参与者收到了提交消息，另一部分事务参与者没收到提交消息，那么就导致了节点之间数据的不一致。

#### 3PC模式（强一致性）

3PC 三阶段提交，是两阶段提交的改进版本，与两阶段提交不同的是，引入超时机制。同时在协 调者和参与者中都引入超时机制。三阶段提交将两阶段的准备阶段拆分为 2 个阶段，插入了一个 preCommit 阶段，解决了原先在两阶段提交中，参与者在准备之后，由于协调者或参与者发生崩 溃或错误，而导致参与者无法知晓处于长时间等待的问题。如果在指定的时间内协调者没有收到参 与者的消息则默认失败。

* 阶段1：canCommit

  协调者向参与者发送 commit 请求，参与者如果可以提交就返回 yes 响应，否则返回 no 响应。

* 阶段2：preCommit

  协调者根据阶段 1 canCommit 参与者的反应情况执行预提交事务或中断事务操作。

  * 参与者均反馈 yes：协调者向所有参与者发出 preCommit 请求，参与者收到 preCommit 请求后，执行事务操作，但不提交；将 undo 和 redo 信息记入事务日志 中；各参与者向协调者反馈 ack 响应或 no 响应，并等待最终指令。
  * 任何一个参与者反馈 no或等待超时：协调者向所有参与者发出 abort 请求，无论收到 协调者发出的 abort 请求，或者在等待协调者请求过程中出现超时，参与者均会中断事务。

* 阶段3：do Commit

  该阶段进行真正的事务提交，根据阶段 2 preCommit反馈的结果完成事务提交或中断操作。

#### XA（强一致性）

XA是由X/Open组织提出的分布式事务的规范，是基于两阶段提交协议。 XA规范主要定义了全局事务管理器（TM）和局部资源管理器（RM）之间的接口。目前主流的关系型数据库产品都是实现 了XA接口。

![20201224-112415-0983.png](https://gitee.com/chuchin/img/raw/master/20201224-112415-0983.png)

XA之所以需要引入事务管理器，是因为在分布式系统中，从理论上讲两台机器理论上无法达到一致的状态，需要引入一个单点进行协调。由全局事务管理器管理和协调的事务，可以跨越多个资源 （数据库）和进程。

事务管理器用来保证所有的事务参与者都完成了准备工作(第一阶段)。如果事务管理器收到所有参与者都准备好的消息，就会通知所有的事务都可以提交了（第二阶段）。MySQL 在这个XA事务中 扮演的是参与者的角色，而不是事务管理器。

#### TCC模式（最终一致性）

TCC（Try-Confirm-Cancel）的概念，最早是由 Pat Helland 于 2007 年发表的一篇名为《Life beyond Distributed Transactions:an Apostate’s Opinion》的论文提出。TCC 是服务化的两阶段 编程模型，其 Try、Confirm、Cancel 3 个方法均由业务编码实现：

* Try 操作作为一阶段，负责资源的检查和预留；
* Confirm 操作作为二阶段提交操作，执行真正的业务；
* Cancel 是预留资源的取消；

TCC事务模式相对于 XA 等传统模型如下图所示：

![20201224-111118-0407.png](https://gitee.com/chuchin/img/raw/master/20201224-111118-0407.png)

TCC 模式相比于 XA，解决了如下几个缺点：

* 解决了协调者单点，由主业务方发起并完成这个业务活动。业务活动管理器可以变成多点， 引入集群。
* 同步阻塞：引入超时机制，超时后进行补偿，并且不会锁定整个资源，将资源转换为业务逻 辑形式，粒度变小。
* 数据一致性，有了补偿机制之后，由业务活动管理器控制一致性。

#### 消息队列模式（最终一致性）

消息队列的方案最初是由 eBay 提出，基于TCC模式，消息中间件可以基于 Kafka、RocketMQ 等消息队列。此方案的核心是将分布式事务拆分成本地事务进行处理，将需要分布式处理的任务通过 消息日志的方式来异步执行。消息日志可以存储到本地文本、数据库或MQ中间件，再通过业务规 则人工发起重试。

下面描述下事务的处理流程：

![20201224-114919-0414.png](https://gitee.com/chuchin/img/raw/master/20201224-114919-0414.png)

* 步骤1：事务主动方处理本地事务。

  事务主动方在本地事务中处理业务更新操作和MQ写消息操作。

* 步骤 2：事务主动方通过消息中间件，通知事务被动方处理事务通知事务待消息。

  事务主动方主动写消息到MQ，事务消费方接收并处理MQ中的消息。

* 步骤 3：事务被动方通过MQ中间件，通知事务主动方事务已处理的消息，事务主动方根据反馈结果提交或回滚事务。

为了数据的一致性，当流程中遇到错误需要重试，容错处理规则如下：

* 当步骤 1 处理出错，事务回滚，相当于什么都没发生。
* 当步骤 2 处理出错，由于未处理的事务消息还是保存在事务发送方，可以重试或撤销本地业务操作。
* 如果事务被动方消费消息异常，需要不断重试，业务处理逻辑需要保证幂等。
* 如果是事务被动方业务上的处理失败，可以通过MQ通知事务主动方进行补偿或者事务回滚。
* 如果多个事务被动方已经消费消息，事务主动方需要回滚事务时需要通知事务被动方回滚。

#### Saga模式（最终一致性）

Saga这个概念源于 1987 年普林斯顿大学的 Hecto 和 Kenneth 发表的一篇数据库论文Sagas ，一 个Saga事务是一个有多个短时事务组成的长时的事务。 在分布式事务场景下，我们把一个Saga分 布式事务看做是一个由多个本地事务组成的事务，每个本地事务都有一个与之对应的补偿事务。在 Saga事务的执行过程中，如果某一步执行出现异常，Saga事务会被终止，同时会调用对应的补偿事务完成相关的恢复操作，这样保证Saga相关的本地事务要么都是执行成功，要么通过补偿恢复成为事务执行之前的状态。（自动反向补偿机制）。

Saga 事务基本协议如下：

* 每个 Saga 事务由一系列幂等的有序子事务(sub-transaction) Ti 组成。
* 每个 Ti 都有对应的幂等补偿动作 Ci，补偿动作用于撤销 Ti 造成的结果。
* Saga是一种补偿模式，它定义了两种补偿策略：
  * 向前恢复（forward recovery）：对应于上面第一种执行顺序，发生失败进行重试，适用于 必须要成功的场景。
  * 向后恢复（backward recovery）：对应于上面提到的第二种执行顺序，发生错误后撤销掉之前所 有成功的子事务，使得整个 Saga 的执行结果撤销。

![20201224-111423-0250.png](https://gitee.com/chuchin/img/raw/master/20201224-111423-0250.png)

Saga 的执行顺序有两种，如上图：

* 事务正常执行完成：T1, T2, T3, ..., Tn，例如：减库存(T1)，创建订单(T2)，支付(T3)，依次有序完 成整个事务。

* 事务回滚：T1, T2, ..., Tj, Cj,..., C2, C1，其中 0 < j < n，例如：减库存(T1)，创建订单(T2)，支付 (T3)，支付失败，支付回滚(C3)，订单回滚(C2)，恢复库存(C1)。

#### Seata框架

Fescar开源项目，最初愿景是能像本地事务一样控制分布式事务，解决分布式环境下的难题。

Seata（Simple Extensible Autonomous Transaction Architecture）是一套一站式分布式事务解 决方案，是阿里集团和蚂蚁金服联合打造的分布式事务框架。Seata目前的事务模式有AT、TCC、 Saga和XA，默认是AT模式，AT本质上是2PC协议的一种实现。

Seata AT事务模型包含TM(事务管理器)，RM(资源管理器)，TC(事务协调器)。其中TC是一个独立 的服务需要单独部署，TM和RM以jar包的方式同业务应用部署在一起，它们同TC建立长连接，在 整个事务生命周期内，保持RPC通信。

* 全局事务的发起方作为TM，全局事务的参与者作为RM
* TM负责全局事务的begin和commit/rollback
* RM负责分支事务的执行结果上报，并且通过TC的协调进行commit/rollback。

![20201224-112925-0885.png](https://gitee.com/chuchin/img/raw/master/20201224-112925-0885.png)

在 Seata 中，AT时分为两个阶段的，第一阶段，就是各个阶段本地提交操作；第二阶段会根据第 一阶段的情况决定是进行全局提交还是全局回滚操作。具体的执行流程如下：

* TM 开启分布式事务，负责全局事务的begin和commit/rollback（TM 向 TC 注册全局事务记 录）；
* RM 作为参与者，负责分支事务的执行结果上报，并且通过TC的协调进行 commit/rollback（RM 向 TC 汇报资源准备状态 ）；
* 根据TC 汇总事务信息，由TM发起事务提交或回滚操作；
* TC 通知所有 RM 提交/回滚资源，事务二阶段结束；

### Sharding-JDBC整合XA原理

Java通过定义JTA接口实现了XA的模型，JTA接口里的ResourceManager需要数据库厂商提供XA的驱动实现，而TransactionManager则需要事务管理器的厂商实现，传统的事务管理器需要同应用服务器绑 定，因此使用的成本很高。 而嵌入式的事务管器可以以jar包的形式提供服务，同ShardingSphere集成 后，可保证分片后跨库事务强一致性。

ShardingSphere支持以下功能：

* 支持数据分片后的跨库XA事务
* 两阶段提交保证操作的原子性和数据的强一致性
* 服务宕机重启后，提交/回滚中的事务可自动恢复
* SPI机制整合主流的XA事务管理器，默认Atomikos
* 同时支持XA和非XA的连接池
* 提供spring-boot和namespace的接入端

ShardingSphere整合XA事务时，分离了XA事务管理和连接池管理，这样接入XA时，可以做到对业务的零侵入。

![20201224-110928-0259.png](https://gitee.com/chuchin/img/raw/master/20201224-110928-0259.png)

1. Begin（开启XA全局事务）

   XAShardingTransactionManager会调用具体的XA事务管理器开启XA的全局事务。

2. 执行物理SQL

   ShardingSphere进行解析/优化/路由后会生成SQL操作，执行引擎为每个物理SQL创建连接的同 时，物理连接所对应的XAResource也会被注册到当前XA事务中。事务管理器会在此阶段发送 XAResource.start命令给数据库，数据库在收到XAResource.end命令之前的所有SQL操作，会被 标记为XA事务。

   例如:

   ```
   XAResource1.start ## Enlist阶段执行
   statement.execute("sql1"); ## 模拟执行一个分片SQL1
   statement.execute("sql2"); ## 模拟执行一个分片SQL2
   XAResource1.end  ## 提交阶段执行
   ```

   这里sql1和sql2将会被标记为XA事务。

3. Commit/rollback（提交XA事务）

   XAShardingTransactionManager收到接入端的提交命令后，会委托实际的XA事务管理进行提交 动作，这时事务管理器会收集当前线程里所有注册的XAResource，首先发送XAResource.end指 令，用以标记此XA事务的边界。 接着会依次发送prepare指令，收集所有参与XAResource投票， 如果所有XAResource的反馈结果都是OK，则会再次调用commit指令进行最终提交，如果有一个 XAResource的反馈结果为No，则会调用rollback指令进行回滚。 在事务管理器发出提交指令后， 任何XAResource产生的异常都会通过recovery日志进行重试，来保证提交阶段的操作原子性，和 数据强一致性。 例如:

   ```
   XAResource1.prepare ## ack: yes 
   XAResource2.prepare ## ack: yes
   XAResource1.commit 
   XAResource2.commit
   
   XAResource1.prepare ## ack: yes
   XAResource2.prepare ## ack: no
   XAResource1.rollback
   XAResource2.rollback
   ```


### Sharding-JDBC整合Saga原理

ShardingSphere的柔性事务已通过第三方servicecomb-saga组件实现的，通过SPI机制注入使用。 ShardingSphere是基于反向SQL技术实现的反向补偿操作，它将对数据库进行更新操作的SQL自动生成 反向SQL，并交由Saga-actuator引擎执行。使用方则无需再关注如何实现补偿方法，将柔性事务管理器 的应用范畴成功的定位回了事务的本源——数据库层面。ShardingSphere支持以下功能：

* 完全支持跨库事务
* 支持失败SQL重试及最大努力送达
* 支持反向SQL、自动生成更新快照以及自动补偿
* 默认使用关系型数据库进行快照及事务日志的持久化，支持使用SPI的方式加载其他类型的持久化

Saga柔性事务的实现类为SagaShardingTransactionMananger, ShardingSphere通过Hook的方式拦 截逻辑SQL的解析和路由结果，这样，在分片物理SQL执行前，可以生成逆向SQL，在事务提交阶段再 把SQL调用链交给Saga引擎处理。

![20201224-132939-0389.png](https://gitee.com/chuchin/img/raw/master/20201224-132939-0389.png)

1. Init（Saga引擎初始化）

   包含Saga柔性事务的应用启动时，saga-actuator引擎会根据saga.properties的配置进行初始化的流程。

2. Begin（开启Saga全局事务）

   每次开启Saga全局事务时，将会生成本次全局事务的上下文（SagaTransactionContext），事务 上下文记录了所有子事务的正向SQL和逆向SQL，作为生成事务调用链的元数据使用。

3. 执行物理SQL

   在物理SQL执行前，ShardingSphere根据SQL的类型生成逆向SQL，这里是通过Hook的方式拦截 Parser的解析结果进行实现。

4. Commit/rollback（提交Saga事务）

   提交阶段会生成Saga执行引擎所需的调用链路图，commit操作产生ForwardRecovery（正向SQL 补偿）任务，rollback操作产生BackwardRecovery任务（逆向SQL补偿）。

### Sharding-JDBC整合Seata原理

分布式事务的实现目前主要分为两阶段的XA强事务和BASE柔性事务。

![20201224-131841-0805.png](https://gitee.com/chuchin/img/raw/master/20201224-131841-0805.png)

Seata AT事务作为BASE柔性事务的一种实现，可以无缝接入到ShardingSphere生态中。在整合Seata AT事务时，需要把TM，RM，TC的模型融入到ShardingSphere 分布式事务的SPI的生态中。在数据库 资源上，Seata通过对接DataSource接口，让JDBC操作可以同TC进行RPC通信。同样， ShardingSphere也是面向DataSource接口对用户配置的物理DataSource进行了聚合，因此把物理 DataSource二次包装为Seata 的DataSource后，就可以把Seata AT事务融入到ShardingSphere的分片中。

![20201224-132939-0389.png](https://gitee.com/chuchin/img/raw/master/20201224-132939-0389.png)

1. Init（Seata引擎初始化）

   包含Seata柔性事务的应用启动时，用户配置的数据源会按seata.conf的配置，适配成Seata事务所 需的DataSourceProxy，并且注册到RM中。

2. Begin（开启Seata全局事务）

   TM控制全局事务的边界，TM通过向TC发送Begin指令，获取全局事务ID，所有分支事务通过此全 局事务ID，参与到全局事务中；全局事务ID的上下文存放在当前线程变量中。

3. 执行分片物理SQL

   处于Seata全局事务中的分片SQL通过RM生成undo快照，并且发送participate指令到TC，加入到 全局事务中。ShardingSphere的分片物理SQL是按多线程方式执行，因此整合Seata AT事务时， 需要在主线程和子线程间进行全局事务ID的上下文传递，这同服务间的上下文传递思路完全相 同。

4. Commit/rollback（提交Seata事务）

   提交Seata事务时，TM会向TC发送全局事务的commit和rollback指令，TC根据全局事务ID协调所有分支事务进行commit和rollback。

## Sharding-JDBC分布式事务实战

ShardingSphere整合了XA、Saga和Seata模式后，为分布式事务控制提供了极大的便利，我们可以在应用程序编程时，采用以下统一模式进行使用。

* 引入Maven依赖

  ```xml
  <dependency>
              <groupId>org.apache.shardingsphere</groupId>
              <artifactId>sharding-transaction-xa-core</artifactId>
              <version>${shardingsphere.version}</version>
          </dependency>
  
          <dependency>
              <groupId>io.shardingsphere</groupId>
              <artifactId>sharding-transaction-base-saga</artifactId>
              <version>${shardingsphere.version}</version>
          </dependency>
  
          <dependency>
              <groupId>org.apache.shardingsphere</groupId>
              <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
  </dependency>
  ```

* JAVA编码方式设置事务类型

  ```java
  TransactionTypeHolder.set(TransactionType.XA); TransactionTypeHolder.set(TransactionType.BASE);
  ```

* 参数配置

  ShardingSphere默认的XA事务管理器为Atomikos，通过在项目的classpath中添加jta.properties 来定制化Atomikos配置项。
  
  Saga可以通过在项目的classpath中添加saga.properties来定制化Saga事务的配置项。

## SPI 加载剖析

在Apache ShardingSphere中，很多功能实现类的加载方式是通过SPI注入的方式完成的。 Service Provider Interface （SPI）是Java提供的一套被第三方实现或扩展的API，它可以用于实现框架扩展或 组件替换。

本节汇总了Apache ShardingSphere所有通过SPI方式载入的功能模块。

* SQL解析 

  SQL解析的接口用于规定用于解析SQL的ANTLR语法文件。 

  主要接口是SQLParserEntry，其内置实现类有MySQLParserEntry, PostgreSQLParserEntry, SQLServerParserEntry和OracleParserEntry。 

* 数据库协议

  数据库协议的接口用于Sharding-Proxy解析与适配访问数据库的协议。

  主要接口是DatabaseProtocolFrontendEngine，其内置实现类有

  MySQLProtocolFrontendEngine和PostgreSQLProtocolFrontendEngine。

* 数据脱敏

  数据脱敏的接口用于规定加解密器的加密、解密、类型获取、属性设置等方式。

  主要接口有两个：Encryptor和QueryAssistedEncryptor，其中Encryptor的内置实现类有 AESEncryptor和MD5Encryptor。

* 分布式主键

  分布式主键的接口主要用于规定如何生成全局性的自增、类型获取、属性设置等。

  主要接口为ShardingKeyGenerator，其内置实现类有UUIDShardingKeyGenerator和 SnowflakeShardingKeyGenerator。

* 分布式事务

  分布式事务的接口主要用于规定如何将分布式事务适配为本地事务接口。

  主要接口为ShardingTransactionManager，其内置实现类有XAShardingTransactionManager和 SeataATShardingTransactionManager。

* XA事务管理器

  XA事务管理器的接口主要用于规定如何将XA事务的实现者适配为统一的XA事务接口。 主要接口为XATransactionManager，其内置实现类有AtomikosTransactionManager, NarayanaXATransactionManager和BitronixXATransactionManager。

* 注册中心

  注册中心的接口主要用于规定注册中心初始化、存取数据、更新数据、监控等行为。 

  主要接口为RegistryCenter，其内置实现类有Zookeeper。

## 编排治理剖析

编排治理模块提供配置中心/注册中心（以及规划中的元数据中心）、配置动态化、数据库熔断禁用、 调用链路等治理能力。

* 配置中心

  配置集中化：越来越多的运行时实例，使得散落的配置难于管理，配置不同步导致的问题十分严 重。将配置集中于配置中心，可以更加有效进行管理。
  配置动态化：配置修改后的分发，是配置中心可以提供的另一个重要能力。它可支持数据源、表与 分片及读写分离策略的动态切换。

  * 配置中心数据结构

    配置中心在定义的命名空间的config下，以YAML格式存储，包括数据源，数据分片，读写分 离、Properties配置，可通过修改节点来实现对于配置的动态管理。

    ![20201224-145735-0197.png](https://gitee.com/chuchin/img/raw/master/20201224-145735-0197.png)

  * config/authentication

    ```
    password: root 
    username: root
    ```

  * config/sharding/props

    ```
    sql.show: true
    ```

  * config/schema/schemeName/datasource

    多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）。

    ```
    ds_0: 
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource 
    properties: 
    	url: jdbc:mysql://127.0.0.1:3306/lagou1?
    	serverTimezone=UTC&useSSL=false 
    	password: root 
    	username: root 
    	maxPoolSize: 50 minPoolSize: 1
    ds_1: 
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource 
    properties: 
    	url: jdbc:mysql://127.0.0.1:3306/lagou2?
    	serverTimezone=UTC&useSSL=false 
    	password: root 
    	username: root 
    	maxPoolSize: 50 
    	minPoolSize: 1
    ```

  * config/schema/sharding_db/rule

    数据分片配置，包括数据分片配置。

    ```
    tables: 
    	b_order: 
    		actualDataNodes: ds_$->{0..1}.b_order_$->{0..1} 
    		databaseStrategy:
    		inline: 
    		shardingColumn: user_id 
    		algorithmExpression: ds_$->{user_id % 2}
    	keyGenerator: 
    		column: order_id
    	logicTable: b_order 
    		tableStrategy: 
    			inline: 
    				shardingColumn: order_id 
    				algorithmExpression: b_order_$->{order_id % 2}
    	b_order_item: 
    		actualDataNodes: ds_$->{0..1}.b_order_item_$->{0..1} 
    		databaseStrategy: 
    			inline: 
    				shardingColumn: user_id 
    				algorithmExpression: ds_$->{user_id % 2}
    		keyGenerator: 
    			column: order_item_id
    		logicTable: b_order_item 
    		tableStrategy: 
    			inline: 
    				shardingColumn: order_id 
    				algorithmExpression: b_order_item_$->{order_id % 2}
    ```

  * config/schema/masterslave/rule读写分离独立使用时使用该配置。

    ```
    name: ds_ms 
    masterDataSourceName: master 
    slaveDataSourceNames: 
    	- ds_slave0 
    	- ds_slave1
    loadBalanceAlgorithmType: ROUND_ROBIN
    ```
    
  * 动态生效
  
    在注册中心上修改、删除、新增相关配置，会动态推送到生产环境并立即生效。
  
* 注册中心

  相对于配置中心管理配置数据，注册中心存放运行时的动态/临时状态数据，比如可用的proxy的实例，需要禁用或熔断的datasource实例。通过注册中心，可以提供熔断数据库访问程序对数据库 的访问和禁用从库的访问的编排治理能力。治理仍然有大量未完成的功能（比如流控等）

  * 注册中心数据结构

    注册中心在定义的命名空间的state下，创建数据库访问对象运行节点，用于区分不同数据库 访问实例。包括instances和datasources节点。

    ```
    instances 
    	├──your_instance_ip_a@-@your_instance_pid_x 
    	├──your_instance_ip_b@-@your_instance_pid_y 
    	├──....
    datasources 
    	├──ds0 
    	├──ds1 
    	├──....
    ```

  * state/instances

    数据库访问对象运行实例信息，子节点是当前运行实例的标识。 运行实例标识由运行服务器 的IP地址和PID构成。运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。 注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

  * state/datasources

    可以控制读写分离，可动态添加删除以及禁用。

  * 熔断实例

    可在IP地址@-@PID节点写入DISABLED（忽略大小写）表示禁用该实例，删除DISABLED表示启用。

    Zookeeper命令如下：

    ```
    [zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@@your_instance_pid_x DISABLED
    ```

  * 禁用从库

    在读写分离场景下，可在数据源名称子节点中写入DISABLED表示禁用从库数据源，删除 DISABLED或节点表示启用。

    Zookeeper命令如下：

    ```
    [zk: localhost:2181(CONNECTED) 0] set
    /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_nam e DISABLED
    ```

  * 支持的配置中心和注册中心
  
    ShardingSphere在数据库治理模块使用SPI方式载入数据到配置中心/注册中心，进行实例熔断和 数据库禁用。 目前，ShardingSphere内部支持Zookeeper和Etcd这种常用的配置中心/注册中心。 此外，您可以使用其他第三方配置中心/注册中心，例如Apollo、Nacos等，并通过SPI的方式 注入到ShardingSphere，从而使用该配置中心/注册中心，实现数据库治理功能。
  
  * 应用性能监控
  
    APM是应用性能监控的缩写。目前APM的主要功能着眼于分布式系统的性能诊断，其主要功能包 括调用链展示，应用拓扑分析等。
  
    ShardingSphere并不负责如何采集、存储以及展示应用性能监控的相关数据，而是将SQL解析与 SQL执行这两块数据分片的最核心的相关信息发送至应用性能监控系统，并交由其处理。 换句话 说，ShardingSphere仅负责产生具有价值的数据，并通过标准协议递交至相关系统。 ShardingSphere可以通过两种方式对接应用性能监控系统。
  
    * 使用OpenTracing API发送性能追踪数据。面向OpenTracing协议的APM产品都可以和 ShardingSphere自动对接，比如SkyWalking，Zipkin和Jaeger。
    * 使用SkyWalking的自动探针。 ShardingSphere团队与SkyWalking团队共同合作，在 SkyWalking中实现了ShardingSphere自动探针，可以将相关的应用性能数据自动发送到 SkyWalking中。

## Sharding-Proxy实战

Sharding-Proxy是ShardingSphere的第二个产品，定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。 目前先提供MySQL版本，它可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQL Workbench等操作数据，对DBA更 加友好。

* 向应用程序完全透明，可直接当做MySQL使用
* 适用于任何兼容MySQL协议的客户端

![20201224-141308-0645.png](https://gitee.com/chuchin/img/raw/master/20201224-141308-0645.png)

Sharding-Proxy的优势在于对异构语言的支持，以及为DBA提供可操作入口。

**Sharding-Proxy使用过程：**

* 下载Sharding-Proxy的最新发行版；

* 解压缩后修改conf/server.yaml和以config-前缀开头的文件，进行分片规则、读写分离规则配置

  编辑%SHARDING_PROXY_HOME%\conf\config-xxx.yaml 

  编辑%SHARDING_PROXY_HOME%\conf\server.yaml

* 引入依赖jar

  如果后端连接MySQL数据库，需要下载MySQL驱动， 解压缩后将mysql-connector-java5.1.48.jar拷贝到${sharding-proxy}\lib目录。 

  如果后端连接PostgreSQL数据库，不需要引入额外依赖。

* Linux操作系统请运行bin/start.sh，Windows操作系统请运行bin/start.bat启动Sharding-Proxy。

  使用默认配置启动：${sharding-proxy}\bin\start.sh

  配置端口启动：${sharding-proxy}\bin\start.sh ${port}

* 使用客户端工具连接。如: mysql -h 127.0.0.1 -P 3307 -u root -p root

若想使用Sharding-Proxy的数据库治理功能，则需要使用注册中心实现实例熔断和从库禁用功能。 Sharding-Proxy默认提供了Zookeeper的注册中心解决方案。只需按照配置规则进行注册中心的配置， 即可使用。

**注意事项**

* Sharding-Proxy 默认不支持hint，如需支持，请在conf/server.yaml中，将props的属性 proxy.hint.enabled设置为true。在Sharding-Proxy中，HintShardingAlgorithm的泛型只能是 String类型。 

* Sharding-Proxy默认使用3307端口，可以通过启动脚本追加参数作为启动端口号。如: bin/start.sh 3308 
* Sharding-Proxy使用conf/server.yaml配置注册中心、认证信息以及公用属性。 
* Sharding-Proxy支持多逻辑数据源，每个以"config-"做前缀命名yaml配置文件，即为一个逻辑数据源。