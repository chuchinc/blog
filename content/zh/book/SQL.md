---
title: "SQL即查即用"
date: 2019-11-05T16:28:34+08:00
draft: false
tags: ["SQL"]                       
---

![20201210-131047-0048.jpg](https://gitee.com/chuchin/img/raw/master/20201210-131047-0048.jpg)

京东入手的一本工具书，主要介绍SQL的知识，比较全，书中配套资料：[https://github.com/chuchinc/kl-book-resource](https://github.com/chuchinc/kl-book-resource)

## 简单查询

### SELECT语句基本结构

```mysql
SELECT select_list
[ INTO new_table ]
FROM table_name
[ WHERE search_condition ]
[ GROUP BY group_by_condition ]
[ HAVING search_condition ]
[ ORDER BY order_expression [ ASC | DESC ]];
```

### 单列查询

```mysql
#语法格式：
SELECT select_list FROM table_name;
```

```mysql
#eg:
SELECT goods_name FROM goods;
```

| goods\_name               |
| :------------------------ |
| 华为 M2 10.0 平板电脑     |
| 华为 M2 8英寸平板电脑     |
| 荣耀畅玩5X 智能手机       |
| 华为 Mate 8 64GB          |
| 三星55M5 智能液晶电视     |
| TCL D50A710 液晶电视      |
| 海信 LED55EC290N 液晶电视 |
| 海尔 BCD-572WDPM电冰箱    |
| 三星 BCD-535WKZM电冰箱    |
| 索尼 D7200单反相机        |

### 多列查询

```mysql
#语法格式
SELECT select_list,...,select_list FROM table_name;
```

```mysql
#eg:
SELECT goods_id,goods_name,market_price FROM table_name;
```

| goods\_id | goods\_name               | market\_price |
| :-------- | :------------------------ | :------------ |
| 39        | 华为 M2 10.0 平板电脑     | 2388.00       |
| 41        | 华为 M2 8英寸平板电脑     | 1688.00       |
| 49        | 荣耀畅玩5X 智能手机       | 1099.00       |
| 51        | 华为 Mate 8 64GB          | 3799.00       |
| 56        | 三星55M5 智能液晶电视     | 3899.00       |
| 57        | TCL D50A710 液晶电视      | 2899.00       |
| 58        | 海信 LED55EC290N 液晶电视 | 3299.00       |
| 106       | 海尔 BCD-572WDPM电冰箱    | 3499.00       |
| 109       | 三星 BCD-535WKZM电冰箱    | 3599.00       |
| 114       | 索尼 D7200单反相机        | 3999.00       |

### 查询所有列

```mysql
#语法格式
SELECT * FROM table_name;
```

```mysql
#eg:
SELECT * FROM goods_type;
```

| id   | name     |
| :--- | :------- |
| 4    | 手机     |
| 8    | 化妆品   |
| 15   | 平板电脑 |
| 16   | 路由器   |
| 18   | 电视     |
| 29   | 冰箱     |
| 34   | 雪地鞋   |
| 35   | 护手霜   |
| 36   | 薯片     |

### 别名的应用

```mysql
#创建别名以增强阅读性
#(1)使用双引号创建别名
SELECT goods_name "商品名称" FROM goods;
#(2)使用单引号创建别名
SELECT goods_name '商品名称' FROM goods;
#(3)不使用引号创建别名
SELECT goods_name 商品名称 FROM goods;
#(4)使用AS关键字创建别名
SELECT goods_name AS "商品名称" FROM goods;
```

### 删除重复数据

```mysql
#语法格式
SELECT [DISTINCT | ALL] select_list FROM table_name;
```

```mysql
#eg:
SELECT consignee,address,mobile FROM orderform;
```

| consignee | address                                  | mobile      |
| :-------- | :--------------------------------------- | :---------- |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 明日科技  | 北京朝阳北路104号楼4层402室              | 13578982158 |
| 明日科技  | 北京朝阳北路104号楼4层402室              | 13578982158 |
| 明日科技  | 北京朝阳北路104号楼4层402室              | 13578982158 |
| andy      | 上海海淀西三环北路21号                   | 13211111111 |
| andy      | 上海海淀西三环北路21号                   | 13211111111 |

```mysql
#eg:
SELECT DISTINCT consignee,address,mobile FROM orderform;
```

| consignee | address                                  | mobile      |
| :-------- | :--------------------------------------- | :---------- |
| 小明      | 深圳东门深南东路4003号世界金融中心B座4楼 | 18910441212 |
| 明日科技  | 北京朝阳北路104号楼4层402室              | 13578982158 |
| andy      | 上海海淀西三环北路21号                   | 13211111111 |

### 限制查询结果

#### 在SQL Server中限制查询结果

```mysql
SELECT TOP n FROM table;
```

#### 在MySQL中限制查询结果

##### 限制查询前n条数据

   ```mysql
   SELECT goods_name,market_price FROM shop.goods LIMIT 5;
   ```

| goods\_name           | market\_price |
| :-------------------- | :------------ |
| 华为 M2 10.0 平板电脑 | 2388.00       |
| 华为 M2 8英寸平板电脑 | 1688.00       |
| 荣耀畅玩5X 智能手机   | 1099.00       |
| 华为 Mate 8 64GB      | 3799.00       |
| 三星55M5 智能液晶电视 | 3899.00       |

##### 限制查询n条数据

   ```mysql
   #第3条数据开始的5条数据
   SELECT goods_name,market_price FROM shop.goods LIMIT 2,5;
   #参数1是开始读取的第一条记录的编号
   #参数2是要查询记录的个数
   ```

| goods\_name               | market\_price |
| :------------------------ | :------------ |
| 荣耀畅玩5X 智能手机       | 1099.00       |
| 华为 Mate 8 64GB          | 3799.00       |
| 三星55M5 智能液晶电视     | 3899.00       |
| TCL D50A710 液晶电视      | 2899.00       |
| 海信 LED55EC290N 液晶电视 | 3299.00       |

   ```mysql
   #第4条数据开始的2条数据
   SELECT goods_name,market_price FROM shop.goods LIMIT 2 OFFSET 3;
   #参数1是查询行数
   #参数2是查询的起始位置
   ```

| goods\_name           | market\_price |
| :-------------------- | :------------ |
| 华为 Mate 8 64GB      | 3799.00       |
| 三星55M5 智能液晶电视 | 3899.00       |

#### 在Oracle中限制查询结果

```mysql
#查看前5条数据
SELECT goods_name,market_price FROM goods WHERE ROWNUM <= 5;
```

## 计算列查询

### 连接列值

连接列值是将多个列中的数据合并到同一列中，合并后起别名便于查看

(MySQL中连接列值必须使用CONCAT拼接)

```mysql
SELECT CONCAT(name,cat_name) AS '品牌信息' FROM brand;
```

| 品牌信息                       |
| :----------------------------- |
| 华为/HUAWEI手机、数码、配件    |
| 索尼/SONY手机、数码、配件      |
| 诺基亚/NOKIA手机、数码、配件   |
| TCL手机、数码、配件            |
| 飞利浦/PHILIPS手机、数码、配件 |
| OPPO手机、数码、配件           |
| 苹果/Apple手机、数码、配件     |
| 海尔/Haier手机、数码、配件     |

### 查询中使用计算列

#### 减法运算符"-"的应用

```mysql
SELECT goods_id AS 商品ID,goods_name AS 商品名称,
(shop_price - cost_price) AS 销售利润
FROM goods;
```

| 商品ID | 商品名称                  | 销售利润 |
| :----- | :------------------------ | :------- |
| 39     | 华为 M2 10.0 平板电脑     | 788.00   |
| 41     | 华为 M2 8英寸平板电脑     | 388.00   |
| 49     | 荣耀畅玩5X 智能手机       | 99.00    |
| 51     | 华为 Mate 8 64GB          | 199.00   |
| 56     | 三星55M5 智能液晶电视     | 299.00   |
| 57     | TCL D50A710 液晶电视      | 399.00   |
| 58     | 海信 LED55EC290N 液晶电视 | 211.00   |
| 106    | 海尔 BCD-572WDPM电冰箱    | 244.00   |
| 109    | 三星 BCD-535WKZM电冰箱    | 200.00   |
| 114    | 索尼 D7200单反相机        | 599.00   |

#### 乘法运算符"*"的应用

```mysql
SELECT goods_id AS 商品ID,goods_name AS 商品名称,
(shop_price * sales_sum) AS 销售额
FROM goods;
```

| 商品ID | 商品名称                  | 销售额   |
| :----- | :------------------------ | :------- |
| 39     | 华为 M2 10.0 平板电脑     | 0.00     |
| 41     | 华为 M2 8英寸平板电脑     | 0.00     |
| 49     | 荣耀畅玩5X 智能手机       | 0.00     |
| 51     | 华为 Mate 8 64GB          | 0.00     |
| 56     | 三星55M5 智能液晶电视     | 3799.00  |
| 57     | TCL D50A710 液晶电视      | 13995.00 |
| 58     | 海信 LED55EC290N 液晶电视 | 3199.00  |
| 106    | 海尔 BCD-572WDPM电冰箱    | 0.00     |
| 109    | 三星 BCD-535WKZM电冰箱    | 0.00     |
| 114    | 索尼 D7200单反相机        | 0.00     |

#### 算数运算符的综合应用

```mysql
SELECT goods_id AS 商品ID,goods_name 商品名称,
(sales_sum*shop_price - sales_sum*cost_price)/sales_sum AS 销售利润
FROM goods
WHERE sales_sum <> 0;
```

| 商品ID | 商品名称                  | 销售利润   |
| :----- | :------------------------ | :--------- |
| 56     | 三星55M5 智能液晶电视     | 299.000000 |
| 57     | TCL D50A710 液晶电视      | 399.000000 |
| 58     | 海信 LED55EC290N 液晶电视 | 211.000000 |

### 查询中使用表达式

#### 数值表达式

```mysql
SELECT goods_id AS 商品ID,goods_name 商品名称,
cost_price + 50 AS 进价加50
FROM goods;
```

| 商品ID | 商品名称                  | 进价加50 |
| :----- | :------------------------ | :------- |
| 39     | 华为 M2 10.0 平板电脑     | 1550.00  |
| 41     | 华为 M2 8英寸平板电脑     | 1250.00  |
| 49     | 荣耀畅玩5X 智能手机       | 950.00   |
| 51     | 华为 Mate 8 64GB          | 3550.00  |
| 56     | 三星55M5 智能液晶电视     | 3550.00  |
| 57     | TCL D50A710 液晶电视      | 2450.00  |
| 58     | 海信 LED55EC290N 液晶电视 | 3038.00  |
| 106    | 海尔 BCD-572WDPM电冰箱    | 3205.00  |
| 109    | 三星 BCD-535WKZM电冰箱    | 3349.00  |
| 114    | 索尼 D7200单反相机        | 3150.00  |

#### 字符表达式

```MYSQL
SELECT goods_id AS 商品ID,goods_name 商品名称,
CONCAT(CONVERT(sales_sum,char(2)),'个') AS 销售数量,
CONCAT(CONVERT(shop_price,char(8)),'元') AS 商场价格 FROM goods;
```

| 商品ID | 商品名称                  | 销售数量 | 商场价格  |
| :----- | :------------------------ | :------- | :-------- |
| 39     | 华为 M2 10.0 平板电脑     | 0个      | 2288.00元 |
| 41     | 华为 M2 8英寸平板电脑     | 0个      | 1588.00元 |
| 49     | 荣耀畅玩5X 智能手机       | 0个      | 999.00元  |
| 51     | 华为 Mate 8 64GB          | 0个      | 3699.00元 |
| 56     | 三星55M5 智能液晶电视     | 1个      | 3799.00元 |
| 57     | TCL D50A710 液晶电视      | 5个      | 2799.00元 |
| 58     | 海信 LED55EC290N 液晶电视 | 1个      | 3199.00元 |
| 106    | 海尔 BCD-572WDPM电冰箱    | 0个      | 3399.00元 |
| 109    | 三星 BCD-535WKZM电冰箱    | 0个      | 3499.00元 |
| 114    | 索尼 D7200单反相机        | 0个      | 3699.00元 |

#### 使用表达式创建新列

```mysql
SELECT goods_id AS 商品ID,goods_name 商品名称,1+1,'字符'+'串列'
FROM goods;
```

| 商品ID | 商品名称                  | 1+1  | '字符'+'串列' |
| :----- | :------------------------ | :--- | :------------ |
| 39     | 华为 M2 10.0 平板电脑     | 2    | 0             |
| 41     | 华为 M2 8英寸平板电脑     | 2    | 0             |
| 49     | 荣耀畅玩5X 智能手机       | 2    | 0             |
| 51     | 华为 Mate 8 64GB          | 2    | 0             |
| 56     | 三星55M5 智能液晶电视     | 2    | 0             |
| 57     | TCL D50A710 液晶电视      | 2    | 0             |
| 58     | 海信 LED55EC290N 液晶电视 | 2    | 0             |
| 106    | 海尔 BCD-572WDPM电冰箱    | 2    | 0             |
| 109    | 三星 BCD-535WKZM电冰箱    | 2    | 0             |
| 114    | 索尼 D7200单反相机        | 2    | 0             |

Oracle中，拼接字符串的运算符为“||”

```mysql
SELECT goods_id AS 商品ID,goods_name 商品名称,1+1,'字符'||'串列'
FROM goods;
```

## 条件查询

### WHERE子句

```mysql
SELECT <字段列表>
FROM <表名>
WHERE <条件表达式>
```
| 运算符 | 说明       |
| ------ | ---------- |
| =      | 等于       |
| >      | 大于       |
| <      | 小于       |
| >=     | 大于或等于 |
| <=     | 小于或等于 |
| !>     | 不大于     |
| !<     | 不小于     |
| <>或!= | 不等于     |

### 使用比较运算符限制查询结果

#### 使用“=”查询数据

```mysql
SELECT * FROM goods WHERE goods_id = 106;
```

| goods\_id | cat\_id | goods\_name            | click\_count | brand\_id | store\_count | comment\_count | weight | market\_price | shop\_price | cost\_price | is\_new | goods\_type | spec\_type | exchange\_integral | sales\_sum |
| :-------- | :------ | :--------------------- | :----------- | :-------- | :----------- | :------------- | :----- | :------------ | :---------- | :---------- | :------ | :---------- | :--------- | :----------------- | :--------- |
| 106       | 131     | 海尔 BCD-572WDPM电冰箱 | 27           | 14        | 1000         | 0              | 500    | 3499.00       | 3399.00     | 3155.00     | 0       | 29          | 0          | 100                | 0          |

#### 使用“>”查询数据

```mysql
SELECT goods_id,goods_name,click_count FROM goods
WHERE click_count > 50;
```

| goods\_id | goods\_name           | click\_count |
| :-------- | :-------------------- | :----------- |
| 39        | 华为 M2 10.0 平板电脑 | 52           |
| 49        | 荣耀畅玩5X 智能手机   | 98           |
| 56        | 三星55M5 智能液晶电视 | 58           |
| 57        | TCL D50A710 液晶电视  | 60           |

#### 使用“<”查询数据

```mysql
SELECT goods_id,goods_name,store_count FROM goods 
WHERE store_count < 1000;
```

| goods\_id | goods\_name               | store\_count |
| :-------- | :------------------------ | :----------- |
| 56        | 三星55M5 智能液晶电视     | 598          |
| 57        | TCL D50A710 液晶电视      | 590          |
| 58        | 海信 LED55EC290N 液晶电视 | 598          |

#### 使用“>=”查询数据

```mysql
SELECT goods_id,goods_name,sales_sum
FROM goods
WHERE sales_sum >= 5;
```

| goods\_id | goods\_name          | sales\_sum |
| :-------- | :------------------- | :--------- |
| 57        | TCL D50A710 液晶电视 | 5          |

#### 使用“<=”查询数据

```mysql
SELECT goods_id,goods_name,click_count
FROM goods
WHERE click_count <= 20;
```

| goods\_id | goods\_name            | click\_count |
| :-------- | :--------------------- | :----------- |
| 51        | 华为 Mate 8 64GB       | 19           |
| 109       | 三星 BCD-535WKZM电冰箱 | 17           |
| 114       | 索尼 D7200单反相机     | 15           |

#### 使用“!>”查询数据

MySQL无此运算符

```mysql
SELECT goods_id,goods_name,shop_price
FROM goods 
WHERE shop_price !> 2000; 
```

#### 使用“!<”查询数据

MySQL无此运算符

```mysql
SELECT goods_id,goods_name,shop_price
FROM goods 
WHERE shop_price !< 2000; 
```

#### 使用"!="和"<>"查询数据

```mysql
SELECT goods_id,goods_name,is_new FROM goods WHERE is_new != 0;
SELECT goods_id,goods_name,is_new FROM goods WHERE is_new <> 0;
```

| goods\_id | goods\_name            | is\_new |
| :-------- | :--------------------- | :------ |
| 41        | 华为 M2 8英寸平板电脑  | 1       |
| 57        | TCL D50A710 液晶电视   | 1       |
| 109       | 三星 BCD-535WKZM电冰箱 | 1       |

## 范围查询

### 查询两个值之间的数据

```MYSQL
SELECT goods_id AS 商品ID,goods_name 商品名称,market_price AS 市场价
FROM goods
WHERE market_price BETWEEN 1000 AND 3000;
```

| 商品ID | 商品名称              | 市场价  |
| :----- | :-------------------- | :------ |
| 39     | 华为 M2 10.0 平板电脑 | 2388.00 |
| 41     | 华为 M2 8英寸平板电脑 | 1688.00 |
| 49     | 荣耀畅玩5X 智能手机   | 1099.00 |
| 57     | TCL D50A710 液晶电视  | 2899.00 |

### 查询两个日期之间的数据

```MYSQL
SELECT ISBN,BookName,INTime AS 数据录入时间 FROM bookinfo_zerobasis
WHERE INTime
BETWEEN '2017-12-1' AND '2018-12-1';
```

| ISBN          | BookName           | 数据录入时间               |
| :------------ | :----------------- | :------------------------- |
| 7-110-12073-5 | 零基础学C#         | 2018-03-17 03:35:13.530000 |
| 7-110-12073-6 | 零基础学JavaScript | 2018-03-17 03:37:35.467000 |
| 7-110-12073-7 | 零基础学HTML5+CSS3 | 2018-03-17 03:40:13.167000 |
| 7-110-12073-8 | 零基础学Oracle     | 2018-01-23 02:05:53.543000 |

### 在BETWEEN中使用日期函数

```MYSQL
SELECT NOW();
```

| NOW\(\)             |
| :------------------ |
| 2019-11-13 13:07:20 |

```MYSQL
SELECT ISBN,BookName,INTime 数据录入时间 FROM bookinfo_zerobasis WHERE INTime
BETWEEN
DATE_ADD(NOW(),INTERVAL 1 DAY )
AND
NOW();
```

### 查询不在两个数之间的数据

```mysql
SELECT goods_id,goods_name,market_price
FROM goods
WHERE market_price NOT BETWEEN 2000 AND 3000;
```

| goods\_id | goods\_name               | market\_price |
| :-------- | :------------------------ | :------------ |
| 41        | 华为 M2 8英寸平板电脑     | 1688.00       |
| 49        | 荣耀畅玩5X 智能手机       | 1099.00       |
| 51        | 华为 Mate 8 64GB          | 3799.00       |
| 56        | 三星55M5 智能液晶电视     | 3899.00       |
| 58        | 海信 LED55EC290N 液晶电视 | 3299.00       |
| 106       | 海尔 BCD-572WDPM电冰箱    | 3499.00       |
| 109       | 三星 BCD-535WKZM电冰箱    | 3599.00       |
| 114       | 索尼 D7200单反相机        | 3999.00       |

### 日期时间查询

#### 转换日期格式

##### 把长日期格式数据转换为短日期格式数据

SQL Server

```mysql
CONVERT(data_type[(length)], expression, style)
#data_type: 要转换的数据类型
#express:DATATIME类型的数据
#style：指定转换形式
```

MySQL

```mysql
#DATE转字符串
SELECT DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s');
```

| DATE\_FORMAT\(NOW\(\),'%Y-%m-%d %H:%i:%s'\) |
| :------------------------------------------ |
| 2020-11-13 13:29:51                         |

```MYSQL
#字符串转DATE
SELECT STR_TO_DATE('2020-11-13 13:29:51','%Y-%m-%d')
```

| STR\_TO\_DATE\('2020-11-13 13:29:51','%Y-%m-%d'\) |
| :------------------------------------------------ |
| 2020-11-13                                        |

##### 将日期格式中的“-”转换为“/”

```MYSQL
SELECT ISBN,bookname,writer,price,intime FROM bookinfo ORDER BY ISBN;
```

```MYSQL
SELECT ISBN,bookname, DATE_FORMAT(INTime,'%Y-%m-%d %H:%i:%s') AS 数据录入日期
FROM bookinfo ORDER BY ISBN;
```

| ISBN          | bookname                          | 数据录入日期        |
| :------------ | :-------------------------------- | :------------------ |
| 7-110-12000-1 | Java项目开发实战入门              | 2018-01-22 06:53:55 |
| 7-110-12000-2 | C语言项目开发实战入门             | 2018-01-22 06:55:09 |
| 7-110-12000-3 | Android项目开发实战入门           | 2018-01-22 06:57:14 |
| 7-110-12000-4 | JavaWeb项目开发实战入门           | 2018-01-22 06:58:19 |
| 7-110-12000-5 | C++项目开发实战入门               | 2018-01-22 06:58:59 |
| 7-110-12000-6 | JSP项目开发实战入门               | 2018-01-22 06:59:49 |
| 7-110-12000-7 | PHP项目开发实战入门               | 2018-01-22 07:12:36 |

```MYSQL
SELECT ISBN,bookname, REPLACE(DATE_FORMAT(INTime,'%Y-%m-%d %H:%i:%s'),'-','/')
AS 数据录入日期
FROM bookinfo ORDER BY ISBN;
```

| ISBN          | bookname                          | 数据录入日期        |
| :------------ | :-------------------------------- | :------------------ |
| 7-110-12000-1 | Java项目开发实战入门              | 2018/01/22 06:53:55 |
| 7-110-12000-2 | C语言项目开发实战入门             | 2018/01/22 06:55:09 |
| 7-110-12000-3 | Android项目开发实战入门           | 2018/01/22 06:57:14 |
| 7-110-12000-4 | JavaWeb项目开发实战入门           | 2018/01/22 06:58:19 |
| 7-110-12000-5 | C++项目开发实战入门               | 2018/01/22 06:58:59 |
| 7-110-12000-6 | JSP项目开发实战入门               | 2018/01/22 06:59:49 |
| 7-110-12000-7 | PHP项目开发实战入门               | 2018/01/22 07:12:36 |
| 7-110-12000-8 | C#项目开发实战入门                | 2018/01/22 07:13:49 |

#### 计算两个日期的间隔天数

```mysql
DATEDIFF(datepart,startdate,enddate)
#datepart规定了应在那一日期部分计算间隔差额的参数
```

```mysql
SELECT DATEDIFF('2008-12-30','2008-12-29') AS DiffDate
```

| DiffDate |
| :------- |
| 1        |

#### 按指定日期查询数据

##### DAY()函数

```mysql
SELECT DAY(0) AS MY_DAY1,DAY('2019-10-09') AS MY_DAY2;
```

| MY\_DAY1 | MY\_DAY2 |
| :------- | :------- |
| 0        | 9        |

##### MONTH()函数

```mysql
SELECT MONTH('2019-10-09');
```

| MONTH\('2019-10-09'\) |
| :-------------------- |
| 10                    |

##### YEAR()

```MYSQL
SELECT YEAR('2019-10-09');
```

| YEAR\('2019-10-09'\) |
| :------------------- |
| 2019                 |

##### 综合案例

```mysql
#查找指定日期的月和年查询
SELECT BookName,Type,pDate FROM bookinfo
WHERE MONTH(pDate)=10 AND YEAR(pDate)=2017
AND Type = '零基础系列';
```

| BookName           | Type       | pDate      |
| :----------------- | :--------- | :--------- |
| 零基础学C#         | 零基础系列 | 2017-10-01 |
| 零基础学JavaScript | 零基础系列 | 2017-10-01 |
| 零基础学HTML5+CSS3 | 零基础系列 | 2017-10-01 |

## 使用逻辑运算符过滤数据

### 使用AND运算符

```mysql
SELECT goods_id,goods_name,shop_price
FROM goods
WHERE shop_price > 3000 AND shop_price < 6000;
```

| goods\_id | goods\_name               | shop\_price |
| :-------- | :------------------------ | :---------- |
| 51        | 华为 Mate 8 64GB          | 3699.00     |
| 56        | 三星55M5 智能液晶电视     | 3799.00     |
| 58        | 海信 LED55EC290N 液晶电视 | 3199.00     |
| 106       | 海尔 BCD-572WDPM电冰箱    | 3399.00     |
| 109       | 三星 BCD-535WKZM电冰箱    | 3499.00     |
| 114       | 索尼 D7200单反相机        | 3699.00     |

```mysql
SELECT goods_name,click_count,store_count,shop_price
FROM goods
WHERE click_count > 20 AND store_count = 1000 AND shop_price > 2000;
```

| goods\_name            | click\_count | store\_count | shop\_price |
| :--------------------- | :----------- | :----------- | :---------- |
| 华为 M2 10.0 平板电脑  | 52           | 1000         | 2288.00     |
| 海尔 BCD-572WDPM电冰箱 | 27           | 1000         | 3399.00     |

### 使用OR运算符

```mysql
SELECT ISBN,BookName,Writer,Price
FROM bookinfo_zerobasis
WHERE BookName = '零基础学Java' OR BookName = '零基础学PHP';
```

| ISBN          | BookName     | Writer   | Price   |
| :------------ | :----------- | :------- | :------ |
| 7-110-12073-1 | 零基础学Java | 明日科技 | 69.8000 |
| 7-110-12073-4 | 零基础学PHP  | 明日科技 | 79.8000 |

```mysql
SELECT BookName,Price,pDate
FROM bookinfo
WHERE BookName LIKE '%PHP%' OR BookName LIKE '%Oracle%' OR BookName LIKE '%Android%';
```

| BookName                | Price   | pDate      |
| :---------------------- | :------ | :--------- |
| Android项目开发实战入门 | 59.8000 | 2017-05-01 |
| PHP项目开发实战入门     | 69.8000 | 2017-04-01 |
| 零基础学Android         | 89.8000 | 2017-09-01 |
| 零基础学PHP             | 79.8000 | 2017-09-01 |
| 零基础学Oracle          | 79.8000 | 2018-01-09 |
| Android精彩编程200例    | 89.8000 | 2017-08-01 |

### 使用NOT运算符

```mysql
SELECT goods_id,goods_name,store_count
FROM goods
WHERE NOT store_count = 1000;
```

| goods\_id | goods\_name               | store\_count |
| :-------- | :------------------------ | :----------- |
| 56        | 三星55M5 智能液晶电视     | 598          |
| 57        | TCL D50A710 液晶电视      | 590          |
| 58        | 海信 LED55EC290N 液晶电视 | 598          |

```mysql
SELECT goods_id,goods_name,store_count
FROM goods
WHERE store_count != 1000;
```

| goods\_id | goods\_name               | store\_count |
| :-------- | :------------------------ | :----------- |
| 56        | 三星55M5 智能液晶电视     | 598          |
| 57        | TCL D50A710 液晶电视      | 590          |
| 58        | 海信 LED55EC290N 液晶电视 | 598          |

### 逻辑运算符的优先级

NOT--->AND--->OR

```MYSQL
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE cat_id = 191 OR cat_id = 123 AND shop_price > 2000;
```

| cat\_id | goods\_name           | shop\_price |
| :------ | :-------------------- | :---------- |
| 191     | 华为 M2 10.0 平板电脑 | 2288.00     |
| 191     | 华为 M2 8英寸平板电脑 | 1588.00     |
| 123     | 华为 Mate 8 64GB      | 3699.00     |

```MYSQL
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE (cat_id = 191 OR cat_id = 123) AND shop_price > 2000;
```

| cat\_id | goods\_name           | shop\_price |
| :------ | :-------------------- | :---------- |
| 191     | 华为 M2 10.0 平板电脑 | 2288.00     |
| 123     | 华为 Mate 8 64GB      | 3699.00     |

```MYSQL
SELECT BookName,publisher,Writer
FROM bookinfo
WHERE (BookName LIKE '%PHP%' OR BookName LIKE '%JSP%') AND (NOT publisher = '机械工业出版社');
```

| BookName            | publisher      | Writer   |
| :------------------ | :------------- | :------- |
| JSP项目开发实战入门 | 吉林大学出版社 | 明日科技 |
| PHP项目开发实战入门 | 吉林大学出版社 | 明日科技 |
| 零基础学PHP         | 吉林大学出版社 | 明日科技 |

```MYSQL
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE (NOT cat_id = 191) AND (NOT cat_id = 123);
```

| cat\_id | goods\_name               | shop\_price |
| :------ | :------------------------ | :---------- |
| 130     | 三星55M5 智能液晶电视     | 3799.00     |
| 130     | TCL D50A710 液晶电视      | 2799.00     |
| 130     | 海信 LED55EC290N 液晶电视 | 3199.00     |
| 131     | 海尔 BCD-572WDPM电冰箱    | 3399.00     |
| 131     | 三星 BCD-535WKZM电冰箱    | 3499.00     |
| 104     | 索尼 D7200单反相机        | 3699.00     |

## 使用IN操作符过滤数据

### 使用IN查询数据

```mysql
SELECT column_name
FROM table_name
WHERE column_name IN (value1,value2,...)
```

```mysql
SELECT cat_id,goods_name,shop_price
FROM goods 
WHERE cat_id IN (191,123,131);
```

| cat\_id | goods\_name            | shop\_price |
| :------ | :--------------------- | :---------- |
| 191     | 华为 M2 10.0 平板电脑  | 2288.00     |
| 191     | 华为 M2 8英寸平板电脑  | 1588.00     |
| 123     | 荣耀畅玩5X 智能手机    | 999.00      |
| 123     | 华为 Mate 8 64GB       | 3699.00     |
| 131     | 海尔 BCD-572WDPM电冰箱 | 3399.00     |
| 131     | 三星 BCD-535WKZM电冰箱 | 3499.00     |

实际上，使用IN操作符实现了与OR运算符相同的功能

```mysql
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE cat_id = 191 OR cat_id = 123 OR cat_id = 131;
```

还能使用字符类型

```mysql
SELECT name,cat_name 
FROM brand 
WHERE name IN ('OPPO','维维','湾仔码头','华硕/ASUS');
```

| name      | cat\_name            |
| :-------- | :------------------- |
| OPPO      | 手机、数码、配件     |
| 维维      | 茶冲乳品、酒水、饮料 |
| 湾仔码头  | 生鲜食品             |
| 华硕/ASUS | 电脑、平板、办公设备 |

### 在IN中使用算术表达式

```mysql
SELECT goods_name,shop_price 
FROM goods 
WHERE shop_price IN (3799-100,3799,3799+100);
```

| goods\_name           | shop\_price |
| :-------------------- | :---------- |
| 华为 Mate 8 64GB      | 3699.00     |
| 三星55M5 智能液晶电视 | 3799.00     |
| 索尼 D7200单反相机    | 3699.00     |

### 在IN中使用列进行查询

```mysql
SELECT goods_name,market_price,shop_price
FROM goods
WHERE 3899 IN (market_price,shop_price);
```

| goods\_name           | market\_price | shop\_price |
| :-------------------- | :------------ | :---------- |
| 三星55M5 智能液晶电视 | 3899.00       | 3799.00     |

### 使用NOT IN查询数据

```mysql
SELECT column_name
FROM table_name
WHERE column_name NOT IN (value1,value2,...)
```

```mysql
SELECT BookName,Writer,pDate
FROM bookinfo_zerobasis
WHERE pDate NOT IN ('2017年8月','2017年9月');
```

| BookName           | Writer   | pDate      |
| :----------------- | :------- | :--------- |
| 零基础学C#         | 明日科技 | 2017年10月 |
| 零基础学JavaScript | 明日科技 | 2017年10月 |
| 零基础学HTML5+CSS3 | 明日科技 | 2017年12月 |
| 零基础学Oracle     | 明日科技 | 2018年1月  |

### 使用NOT IN查询后两行数据

```mysql
SELECT order_id,order_sn,total_amount
FROM orderform
WHERE order_id NOT IN (SELECT order_id FROM (SELECT order_id FROM orderform ORDER BY order_id ASC LIMIT 8) tt);
```

| order\_id | order\_sn          | total\_amount |
| :-------- | :----------------- | :------------ |
| 835       | 201709271519568300 | 48.00         |
| 836       | 201709271605249732 | 18000.00      |

## 格式化结果集

### 格式化日期

#### 在SQL Server中格式化日期

```mysql
CONVERT (data_type[(length)], expression [, style]
```

* data_type: 规定目标

* length: 数据类型的可选参数

* expression: 需要转换的值

* style：可选参数

#### 在MySQL数据库中格式化日期

```mysql
DATA_FORMAT(data,format)
```

```mysql
SELECT user_id,email,DATA_FORMAT(reg_time, '%Y/%m/%d') AS reg_time
FROM shop.users LIMIT 6;
```

#### 在Oracle中格式化日期

```mysql
TO_CHAR(expression,format)
```

```mysql
SELECT user_id,birthday AS 出生日期
TO_CHAR(birthday,'YYYY-MM-DD') AS 格式化出生日期
FROM users
WHERE ROWNUM <= 6;
```

### 数据表的数值类型转换

#### SQL Server: CAST()函数

```mysql
CAST(express AS data_type)
```

```mysql
SELECT CAST('236' AS int);
SELECT CAST('236.424' AS int);
```

#### Oracle: CAST()函数

```mysql
CAST(express AS data_type)
```

```mysql
SELECT CAST('236' AS int) FROM DUAL;
```

### 去掉空格

```mysql
LTRIM(character_express)
```

```mysql
SELECT LTRIM('  MR') AS '去掉左空格', LTRIM('  BOOK') AS '去掉有右空格  ';
```

## 模糊查询

### LIKE谓词

| 通配符 | 说明                                                         |
| ------ | ------------------------------------------------------------ |
| %      | 由零个或多个字符组成的任意字符串                             |
| _      | 任意字符串                                                   |
| []     | 表示指定范围，例如[A-F]，表示A到F范围内的任意字符串          |
| [^]    | 表示任意范围之外的，例如   [^ A-F]，表示A到F范围之外的任意字符串 |

### "%"通配符的使用

```mysql
SELECT goods_id,goods_name,shop_price
FROM goods
WHERE goods_name LIKE '%华为%';
```

| goods\_id | goods\_name           | shop\_price |
| :-------- | :-------------------- | :---------- |
| 39        | 华为 M2 10.0 平板电脑 | 2288.00     |
| 41        | 华为 M2 8英寸平板电脑 | 1588.00     |
| 51        | 华为 Mate 8 64GB      | 3699.00     |

### "_"通配符的使用

```mysql
SELECT address_id,LTRIM(consignee) AS consignee, address
FROM user_address
WHERE LTRIM(consignee) LIKE '_ _ _';
```

| address\_id | consignee | address                  |
| :---------- | :-------- | :----------------------- |
| 8           | 张乐乐    | 北京西城西四环中路130号  |
| 11          | 陈家洛    | 上海静安寺南京西路1618号 |
| 13          | 令狐冲    | 深圳市中福华三路36号     |
| 16          | 石中玉    | 北京东城崇文门外大街40号 |

### "[]"通配符使用

并不是所有DBMS都支持[]创建的集合，只有SQL Server 和 Access

```mysql
SELECT order_id,order_sn,total_amount
FROM orderform
WHERE order_sn LIKE '%[6-9]';
```

### "[^]"通配符使用

```mysql
SELECT TOP 6 id,name,cat_name
FROM brand
WHERE name LIKE '[^A-Z]';
```

### 使用ESCAPE定义转义字符

使用通配符时，数据中也可能包含通配符，则需要进行转义

```mysql
WHERE 列名 LIKE '%10#%' ESCAPE '#'
```

```mysql
SELECT user_id,email
FROM users
WHERE email LIKE '%/_%' ESCAPE '/';
```

| user\_id | email           |
| :------- | :-------------- |
| 23       | \_xor@163.com   |
| 40       | ab\_cd@sina.com |

## 行数据过滤

### 行查询

#### 查询指定行

```mysql
SELECT goods_id,goods_name,shop_price FROM (SELECT TOP 6 * FROM goods) aa
WHERE NOT EXISTS (SELECT * FROM (SELECT TOP 5 * FROM goods) bb WHERE aa.goods_id = bb.goods_id);
```

#### 随机查询一行数据

##### 在SQL Server中随机查询一行数据

```mysql
SELECT TOP 1 goods_id,cat_id,goods_name
FROM goods ORDER BY NEWID();
```

##### 在MySQL 中随机查询一行数据

```mysql
SELECT goods_id,cat_id,goods_name
FROM goods
ORDER BY RAND() LIMIT 1;
```

| goods\_id | cat\_id | goods\_name           |
| :-------- | :------ | :-------------------- |
| 39        | 191     | 华为 M2 10.0 平板电脑 |

##### 在Oracle中随机查询一行数据

```mysql
SELECT "goods_id","cat_id","goods_name" FROM (
SELECT "goods_id","cat_id","goods_name" FROM "goods" ORDER BY DBMS_RANDOM.VALUE()) 
WHERE ROWNUM=1;
```

#### 在结果集中添加行号

```mysql
SELECT (SELECT COUNT(order_id) FROM orderform A
WHERE A.order_id>=B.order_id) 编号,order_id,order_sn,total_amount
FROM orderform B ORDER BY 1;
```

| 编号 | order\_id | order\_sn          | total\_amount |
| :--- | :-------- | :----------------- | :------------ |
| 1    | 836       | 201709271605249732 | 18000.00      |
| 2    | 835       | 201709271519568300 | 48.00         |
| 3    | 833       | 201706201658329117 | 1999.00       |
| 4    | 832       | 201706191721157693 | 96.00         |
| 5    | 831       | 201706191706011466 | 96.00         |
| 6    | 830       | 201706191623342538 | 48.00         |
| 7    | 829       | 201706090849505219 | 1399.00       |
| 8    | 828       | 201706090846205237 | 1999.00       |
| 9    | 827       | 201706081732158111 | 2000.00       |
| 10   | 826       | 201706081729418431 | 399.00        |

#### 查询隔行数据

SQL Server

```mysql
SELECT 编号,ISBN,BookName,Writer FROM (
SELECT ROW_NUMBER() OVER(ORDER BY ISBN) 编号,ISBN,BookName,Writer
FROM bookinfo_zerobasis) a WHERE a.编号%2=1;
```

Oracle

```
SELECT 编号,ISBN,BookName,Writer FROM (
SELECT ROW_NUMBER() OVER(ORDER BY ISBN) 编号,ISBN,BookName,Writer
FROM bookinfo_zerobasis) a WHERE MOD(a.编号%2)=1;
```

MySQL不支持ROW_NUMBER() OVER() 函数

#### 查询指定范围内所有行数据

```mysql
SELECT 编号,ISBN,BookName,Writer FROM (
SELECT ROW_NUMBER() OVER(ORDER BY ISBN) 编号,ISBN,BookName,Writer
FROM bookinfo_zerobasis) a WHERE a.编号 BETWEEN 3 AND 6;
```

| 编号 | ISBN          | BookName           | Writer   |
| :--- | :------------ | :----------------- | :------- |
| 3    | 7-110-12073-3 | 零基础学C语言      | 明日科技 |
| 4    | 7-110-12073-4 | 零基础学PHP        | 明日科技 |
| 5    | 7-110-12073-5 | 零基础学C#         | 明日科技 |
| 6    | 7-110-12073-6 | 零基础学JavaScript | 明日科技 |

### 空值（NULL）判断

#### 查询空值（IS NULL）

```MYSQL
SELECT user_id, email, nickname
FROM users
WHERE nickname IS NULL;
```

| user\_id | email           | nickname |
| :------- | :-------------- | :------- |
| 2        | vip@dsads.com   | NULL     |
| 5        | zuanshi@qqh.com | NULL     |

#### 查询非空值（IS NOT NULL）

```mysql
SELECT user_id, email, nickname
FROM users
WHERE nickname IS NOT NULL;
```

| user\_id | email            | nickname   |
| :------- | :--------------- | :--------- |
| 1        | 240874144@qq.com | Andy       |
| 13       | abc@sohu.com     | 支付宝用户 |
| 14       | 3665696@qq.com   | 支付宝用户 |
| 17       | 569696326@qq.com | 微信用户   |
| 23       | \_xor@163.com    | QQ用户     |
| 40       | ab\_cd@sina.com  | QQ用户     |

#### 对空值进行处理

```mysql
SELECT BookName,Writer, IFNULL(newbook, 0) AS newbook
FROM bookinfo_zerobasis;
```

| BookName           | Writer   | newbook |
| :----------------- | :------- | :------ |
| 零基础学Java       | 明日科技 | 1       |
| 零基础学Android    | 明日科技 | 1       |
| 零基础学C语言      | 明日科技 | 1       |
| 零基础学PHP        | 明日科技 | 1       |
| 零基础学C#         | 明日科技 | 1       |
| 零基础学JavaScript | 明日科技 | 1       |
| 零基础学HTML5+CSS3 | 明日科技 | 0       |
| 零基础学Oracle     | 明日科技 | 0       |

```mysql
SELECT BookName,Writer, ISNULL(newbook) AS newbook
FROM bookinfo_zerobasis;
```

| BookName           | Writer   | newbook |
| :----------------- | :------- | :------ |
| 零基础学Java       | 明日科技 | 0       |
| 零基础学Android    | 明日科技 | 0       |
| 零基础学C语言      | 明日科技 | 0       |
| 零基础学PHP        | 明日科技 | 0       |
| 零基础学C#         | 明日科技 | 0       |
| 零基础学JavaScript | 明日科技 | 0       |
| 零基础学HTML5+CSS3 | 明日科技 | 1       |
| 零基础学Oracle     | 明日科技 | 1       |

## 数据排序

### 数值排序

#### 按升序和降序排列

```sql
ORDER BY { order_by_expression [ ASC | DESC ]} [ ,...n ]
```

```SQL
SELECT goods_id, goods_name, sales_sum
FROM goods
ORDER BY sales_sum ASC ;
```

| goods\_id | goods\_name               | sales\_sum |
| :-------- | :------------------------ | :--------- |
| 39        | 华为 M2 10.0 平板电脑     | 0          |
| 41        | 华为 M2 8英寸平板电脑     | 0          |
| 49        | 荣耀畅玩5X 智能手机       | 0          |
| 51        | 华为 Mate 8 64GB          | 0          |
| 106       | 海尔 BCD-572WDPM电冰箱    | 0          |
| 109       | 三星 BCD-535WKZM电冰箱    | 0          |
| 114       | 索尼 D7200单反相机        | 0          |
| 56        | 三星55M5 智能液晶电视     | 1          |
| 58        | 海信 LED55EC290N 液晶电视 | 1          |
| 57        | TCL D50A710 液晶电视      | 5          |

#### 按照列别名排序

```sql
SELECT goods_id 商品编号,goods_name 商品名称,sales_sum 商品销量
FROM goods
ORDER BY 商品销量 DESC;
```

| 商品编号 | 商品名称                  | 商品销量 |
| :------- | :------------------------ | :------- |
| 57       | TCL D50A710 液晶电视      | 5        |
| 56       | 三星55M5 智能液晶电视     | 1        |
| 58       | 海信 LED55EC290N 液晶电视 | 1        |
| 39       | 华为 M2 10.0 平板电脑     | 0        |
| 41       | 华为 M2 8英寸平板电脑     | 0        |
| 49       | 荣耀畅玩5X 智能手机       | 0        |
| 51       | 华为 Mate 8 64GB          | 0        |
| 106      | 海尔 BCD-572WDPM电冰箱    | 0        |
| 109      | 三星 BCD-535WKZM电冰箱    | 0        |
| 114      | 索尼 D7200单反相机        | 0        |

```sql
SELECT goods_id 商品编号,goods_name 商品名称,sales_sum '商品  销量'
FROM goods
ORDER BY '商品  销量' DESC;
```

| 商品编号 | 商品名称                  | 商品  销量 |
| :------- | :------------------------ | :--------- |
| 39       | 华为 M2 10.0 平板电脑     | 0          |
| 41       | 华为 M2 8英寸平板电脑     | 0          |
| 49       | 荣耀畅玩5X 智能手机       | 0          |
| 51       | 华为 Mate 8 64GB          | 0          |
| 56       | 三星55M5 智能液晶电视     | 1          |
| 57       | TCL D50A710 液晶电视      | 5          |
| 58       | 海信 LED55EC290N 液晶电视 | 1          |
| 106      | 海尔 BCD-572WDPM电冰箱    | 0          |
| 109      | 三星 BCD-535WKZM电冰箱    | 0          |
| 114      | 索尼 D7200单反相机        | 0          |

#### 对多列排序

````sql
SELECT goods_id,goods_name,shop_price
FROM goods
ORDER BY shop_price,goods_name;
````

| goods\_id | goods\_name               | shop\_price |
| :-------- | :------------------------ | :---------- |
| 49        | 荣耀畅玩5X 智能手机       | 999.00      |
| 41        | 华为 M2 8英寸平板电脑     | 1588.00     |
| 39        | 华为 M2 10.0 平板电脑     | 2288.00     |
| 57        | TCL D50A710 液晶电视      | 2799.00     |
| 58        | 海信 LED55EC290N 液晶电视 | 3199.00     |
| 106       | 海尔 BCD-572WDPM电冰箱    | 3399.00     |
| 109       | 三星 BCD-535WKZM电冰箱    | 3499.00     |
| 51        | 华为 Mate 8 64GB          | 3699.00     |
| 114       | 索尼 D7200单反相机        | 3699.00     |
| 56        | 三星55M5 智能液晶电视     | 3799.00     |

```sql
SELECT goods_id,goods_name,shop_price
FROM goods
ORDER BY shop_price DESC ,goods_name DESC ;
```

| goods\_id | goods\_name               | shop\_price |
| :-------- | :------------------------ | :---------- |
| 56        | 三星55M5 智能液晶电视     | 3799.00     |
| 114       | 索尼 D7200单反相机        | 3699.00     |
| 51        | 华为 Mate 8 64GB          | 3699.00     |
| 109       | 三星 BCD-535WKZM电冰箱    | 3499.00     |
| 106       | 海尔 BCD-572WDPM电冰箱    | 3399.00     |
| 58        | 海信 LED55EC290N 液晶电视 | 3199.00     |
| 57        | TCL D50A710 液晶电视      | 2799.00     |
| 39        | 华为 M2 10.0 平板电脑     | 2288.00     |
| 41        | 华为 M2 8英寸平板电脑     | 1588.00     |
| 49        | 荣耀畅玩5X 智能手机       | 999.00      |

#### 对数据表中的指定行数进行排列

##### 在SQL Server 中对数据表中的前几行数据进行排序

```sql
SELECT TOP 3 goods_id,goods_name,shop_price
FROM goods
ORDER BY shop_price DESC;
```

##### 在SQL Server 中对数据表中的后几行数据进行排序

```sql
SELECT TOP 1 goods_id,goods_name,shop_price
FROM goods
ORDER BY shop_price;
```

##### 在Oracle 中对数据表中的前几行数据进行排序

```sql
SELECT * FROM
(SELECT "goods_id", "goods_name", "shop_price"
FROM "goods" 
ORDER BY "shop_price" DESC)
WHERE rownum <= 3;
```

##### 在Oracle 中对数据表中的后几行数据进行排序

```sql
SELECT "goods_id", "goods_name", "shop_price"
FROM (SELECT "goods_id", "goods_name", "shop_price" FROM "goods" ORDER BY "shop_price") 
WHERE rownum = 1;
```

### 汉字排序

#### 按姓氏拼音笔画排序

```sql
SELECT * FROM tb_name;
SELECT *
FROM tb_name
ORDER BY LEFT(name,1) COLLATE Chinese_PRC_Stroke_CS_AS_KS_WS DESC;
```

#### 按拼音进行排序

```sql
SELECT * FROM tb_name;
SELECT *
FROM tb_name
ORDER BY LEFT(name, 1) COLLATE Chinese_PRC_CS_AS_KS_WS DESC;
```

## 数据统计分析

### 聚合函数

| 聚合函数         | 支持的数据类型       | 功能描述                                             |
| ---------------- | -------------------- | ---------------------------------------------------- |
| SUM()            | 数字                 | 对特定列中的所有非空值求和                           |
| AVG()            | 数字                 | 对指定列中的所有非空值求平均数                       |
| MIN()            | 数字、字符、日期     | 返回指定列中的最小数字、最小的字符串和最早的日期时间 |
| MAX()            | 数字、字符、日期     | 返回指定列中的最大数字、最大的字符串和最近的日期时间 |
| COUNT([disinct]) | 任意基于行的数据类型 | 统计结果集中全部记录行的数量，最多可达2147483647行   |

### 求平均值

```sql
AVG([DISTINCT] expression)
```

#### AVG() 函数的普通用法

```sql
SELECT AVG(shop_price) AS 平均值
FROM goods;
```

| 平均值      |
| :---------- |
| 2896.800000 |

#### 使用WHERE 子句限制 AVG() 函数统计的行

```sql
SELECT AVG(shop_price) AS 平均值
FROM goods
WHERE shop_price > 3000;
```

| 平均值      |
| :---------- |
| 3549.000000 |

### 获取结果集数

```sql
COUNT( [DISTINCT] *|expression)
```

```sql
SELECT COUNT(*) AS 商品个数
FROM goods
WHERE (shop_price > 3000);
```

| 商品个数 |
| :------- |
| 6        |

```sql
SELECT COUNT(cat_id) AS 商品种类
FROM goods;
```

| 商品种类 |
| :------- |
| 10       |

### 最大值与最小值

```sql
MAX([DISTINCT]expression)
MIN([DISTINCT]expression)
```

```sql
SELECT CAST(AVG(shop_price) as real) AS 去掉最大值与最小值的平均值 FROM goods
WHERE goods_name LIKE '%液晶电视%'
AND shop_price NOT IN(
(SELECT MIN(shop_price) AS 最小值 FROM goods WHERE goods_name LIKE '%液晶电视%')
UNION
(SELECT max(shop_price) AS 最大值 FROM goods WHERE goods_name LIKE '%液晶电视%'));
```

| 去掉最大值与最小值的平均值 |
| :------------------------- |
| 3199                       |

### 对多列求和

```sql
SUM([DISTINCT]expression)
```

```sql
SELECT SUM(shop_price) AS 所有商品价格总和
FROM goods;
```

| 所有商品价格总和 |
| :--------------- |
| 28968.00         |

```sql
SELECT SUM(shop_price-cost_price) AS 所有商品的总盈利
FROM goods;
```

| 所有商品的总盈利 |
| :--------------- |
| 3426.00          |

```sql
SELECT SUM(DISTINCT shop_price) AS 所有商品价格总和
FROM goods;
```

| 所有商品价格总和 |
| :--------------- |
| 25269.00         |

### 在WHERE子句中使用聚合函数

```sql
SELECT user_id,email, birthday,total_amount
FROM users WHERE (birthday BETWEEN '1985-01-01' AND '1990-12-31')
AND (total_amount > (SELECT AVG(total_amount) FROM users));
```

| user\_id | email            | birthday            | total\_amount |
| :------- | :--------------- | :------------------ | :------------ |
| 1        | 240874144@qq.com | 1986-03-03 16:00:00 | 63136.04      |

### Oracle 数据库的NVL()函数在聚合函数中的使用

```sql
NVL(string, replace_with)
```

如果string为NULL，则NVL函数返回replace_with的值，否则返回string的值，如果两个参数都为NULL，则返回NULL

* 数字型：NVL(comm,0)
* 字符型：NVL(TO_CHAR(comm),'NO Commission')
* 日期型：NVL(hiredate,'28-DEC-14')

```sql
SELECT NVL("Type",'没有分类') 丛书系列名称,
COUNT(NVL("Type",'没有分类')) 系列中图书个数
FROM "bookinfo"
GROUP BY NVL("Type",'没有分类');
```

### 多个聚合函数的使用

#### 使用多个聚合函数的注意事项

1. 多个聚合函数在SQL Server中不能嵌套(Oracle可以)
2. 子查询不能作为一个聚合函数的表达式

#### 聚合函数的执行步骤

1. 首先生成一个中间表
2. 如果在SELECT语句中存在一个WHERE子句，就对中间表中每一行根据其搜索条件进行求值，清除那些求值结果为FALSE或NULL的行，保留那些求值结果为TRUE的行
3. 使用中间表的值来计算每个聚合函数的值
4. 将每个聚合函数统计的值作为结果表中的列值显示

##   分组统计

### 创建分组

#### 使用GROUP BY 子句创建分组

如果使用 GROUP BY 子句进行分组查询，SELECT 查询的列必须包含在 GROUP BY 子句中或者包含在聚合函数中

```mysql
SELECT cat_id, MIN(shop_price) 最低售价,
MAX(cost_price) 最高成本价 ,AVG(shop_price) 平均售价, COUNT(*) AS 个数
FROM goods
GROUP BY cat_id
ORDER BY MAX(cost_price) DESC;
```

| cat\_id | 最低售价 | 最高成本价 | 平均售价    | 个数 |
| :------ | :------- | :--------- | :---------- | :--- |
| 123     | 999.00   | 3500.00    | 2349.000000 | 2    |
| 130     | 2799.00  | 3500.00    | 3265.666667 | 3    |
| 131     | 3399.00  | 3299.00    | 3449.000000 | 2    |
| 104     | 3699.00  | 3100.00    | 3699.000000 | 1    |
| 191     | 1588.00  | 1500.00    | 1938.000000 | 2    |

#### 使用 GROUP BY 子句创建多列分组

```mysql
SELECT cat_id, brand_id, MIN(shop_price) 最低售价,
MAX(cost_price) 最高成本价 ,AVG(shop_price) 平均售价, COUNT(*) AS 个数
FROM goods
GROUP BY cat_id, brand_id
ORDER BY MAX(cost_price) DESC;
```

| cat\_id | brand\_id | 最低售价 | 最高成本价 | 平均售价    | 个数 |
| :------ | :-------- | :------- | :--------- | :---------- | :--- |
| 123     | 1         | 999.00   | 3500.00    | 2349.000000 | 2    |
| 130     | 15        | 3799.00  | 3500.00    | 3799.000000 | 1    |
| 131     | 15        | 3499.00  | 3299.00    | 3499.000000 | 1    |
| 131     | 14        | 3399.00  | 3155.00    | 3399.000000 | 1    |
| 104     | 4         | 3699.00  | 3100.00    | 3699.000000 | 1    |
| 130     | 19        | 3199.00  | 2988.00    | 3199.000000 | 1    |
| 130     | 6         | 2799.00  | 2400.00    | 2799.000000 | 1    |
| 191     | 1         | 1588.00  | 1500.00    | 1938.000000 | 2    |

#### 对表达式进行分组统计

```mysql
SELECT 收货地址, 联系方式
FROM (SELECT '收货人：'+ consignee + ' 的地址为: ' + address  AS 收货地址,
      '联系电话为：' + mobile  AS 联系方式 FROM user_address) a
GROUP BY 收货地址, 联系方式;
```

| 收货地址 | 联系方式    |
| :------- | :---------- |
| 0        | 13554754891 |
| 0        | 13554745866 |
| 0        | 13800138000 |
| 0        | 13012345678 |
| 0        | 13554754711 |
| 0        | 13554754132 |
| 0        | 18988888888 |

### 在统计中使用 ROLLUP 关键字和 CUBE 关键字

rollup 首先对abc进行分组 ab分组 a分组

cube 会对ab出现的每种可能性分组

```mysql
SELECT e.deptno, d.dname, SUM(e.sal) AS 工资总和
FROM emp e,dept d
WHERE e.deptno = d.deptno
GROUP BY ROLLUP(e.deptno, d.dname);
```

```mysql
SELECT e.deptno, d.dname, SUM(e.sal) AS 工资总和
FROM emp e,dept d
WHERE e.deptno = d.deptno
GROUP BY e.deptno, d.dname WITH ROLLUP;
```

```mysql
（1）
SELECT e.deptno, d.dname, SUM(e.sal) AS 工资总和
FROM emp e,dept d
WHERE e.deptno = d.deptno
GROUP BY CUBE(e.deptno, d.dname);

（2）
SELECT e.deptno, d.dname, SUM(e.sal) AS 工资总和
FROM emp e,dept d
WHERE e.deptno = d.deptno
GROUP BY e.deptno, d.dname WITH CUBE;
```

### GROUP BY 子句的 NULL 值处理

```mysql
SELECT oauth AS 第三方付款方式, COUNT(*)AS 个数
FROM users
GROUP BY oauth;
```

| 第三方付款方式 | 个数 |
| :------------- | :--- |
|                | 4    |
| alipay         | 2    |
| qq             | 2    |

### 使用HAVING 子句进行过滤分组

```mysql
SELECT cat_id, shop_price, COUNT(cat_id) AS 个数
FROM goods
WHERE (store_count < 1000)
GROUP BY cat_id,shop_price
HAVING (shop_price >
          (SELECT AVG(shop_price)
          FROM goods))
ORDER BY shop_price DESC;
```

| cat\_id | shop\_price | 个数 |
| :------ | :---------- | :--- |
| 130     | 3799.00     | 1    |
| 130     | 3199.00     | 1    |

### 对统计结果进行排序

```mysql
SELECT cat_id, COUNT(cat_id) AS 商品个数
FROM goods
GROUP BY cat_id
ORDER BY 商品个数 DESC;
```

| cat\_id | 商品个数 |
| :------ | :------- |
| 130     | 3        |
| 191     | 2        |
| 123     | 2        |
| 131     | 2        |
| 104     | 1        |

### GROUP BY 子句的特殊用法

```mysql
SELECT AVG(shop_price) AS 平均售价
FROM goods
GROUP BY cat_id;
```

| cat\_id | 商品个数 |
| :------ | :------- |
| 130     | 3        |
| 191     | 2        |
| 123     | 2        |
| 131     | 2        |
| 104     | 1        |

```mysql
SELECT "cat_id", ROUND(AVG("shop_price")) AS 平均售价
FROM "goods"
GROUP BY "cat_id"
ORDER BY AVG("shop_price");
```

### SELECT 子句的顺序

| 子句     | 说明               | 是否必须使用           |
| -------- | ------------------ | ---------------------- |
| SELECT   | 返回列或者表达式   | 是                     |
| FROM     | 从中要检索数据的表 | 是                     |
| WHERE    | 行级过滤           | 否                     |
| GROUP BY | 分组统计           | 仅在按组计算聚集时使用 |
| HAVING   | 组级过滤           | 否                     |
| ORDER BY | 对输出数据排序     | 否                     |

##  简单子查询

### 简单子查询

#### 子查询的语法

```mysql
(SELECT [ALL | DISTINCT]<select item list>
FROM <table list>
[WHERE<search condition>]
[GROUP BY <group item list>]
[HAVING <group by search condition>]]
)
```

#### 子查询常用的语法格式

1. 第一种语法格式

   ```mysql
   WHERE 查询表达式 [NOT] IN 子查询
   ```

2. 第二种语法格式

   ```mysql
   WHERE 查询表达式 比较运算符 [ ANY | ALL ](子查询)
   ```

3. 第三种语法格式

   ```mysql
   WHERE [NOT] EXISTS(子查询)
   ```


#### 子查询和其他SELECT语句之间的区别

除了子查询必须在括号中出现之外

1. SELECT语句只能使用那些来自FROM子句中的表和列，子查询不仅可以使用在该子查询中的FROM子句中的表，而且还可以使用子查询的FROM子句中表的任何列
2. SELECT语句中的子查询必须返回单一数据列。另外，根据其在查询中的使用方法（如将子查询结果用作包括子查询的SELECT子句中的一个数据项），包括子查询的查询可能要求子查询返回单个值（而不是来自单列的多个值）
3. 子查询不能有ORDER BY子句
4. 子查询必须由一个SELECT语句组成，也就不是将多个SQL语句用UNION组合起来作为一个子查询

### SELECT 列表中的子查询

```mysql
SELECT tb_book_author,tb_author_department, 
   (SELECT max(book_price)
      FROM tb_book 
      WHERE tb_book_author.tb_book_author=tb_book.tb_book_author) 
FROM tb_book_author;
```

| tb\_book\_author | tb\_author\_department | \(SELECT max\(book\_price\)<br/>      FROM tb\_book<br/>      WHERE tb\_book\_author.tb\_book\_author=tb\_book.tb\_book\_author\) |
| :--------------- | :--------------------- | :----------------------------------------------------------- |
| 潘一             | PHP                    | 89.0000                                                      |
| 刘一             | PHP                    | 78.0000                                                      |
| 郭一             | VC                     | 89.0000                                                      |
| 王一             | VC                     | 79.0000                                                      |

### 多列子查询

#### 成对比较的多列子查询

```oracle
SELECT ename, job, sal
FROM emp
WHERE (sal, job) IN (SELECT MAX(ssal), job FROM emp GROUP BY job)
```

### 非成对比较的多列子查询

```mysql
SELECT ename, job, sal 
FROM emp
WHERE sal in(SELECT MAX(sal) FROM emp GROUP BY job)
AND
job IN(SELECT  distinct job FROM emp);
```

### 比较子查询

#### 使用比较运算符连接子查询

```mysql
SELECT cat_id,goods_name 
FROM goods
WHERE cat_id>(
    SELECT cat_id
    FROM brand
    WHERE name='蓝月亮'
);
```

| cat\_id | goods\_name               |
| :------ | :------------------------ |
| 191     | 华为 M2 10.0 平板电脑     |
| 191     | 华为 M2 8英寸平板电脑     |
| 123     | 荣耀畅玩5X 智能手机       |
| 123     | 华为 Mate 8 64GB          |
| 130     | 三星55M5 智能液晶电视     |
| 130     | TCL D50A710 液晶电视      |
| 130     | 海信 LED55EC290N 液晶电视 |
| 131     | 海尔 BCD-572WDPM电冰箱    |
| 131     | 三星 BCD-535WKZM电冰箱    |
| 104     | 索尼 D7200单反相机        |

#### 子查询易错点

1. 子查询不能返回多个值
2. 子查询不能返回包含ORDER BY子句

### 在子查询中使用聚合函数

```mysql
SELECT ename,sal,job
FROM emp
WHERE sal > (SELECT AVG(sal)FROM emp);
```

## 多行子查询

### 使用 IN、NOT IN 操作符的多行子查询

#### 使用 IN 子查询实现交集运算

```mysql
SELECT * 
FROM tb_book
WHERE book_sort IN (
    SELECT tb_author_department 
    FROM tb_book_author
    WHERE tb_book.book_sort=tb_book_author.tb_author_department)
ORDER BY tb_book.book_price;
```

#### 使用 NOT IN 子查询实现差集运算

```mysql
SELECT * 
FROM tb_book
WHERE book_sort NOT IN (
    SELECT tb_author_department 
    FROM tb_book_author);
```

### EXISTS 子查询与 NOT EXISTS 子查询

#### EXISTS 子查询实现两个表的交集

```mysql
SELECT * 
FROM tb_book
WHERE EXISTS (
    SELECT tb_author_department 
    FROM tb_book_author
    WHERE tb_book.book_sort=tb_book_author.tb_author_department)
ORDER BY tb_book.book_price;
```

#### NOT EXISTS 子查询实现两个表的差集

```mysql
SELECT * 
FROM tb_book
WHERE NOT EXISTS(
    SELECT tb_author_department
    FROM tb_book_author
    WHERE tb_book.book_sort=tb_book_author.tb_author_department );
```

### 通过量词实现多行子查询

#### 使用量词实现多行子查询

```mysql
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE shop_price < all(
    SELECT AVG(shop_price)
    FROM goods
    GROUP BY cat_id);

```

#### 使用 ALL 操作符的多行子查询

```mysql
SELECT cat_id,goods_name,shop_price
FROM goods
WHERE shop_price > ANY(
    SELECT AVG(shop_price)
    FROM goods
    GROUP BY cat_id);
```

