---
title: "《SQL即查即用》"
date: 2020-11-05T16:28:34+08:00
draft: false
tags: ["SQL"]                       
---

![sql](/img/sql.jpg)

配套资料：https://github.com/chuchinc/kl-book-resource

# 1.简单查询

## 1.1 SELECT语句基本结构

```mysql
SELECT select_list
[ INTO new_table ]
FROM table_name
[ WHERE search_condition ]
[ GROUP BY group_by_condition ]
[ HAVING search_condition ]
[ ORDER BY order_expression [ ASC | DESC ]];
```

## 1.2 单列查询

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

## 1.3 多列查询

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

## 1.4 查询所有列

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

## 1.5 别名的应用

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

## 1.6 删除重复数据

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

## 1.7 限制查询结果

### 1.7.1 在SQL Server数据库中限制查询结果

```mysql
SELECT TOP n FROM table;
```

### 1.7.2 在MySQL数据库中限制查询结果

1. 限制查询前n条数据

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

2. 限制查询n条数据

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

### 1.7.3 在Oracle数据库中限制查询结果

```mysql
#查看前5条数据
SELECT goods_name,market_price FROM goods WHERE ROWNUM <= 5;
```

