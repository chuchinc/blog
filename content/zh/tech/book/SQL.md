---
title: "《SQL即查即用》"
date: 2019-11-05T16:28:34+08:00
draft: false
tags: ["SQL"]                       
---

![sql](/img/sql.jpg)

京东入手的一本工具书，主要介绍SQL知识，比较全，一天一章，边看边记录，配套资料：https://github.com/chuchinc/kl-book-resource

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