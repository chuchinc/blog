---
title: "MySQL 索引 查询优化"
date: 2020-09-08T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

## 慢查询定位

* **开启慢查询日志**

  查看 MySQL 数据库是否开启了慢查询日志和慢查询日志文件的存储位置的命令如下：

  ```sql
  SHOW VARIABLES LIKE 'slow_query_log%'
  ```

  通过如下命令开启慢查询日志：

  ```sql
  SET global slow_query_log = ON;
  SET global slow_query_log_file = 'OAK-slow.log';
  SET global log_queries_not_using_indexes = ON;
  SET long_query_time = 10;
  ```

  * long_query_time：指定慢查询的阀值，单位秒。如果SQL执行时间超过阀值，就属于慢查询 记录到日志文件中。
  * log_queries_not_using_indexes：表示会记录没有使用索引的查询SQL。前提是slow_query_log 的值为ON，否则不会奏效。

* **查看慢查询日志**

  * 文本方式查看

    直接使用文本编辑器打开slow.log日志即可。

    ![20201210-162948-0922.png](https://gitee.com/chuchin/img/raw/master/20201210-162948-0922.png)

  * 使用mysqldumpslow查看

    MySQL 提供了一个慢查询日志分析工具mysqldumpslow，可以通过该工具分析慢查询日志 内容。

    在 MySQL bin目录下执行下面命令可以查看该使用格式。

    ```shell
    perl mysqldumpslow.pl --help
    ```

    运行如下命令查看慢查询日志信息：

    ```shell
    perl mysqldumpslow.pl -t 5 -s at C:\ProgramData\MySQL\Data\OAK-slow.log
    ```

    除了使用mysqldumpslow工具，也可以使用第三方分析工具，比如pt-query-digest、 mysqlsla等。

## 慢查询优化

* **索引和慢查询**

  * 如何判断是否为慢查询？

    MySQL判断一条语句是否为慢查询语句，主要依据SQL语句的执行时间，它把当前语句的执行时间跟 long_query_time 参数做比较，如果语句的执行时间 > long_query_time，就会把这条执行语句记录到慢查询日志里面。long_query_time 参数的默认值是 10s，该参数值可以根据自己的业务需要进行调整。

  * 如何判断是否应用了索引？

    SQL语句是否使用了索引，可根据SQL语句执行过程中有没有用到表的索引，可通过 explain 命令分析查看，检查结果中的 key 值，是否为NULL。

  * 应用了索引是否一定快？

    下面我们来看看下面语句的 explain 的结果，你觉得这条语句有用上索引吗？比如

    ```sql
    select * from user where id>0;
    ```

    虽然使用了索引，但是还是从主键索引的最左边的叶节点开始向右扫描整个索引树，进行了全表扫描，此时索引就失去了意义。

    而像 select * from user where id = 2; 这样的语句，才是我们平时说的使用了索引。它表示的意思是，我们使用了索引的快速搜索功能，并且有效地减少了扫描行数。

  查询是否使用索引，只是表示一个SQL语句的执行过程；而是否为慢查询，是由它执行的时间决定 的，也就是说是否使用了索引和是否是慢查询两者之间没有必然的联系。

  我们在使用索引时，不要只关注是否起作用，应该关心索引是否减少了查询扫描的数据行数，如果 扫描行数减少了，效率才会得到提升。对于一个大表，不止要创建索引，还要考虑索引过滤性，过 滤性好，执行速度才会快。

* **提高索引过滤性**

  假如有一个5000万记录的用户表，通过sex='男'索引过滤后，还需要定位3000万，SQL执行速度也不会很快。其实这个问题涉及到索引的过滤性，比如1万条记录利用索引过滤后定位10条、100 条、1000条，那他们过滤性是不同的。索引过滤性与索引字段、表的数据量、表设计结构都有关系。

  下面我们看一个案例：

  ```
  表：student 
  字段：id,name,sex,age 
  造数据：insert into student (name,sex,age) select name,sex,age from student; 
  SQL案例：select * from student where age=18 and name like '张%';（全表扫描）
  ```

  * 优化1

    ```sql
    alter table student add index(name); //追加name索引
    ```

  * 优化2

    ```sql
    alter table student add index(age,name); //追加age,name索引
    ```
  
  * 优化3
  
    ```sql
    可以看到，index condition pushdown 优化的效果还是很不错的。再进一步优化，我们可以把名
    字的第一个字和年龄做一个联合索引，这里可以使用 MySQL 5.7 引入的虚拟列来实现。
    ```
  
    ```sql
    //为user表添加first_name虚拟列，以及联合索引(first_name,age)
    alter table student add first_name varchar(2) generated always as
    (left(name, 1)), add index(first_name, age);
    
    explain select * from student where first_name='张' and age=18;
    ```
  
  * 慢查询原因总结
  
    * 全表扫描：expain分析type属性all
    * 全索引扫描：explain分析type属性index
    * 索引过滤性不好：靠索引字段选型、数据量和状态、表设计
    * 频繁的回表查询开销：尽量少用select *，使用覆盖索引
  
## 分页查询优化

* 一般性分页

  一般的分页查询使用简单的 limit 子句就可以实现。limit格式如下：

  ```sql
  SELECT * FROM 表名 LIMIT [offset,] rows
  ```

  * 第一个参数指定第一个返回记录行的偏移量，注意从0开始；
  * 第二个参数指定返回记录行的最大数目；
  * 如果只给定一个参数，它表示返回最大的记录行数目；

  **思考1：如果偏移量固定，返回记录量对执行时间有什么影响？**

  ```sql
  select * from user limit 10000,1;
  select * from user limit 10000,10;
  select * from user limit 10000,100;
  select * from user limit 10000,1000;
  select * from user limit 10000,10000;
  ```

  结果：在查询记录时，返回记录量低于100条，查询时间基本没有变化，差距不大。随着查询记录量越大，所花费的时间也会越来越多。

  **思考2：如果查询偏移量变化，返回记录数固定对执行时间有什么影响？**

  ```sql
  select * from user limit 1,100;
  select * from user limit 10,100;
  select * from user limit 100,100;
  select * from user limit 1000,100;
  select * from user limit 10000,100;
  ```

  结果：在查询记录时，如果查询记录量相同，偏移量超过100后就开始随着偏移量增大，查询时间急剧的增加。（这种分页查询机制，每次都会从数据库第一条记录开始扫描，越往后查询越慢，而且查询的数据越多，也会拖慢总查询速度。）

* 分页优化方案

  1. **利用覆盖索引优化**

     ```sql
     select * from user limit 10000,100;
     select id from user limit 10000,100;
     ```

  2. **利用子查询优化**

     ```sql
     select * from user limit 10000,100;
     select * from user where id>= (select id from user limit 10000,1) limit 100;
     ```

     原因：使用了id做主键比较(id>=)，并且子查询使用了覆盖索引进行优化