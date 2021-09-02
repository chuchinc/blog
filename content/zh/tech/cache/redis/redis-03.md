---
title: "Redis 客户端访问"
date: 2020-03-03T10:21:43+08:00
draft: false
tags: ["Redis"]
---

## Java 程序访问 Redis

采用jedis API进行访问即可

1. 关闭RedisServer端的防火墙

   ```shell
   systemctl stop firewalld(默认)
   systemctl disable firewalld.service(设置开启不启动)
   ```

2. 新建maven项目后导入Jedis包

   ```xml
   <dependency>
         <groupId>redis.clients</groupId>
         <artifactId>jedis</artifactId>
         <version>2.9.0</version>
   </dependency>
   ```

3. 测试

   ```java
   public class JedisTest {
   
       public static void main(String[] args) {
           Jedis jedis = new Jedis("127.0.0.1", 6379);
           jedis.set("jedis:name:1","cn-chuchin");
           System.out.println(jedis.get("jedis:name:1"));
           jedis.lpush("jedis:list:1", "1", "2", "3", "4", "5");
           System.out.println(jedis.llen("jedis:list:1"));
       }
   
   }
   ```

## Spring 访问 Redis

1. 新建项目，添加依赖

   ```xml
   <dependencies>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-beans</artifactId>
         <version>5.2.5.RELEASE</version>
       </dependency>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-core</artifactId>
         <version>5.2.5.RELEASE</version>
       </dependency>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-context</artifactId>
         <version>5.2.5.RELEASE</version>
       </dependency>
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-test</artifactId>
         <version>5.2.5.RELEASE</version>
       </dependency>
       <dependency>
         <groupId>junit</groupId>
         <artifactId>junit</artifactId>
         <version>4.12</version>
         <scope>test</scope>
       </dependency>
     </dependencies>
   ```

   redis依赖

   ```xml
   <dependency>
       <groupId>org.springframework.data</groupId>
       <artifactId>spring-data-redis</artifactId>
       <version>1.0.3.RELEASE</version>
   </dependency>
   ```

2. 添加Spring配置文件

   redis.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans.xsd">
     <bean id="propertyConfigurer"
       class="org.springframework.beans.factory.config.PropertyPlaceholderConfigur
   er">
       <property name="locations">
         <list>
           <value>classpath:redis.properties</value>
         </list>
       </property>
     </bean>
     <!-- redis config -->
     <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
       <property name="maxActive" value="${redis.pool.maxActive}"/>
       <property name="maxIdle" value="${redis.pool.maxIdle}"/>
       <property name="maxWait" value="${redis.pool.maxWait}"/>
       <property name="testOnBorrow" value="${redis.pool.testOnBorrow}"/>
     </bean>
     <bean id="jedisConnectionFactory"
       class="org.springframework.data.redis.connection.jedis.JedisConnectionFactor
   y">
       <property name="hostName" value="${redis.server}"/>
       <property name="port" value="${redis.port}"/>
       <property name="timeout" value="${redis.timeout}"/>
       <property name="poolConfig" ref="jedisPoolConfig"/>
     </bean>
     <bean id="redisTemplate"
       class="org.springframework.data.redis.core.RedisTemplate">
       <property name="connectionFactory" ref="jedisConnectionFactory"/>
       <property name="KeySerializer">
         <bean
           class="org.springframework.data.redis.serializer.StringRedisSerializer">
         </bean>
       </property>
       <property name="ValueSerializer">
       <bean
         class="org.springframework.data.redis.serializer.StringRedisSerializer">
       </bean>
   
       /property>
     </bean>
   </beans>
   ```

3. 添加properties文件

   redis.properties

   ```properties
   redis.pool.maxActive=100
   redis.pool.maxIdle=50
   redis.pool.maxWait=1000
   redis.pool.testOnBorrow=true
   redis.timeout=50000
   redis.server=192.168.72.128
   redis.port=6379
   ```

4. 编写测试用例

   ```java
   import org.junit.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.data.redis.core.RedisTemplate;
   import org.springframework.test.context.ContextConfiguration;
   import
   org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
   import java.io.Serializable;
   @ContextConfiguration({ "classpath:redis.xml" })
   public class RedisTest extends AbstractJUnit4SpringContextTests {
       @Autowired
       private RedisTemplate<Serializable, Serializable> rt;
   @Test
       public void testConn() {
           rt.opsForValue().set("name","zhangfei");
           System.out.println(rt.opsForValue().get("name"));
   } }
   ```

## Spring Boot 访问 Redis

1. 新建Spring Boot 项目，勾选spring web，添加依赖

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
   ```

2. 添加配置文件 application.yml

   ```yaml
   spring:
     redis:
       host: 192.168.72.128
       port: 6379
       jedis:
         pool:
           min-idle: 0
           max-idle: 8
           max-active: 80
           max-wait: 30000
           timeout: 3000
   ```

3. 添加配置类 RedisConfig

   ```java
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.data.redis.connection.RedisConnectionFactory;
   import org.springframework.data.redis.core.RedisTemplate;
   import org.springframework.data.redis.serializer.StringRedisSerializer;
   @Configuration
   public class RedisConfig {
       @Autowired
       private RedisConnectionFactory factory;
   @Bean
       public RedisTemplate<String, Object> redisTemplate() {
           RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
           redisTemplate.setKeySerializer(new StringRedisSerializer());
           redisTemplate.setHashKeySerializer(new StringRedisSerializer());
           redisTemplate.setHashValueSerializer(new StringRedisSerializer());
           redisTemplate.setValueSerializer(new StringRedisSerializer());
           redisTemplate.setConnectionFactory(factory);
           return redisTemplate;
   } }
   ```
   
4. 添加 RedisController

   ```java
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.data.redis.core.RedisTemplate;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RequestParam;
   import org.springframework.web.bind.annotation.RestController;
   import java.util.concurrent.TimeUnit;
   @RestController
   @RequestMapping(value = "/redis")
   public class RedisController {
   @Autowired
       RedisTemplate redisTemplate;
       @GetMapping("/put")
       public String put(@RequestParam(required = true) String key,
   @RequestParam(required = true) String value) { //设置过期时间为20秒
           redisTemplate.opsForValue().set(key,value,20, TimeUnit.SECONDS);
           return "Success";
       }
       @GetMapping("/get")
       public String get(@RequestParam(required = true) String key){
           return (String) redisTemplate.opsForValue().get(key);
       }
   }
   ```
   
5. 修改Application并运行

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cache.annotation.EnableCaching;
   @SpringBootApplication
   @EnableCaching
   public class SpringbootRedisApplication {
       public static void main(String[] args) {
           SpringApplication.run(SpringbootRedisApplication.class, args);
   } }
   ```
   

   
