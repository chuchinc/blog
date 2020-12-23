---
title: "MySQL 索引 分析优化"
date: 2020-09-07T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

## EXPLAIN

MySQL 提供了一个 EXPLAIN 命令，它可以对 SELECT 语句进行分析，并输出 SELECT 执行的详细信 息，供开发人员有针对性的优化。例如：

```SQL
EXPLAIN SELECT * from user WHERE id < 3;
```

EXPLAIN 命令的输出内容大致如下：

![20201210-150815-0761.png](https://gitee.com/chuchin/img/raw/master/20201210-150815-0761.png)

参数说明：

* select_type

  表示查询的类型。常用的值如下：

  * SIMPLE ： 表示查询语句不包含子查询或union
  * PRIMARY：表示此查询是最外层的查询
  * UNION：表示此查询是UNION的第二个或后续的查询
  * DEPENDENT UNION：UNION中的第二个或后续的查询语句，使用了外面查询结果
  * UNION RESULT：UNION的结果
  * SUBQUERY：SELECT子查询语句
  * DEPENDENT SUBQUERY：SELECT子查询语句依赖外层查询的结果。

  最常见的查询类型是SIMPLE，表示我们的查询没有子查询也没用到UNION查询。

* type

  表示存储引擎查询数据时采用的方式。比较重要的一个属性，通过它可以判断出查询是全表扫描还 是基于索引的部分扫描。常用属性值如下，从上至下效率依次增强。

  * ALL：表示全表扫描，性能最差。
  * index：表示基于索引的全表扫描，先扫描索引再扫描全表数据。
  * range：表示使用索引范围查询。使用>、>=、<、<=、in等等。
  * ref：表示使用非唯一索引进行单值查询。
  * eq_ref：一般情况下出现在多表join查询，表示前面表的每一个记录，都只能匹配后面表的一 行结果。
  * const：表示使用主键或唯一索引做等值查询，常量查询。
  * NULL：表示不用访问表，速度最快。

* possible_keys

  表示查询时能够使用到的索引。注意并不一定会真正使用，显示的是索引名称。

* key

  表示查询时真正使用到的索引，显示的是索引名称。

* rows

  MySQL查询优化器会根据统计信息，估算SQL要查询到结果需要扫描多少行记录。原则上rows是 越少效率越高，可以直观的了解到SQL效率高低。

* key_len

  表示查询使用了索引的字节数量。可以判断是否全部使用了组合索引。

  key_len的计算规则如下：

  * 字符串类型

    字符串长度跟字符集有关：latin1=1、gbk=2、utf8=3、utf8mb4=4 

    char(n)：n*字符集长度 

    varchar(n)：n * 字符集长度 + 2字节

  * 数值类型

    TINYINT：1个字节

    SMALLINT：2个字节

    MEDIUMINT：3个字节

    INT、FLOAT：4个字节

    BIGINT、DOUBLE：8个字节

  * 时间类型

    DATE：3个字节

    TIMESTAMP：4个字节

    DATETIME：8个字节

  * 字段属性

    NULL属性占用1个字节，如果一个字段设置了NOT NULL，则没有此项。

  * Extra

    Extra表示很多额外的信息，各种操作会在Extra提示相关信息，常见几种如下：

    * Using where
  
      表示查询需要通过索引回表查询数据。
  
    * Using index
  
      表示查询需要通过索引，索引就可以满足所需数据。
  
    * Using filesort
  
      表示查询出来的结果需要额外排序，数据量小在内存，大的话在磁盘，因此有Using filesort 建议优化。
  
    * Using temprorary
  
      查询使用到了临时表，一般出现于去重、分组等操作。
  

## 回表查询

在之前介绍过，InnoDB索引有聚簇索引和辅助索引。聚簇索引的叶子节点存储行记录，InnoDB必须要有，且只有一个。辅助索引的叶子节点存储的是主键值和索引字段值，通过辅助索引无法直接定位行记录，通常情况下，需要扫码两遍索引树。先通过辅助索引定位主键值，然后再通过聚簇索引定位行记 录，这就叫做回表查询，它的性能比扫一遍索引树低。

**总结：通过索引查询主键值，然后再去聚簇索引查询记录信息**

## 覆盖索引

在SQL-Server官网的介绍如下：

![20201210-165316-0974.png](https://gitee.com/chuchin/img/raw/master/20201210-165316-0974.png)

在MySQL官网，类似的说法出现在explain查询计划优化章节，即explain的输出结果Extra字段为Using index时，能够触发索引覆盖。

![20201210-164517-0180.png](https://gitee.com/chuchin/img/raw/master/20201210-164517-0180.png)

不管是SQL-Server官网，还是MySQL官网，都表达了：**只需要在一棵索引树上就能获取SQL所需的所有列数据，无需回表，速度更快，这就叫做索引覆盖。**

实现索引覆盖最常见的方法就是：将被查询的字段，建立到组合索引。

## 最左前缀原则

复合索引使用时遵循最左前缀原则，最左前缀顾名思义，就是最左优先，即查询中使用到最左边的列， 那么查询就会使用到索引，如果从索引的第二列开始查找，索引将失效。

![20201210-160122-0049.png](https://gitee.com/chuchin/img/raw/master/20201210-160122-0049.png)

## LIKE 查询

**面试题：MySQL在使用like模糊查询时，索引能不能起作用？**

回答：MySQL在使用Like模糊查询时，索引是可以被使用的，只有把%字符写在后面才会使用到索引。

select * from user where name like '%o%'; //不起作用

select * from user where name like 'o%'; //起作用

select * from user where name like '%o'; //不起作用

## NULL 查询

**面试题：如果MySQL表的某一列含有NULL值，那么包含该列的索引是否有效？**

对MySQL来说，NULL是一个特殊的值，从概念上讲，NULL意味着“一个未知值”，它的处理方式与其他 值有些不同。比如：不能使用=，<，>这样的运算符，对NULL做算术运算的结果都是NULL，count时不会包括NULL行等，NULL比空字符串需要更多的存储空间等。

> “NULL columns require additional space in the row to record whether their values are NULL. For MyISAM tables, each NULL column takes one bit extra, rounded up to the nearest byte.”

NULL列需要增加额外空间来记录其值是否为NULL。对于MyISAM表，每一个空列额外占用一位，四舍 五入到最接近的字节。

虽然MySQL可以在含有NULL的列上使用索引，但NULL和其他数据还是有区别的，不建议列上允许为 NULL。最好设置NOT NULL，并给一个默认值，比如0和 ‘’ 空字符串等，如果是datetime类型，也可以设置系统当前时间或某个固定的特殊值，例如'1970-01-01 00:00:00'。

## 索引与排序

MySQL查询支持filesort和index两种方式的排序，filesort是先把结果查出，然后在缓存或磁盘进行排序操作，效率较低。使用index是指利用索引自动实现排序，不需另做排序操作，效率会比较高。

filesort有两种排序算法：双路排序和单路排序。

双路排序：需要两次磁盘扫描读取，最终得到用户数据。第一次将排序字段读取出来，然后排序；第二 次去读取其他字段数据。

单路排序：从磁盘查询所需的所有列数据，然后在内存排序将结果返回。如果查询数据超出缓存 sort_buffer，会导致多次磁盘读取操作，并创建临时表，最后产生了多次IO，反而会增加负担。解决方 案：少使用select *；增加sort_buffer_size容量和max_length_for_sort_data容量。

如果我们Explain分析SQL，结果中Extra属性显示Using filesort，表示使用了filesort排序方式，需要优 化。如果Extra属性显示Using index时，表示覆盖索引，也表示所有操作在索引上完成，也可以使用 index排序方式，建议大家尽可能采用覆盖索引。

* 以下几种情况，会使用index方式的排序。

  * ORDER BY 子句索引列组合满足索引最左前缀

    ```sql
    explain select id from user order by id; //对应(id)、(id,name)索引有效
    ```

  * WHERE子句+ORDER BY子句索引列组合满足索引最左前缀

    ```sql
    explain select id from user where age=18 order by name; //对应 (age,name)索引
    ```

* 以下几种情况，会使用filesort方式的排序。

  * 对索引列同时使用了ASC和DESC

    ```sql
    explain select id from user order by age asc,name desc; //对应 (age,name)索引
    ```

  * WHERE子句和ORDER BY子句满足最左前缀，但where子句使用了范围查询（例如>、<、in 等）

    ```sql
    explain select id from user where age>10 order by name; //对应 (age,name)索引
    ```

  * ORDER BY或者WHERE+ORDER BY索引列没有满足索引最左前列

    ```sql
    explain select id from user order by name; //对应(age,name)索引
    ```

  * 使用了不同的索引，MySQL每次只采用一个索引，ORDER BY涉及了两个索引

    ```sql
    explain select id from user order by name,age; //对应(name)、(age)两个索 引
    ```

  * WHERE子句与ORDER BY子句，使用了不同的索引

    ```sql
    explain select id from user where name='tom' order by age; //对应 (name)、(age)索引
    ```

  * WHERE子句或者ORDER BY子句中索引列使用了表达式，包括函数表达式

    ```sql
    explain select id from user order by abs(age); //对应(age)索引
    ```

    