---
title: "MySQL 索引 类型"
date: 2020-09-05T10:21:43+08:00
draft: false
tags: ["MySQL"]
---

索引可以提升查询速度，会影响where查询，以及order by排序。MySQL索引类型如下：

* 从**索引存储**结构划分：**B Tree索引**、**Hash索引**、**FULLTEXT全文索引**、**R Tree索引**
* 从**应用层次**划分：**普通索引**、**唯一索引**、**主键索引**、**复合索引**
* 从**索引键值类型**划分：**主键索引**、**辅助索引（二级索引）**
* 从**数据存储和索引键值逻辑关系**划分：**聚集索引（聚簇索引）**、**非聚集索引（非聚簇索引）**

## 普通索引

这是最基本的索引类型，基于普通字段建立的索引，没有任何限制。

创建普通索引的方法如下：

* CREATE INDEX <索引的名字> ON tablename (字段名);
* ALTER TABLE tablename ADD INDEX [索引的名字] (字段名);
* CREATE TABLE tablename ( [...], INDEX [索引的名字] (字段名) );

## 唯一索引

与"普通索引"类似，不同的就是：索引字段的值必须唯一，但允许有空值 。在创建或修改表时追加唯一 约束，就会自动创建对应的唯一索引。 

创建唯一索引的方法如下：

* CREATE UNIQUE INDEX <索引的名字> ON tablename (字段名);
* ALTER TABLE tablename ADD UNIQUE INDEX [索引的名字] (字段名);
* CREATE TABLE tablename ( [...], UNIQUE [索引的名字] (字段名) ;

## 主键索引

它是一种特殊的唯一索引，不允许有空值。在创建或修改表时追加主键约束即可，每个表只能有一个主键。

创建主键索引的方法如下：

* CREATE TABLE tablename ( [...], PRIMARY KEY (字段名) );
* ALTER TABLE tablename ADD PRIMARY KEY (字段名);

## 复合索引

单一索引是指索引列为一列的情况，即新建索引的语句只实施在一列上；用户可以在多个列上建立索 引，这种索引叫做组复合索引（组合索引）。复合索引可以代替多个单一索引，相比多个单一索引复合 索引所需的开销更小。

索引同时有两个概念叫做窄索引和宽索引，窄索引是指索引列为1-2列的索引，宽索引也就是索引列超 过2列的索引，设计索引的一个重要原则就是能用窄索引不用宽索引，因为窄索引往往比组合索引更有 效。

创建组合索引的方法如下：

* CREATE INDEX <索引的名字> ON tablename (字段名1，字段名2...);
* ALTER TABLE tablename ADD INDEX [索引的名字] (字段名1，字段名2...);
* CREATE TABLE tablename ( [...], INDEX [索引的名字] (字段名1，字段名2...) );

复合索引使用注意事项：

* 复合索引字段是有顺序的，在查询使用时要按照索引字段的顺序使用。例如select * from user where name=xx and age=xx，匹配(name,age)组合索引，不匹配(age,name)。
* 何时使用复合索引，要根据where条件建索引，注意不要过多使用索引，过多使用会对更新操作效 率有很大影响。
* 如果表已经建立了(col1，col2)，就没有必要再单独建立（col1）；如果现在有(col1)索引，如果查 询需要col1和col2条件，可以建立(col1,col2)复合索引，对于查询有一定提高。

## 全文索引

查询操作在数据量比较少时，可以使用like模糊查询，但是对于大量的文本数据检索，效率很低。如果 使用全文索引，查询速度会比like快很多倍。在MySQL 5.6 以前的版本，只有MyISAM存储引擎支持全 文索引，从MySQL 5.6开始MyISAM和InnoDB存储引擎均支持。

创建全文索引的方法如下：

* CREATE FULLTEXT INDEX <索引的名字> ON tablename (字段名);
* ALTER TABLE tablename ADD FULLTEXT [索引的名字] (字段名);
* CREATE TABLE tablename ( [...], FULLTEXT KEY [索引的名字] (字段名) ;

和常用的like模糊查询不同，全文索引有自己的语法格式，使用 match 和 against 关键字，比如

```sql
select * from user where match(name) against('aaa');
```

全文索引使用注意事项：

* 全文索引必须在字符串、文本字段上建立。
* 全文索引字段值必须在最小字符和最大字符之间的才会有效。（innodb：3-84；myisam：484）
* 全文索引字段值要进行切词处理，按syntax字符进行切割，例如b+aaa，切分成b和aaa
* 全文索引匹配查询，默认使用的是等值匹配，例如a匹配a，不会匹配ab,ac。如果想匹配可以在布 尔模式下搜索a*

```sql
select * from user where match(name) against('a*' in boolean mode);
```

