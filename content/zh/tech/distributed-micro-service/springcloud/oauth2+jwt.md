---
title: "统一认证 OAuth2+JWT"
date: 2020-10-14T10:21:43+08:00
draft: false
tags: ["Java","Spring Cloud","OAuth2","JWT"]
---

*本文源代码下载：[spring-cloud-oauth2+jwt.zip](/file/springcloud/spring-cloud-oauth2+jwt.zip)*

认证：验证用户的合法身份，比如输入用户名和密码，系统会在后台验证⽤户名和密码是否合法， 合法的前提下，才能够进行后续的操作，访问受保护的资源

## 微服务架构下统一认证场景

分布式系统的每个服务都会有认证需求，如果每个服务都实现⼀套认证逻辑会非常冗余，考虑分布式系统共享性的特点，需要由独⽴的认证服务处理系统认证的请求。

## 微服务架构下统一认证思路

* 基于Session的认证方式

  在分布式的环境下，基于session的认证会出现⼀个问题，每个应⽤服务都需要在session中存储用户身份信息，通过负载均衡将本地的请求分配到另⼀个应用服务需要将session信息带过去，否则会重新认证。我们可以使用Session共享、Session黏贴等⽅案。

* 基于token的认证方式

  基于token的认证⽅式，服务端不用存储认证数据，易维护扩展性强， 客户端可以把token存在任意地⽅，并且可以实现web和app统⼀认证机制。其缺点也很明显，token由于⾃包含信息，因此⼀般数据量较⼤，⽽且每次请求都需要传递，因此比较占带宽。另外，token的签名验签操作也会 给cpu带来额外的处理负担。

## OAuth2 开放授权协议/标准

### OAuth2 介绍

OAuth（开放授权）是⼀个开放协议/标准，允许⽤户授权第三方应用访问他们存储在另外的服务提供者上的信息，而不需要将⽤户名和密码提供给第三⽅应用或分享他们数据的所有内容。

**允许用户授权第三⽅应用访问他们存储在另外的服务提供者上的信息，而不需要将用户名和密码提供给第三⽅应用或分享他们数据的所有内容**

结合“使用QQ登录第三方网站”这个场景拆分理解上述那句话

用户：我们自己

第三方应用：网站

另外的服务提供者：QQ

OAuth2是OAuth协议的延续版本，但不向后兼容OAuth1即完全废除OAuth1

### OAuth2 协议角色和流程

网站要开发使用QQ登录这个功能的话，那么是需要提前到QQ平台进行登记的

1. 网站-->登记-->QQ平台
2. QQ 平台会颁发⼀些参数给网站，后续上线进行授权登录的时候（刚才打开授权⻚⾯）需要携带这些参数

client_id：客户端id （QQ最终相当于⼀个认证授权服务器，网站就相当于⼀个客户端了，所以会给⼀个客户端id），相当于账号

secret：相当于密码

* 资源所有者（Resource Owner）：可以理解为用户自己
* 客户端（Client）：我们想登陆的网站或应⽤，比如拉勾网
* 认证服务器（Authorization Server）：可以理解为微信或者QQ
* 资源服务器（Resource Server）：可以理解为微信或者QQ

### 什么情况下需要使用OAuth2

**第三⽅授权登录的场景**：比如，我们经常登录⼀些⽹站或者应⽤的时候，可以选择使⽤第三⽅授权登录 的⽅式，比如：微信授权登录、QQ授权登录、微博授权登录等，这是典型的 OAuth2 使⽤场景。

**单点登录的场景**：如果项目中有很多微服务或者公司内部有很多服务，可以专门做⼀个认证中心（充当 认证平台⻆⾊），所有的服务都要到这个认证中心做认证，只做⼀次登录，就可以在多个授权范围内的 服务中⾃由串⾏。

### OAuth2的颁发Token授权方式

1. **授权码（authorization-code）**
2. **密码式（password）提供用户名+密码换取token**
3. **隐藏式（implicit）**
4. **客户端凭证（client credentials）**

授权码模式使⽤到了回调地址，是最复杂的授权方式，微博、微信、QQ等第三⽅登录就是这种模式。 这里重点介绍接口对接中常使用的password密码模式（提供用户名+密码换取token）

## Spring Cloud OAuth2 + JWT 实现

### Spring Cloud OAuth2 介绍

Spring Cloud OAuth2 是 Spring Cloud 体系对OAuth2协议的实现，可以用来做多个微服务的统⼀认证 （验证身份合法性）授权（验证权限）。通过向OAuth2服务（统⼀认证授权服务）发送某个类型的 grant_type进行集中认证和授权，从而获得access_token（访问令牌），而这个token是受其他微服务信任的。

**注意：使用OAuth2解决问题的本质是，引⼊了⼀个认证授权层，认证授权层连接了资源的拥有者，在授权层⾥⾯，资源的拥有者可以给第三⽅应⽤授权去访问我们的某些受保护资源。**

### Spring Cloud OAuth2 构建微服务统一认证服务思路

在我们统⼀认证的场景中，Resource Server其实就是我们的各种受保护的微服务，微服务中的各种API访问接口就是资源，发起http请求的浏览器就是Client客户端（对应为第三⽅应⽤）

### 搭建认证服务器（Authorization Server）

认证服务器（Authorization Server），负责颁发token

1. 新建子模块 cloud-oauth-server-9999

```xml
<dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-oauth2</artifactId>
      <exclusions>
        <exclusion>
          <groupId>org.springframework.security.oauth.boot</groupId>
          <artifactId>spring-security-oauth2-autoconfigure</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
    <dependency>
      <groupId>org.springframework.security.oauth.boot</groupId>
      <artifactId>spring-security-oauth2-autoconfigure</artifactId>
      <version>2.1.11.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.security.oauth</groupId>
      <artifactId>spring-security-oauth2</artifactId>
      <version>2.3.4.RELEASE</version>
    </dependency>
</dependencies>
```

2. 配置

```yaml
server:
  port: 9999
eureka:
  client:
    service-url:
      defaultZone: http://a.eureka.server:8761/eureka/,http://b.eureka.server:8762/eureka/
  instance:
    #使⽤ip注册，否则会使⽤主机名注册了（此处考虑到对⽼版本的兼容，新版本经过实验都是ip）
    prefer-ip-address: true
    #⾃定义实例显示格式，加上版本号，便于多版本管理，注意是ip-address，早期版本是ipAddress
    instance-id: ${spring.cloud.client.ipaddress}:${spring.application.name}:${server.port}:@project.version@
spring:
  application:
    name: cloud-oauth-server
```

3. 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class CloudOauthServer9999 {

    public static void main(String[] args) {
        SpringApplication.run(CloudOauthServer9999.class, args);
    }

}
```

4. 认证服务器配置类

```java
/**
 * 当前类为Oauth2 Server的配置类（需要继承特定的父类）
 */
@Configuration
@EnableAuthorizationServer //开启认证服务器功能
public class OauthServerConfigurer extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 认证服务器最终是以api接口的方式对外提供服务（检验合法性并生成令牌、校验令牌等）
     * 那么，以api接口方式对外的话，就涉及到接口的访问权限，为此在此进行必要的配置
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        super.configure(security);
        security  //允许客户端表单认证
                .allowFormAuthenticationForClients()
                //开启端口/oauth/token_key的访问权限（允许）
                .tokenKeyAccess("permitAll")
                //开启端口/oauth/check_token的访问权限（允许）
                .checkTokenAccess("permitAll");
    }

    /**
     * 客户端详情配置
     * 比如client_id, secret
     * 当前这个服务就如同QQ平台，网站作为客户端需要QQ平台进行登录授权认证等，提前需要到QQ平台注册
     * QQ平台会给网站
     * 颁发client_id等必要参数，说明客户端是谁
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);
        //客户端信息存储到什么地方，可以在内存中，可以在数据库里
        clients.inMemory()
                //添加一个client配置，指定其client_id
                .withClient("client_edm")
                //指定客户端的密码/安全码
                .secret("abcxyz")
                //指定客户端所能访问资源id清单，此处的资源id是需要在具体的资源服务器上配置也一样
                .resourceIds("autodeliver")
                //认证类型/令牌颁发模式，可以配置多个在这里，但不一定都用，
                // 具体使用哪种方式颁发token，需要客户端调用的使用传递参数指定
                .authorizedGrantTypes("password", "refresh_token")
                //客户端权限范围，此处配置为all即可
                .scopes("all");
    }

    /**
     * 配置token令牌管理相关，当下的token需要在服务器端存储
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);
        //指定token存储方式
        endpoints.tokenStore(getTokenStore())
                //token服务的一个描述，可以认为是token生成细节的描述，比如有效时间多少等
                .tokenServices(getAuthorizationServerTokenServices())
                //指定认证器，随后注入一个到当前类使用即可
                .authenticationManager(authenticationManager)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST);
    }

    /**
     * 创建tokenStore对象
     * @return
     */
    public TokenStore getTokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * 该方法用户获取一个token服务对象（该对象描述了一个token有效期等信息）
     * @return
     */
    public AuthorizationServerTokenServices getAuthorizationServerTokenServices() {
        //使用默认实现
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        //是否开启令牌刷新
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setTokenStore(getTokenStore());
        //设置令牌有效时间（一般设置为20小时）
        //access_token就是我们请求资源需要携带的令牌
        defaultTokenServices.setAccessTokenValiditySeconds(20);
        //3天
        defaultTokenServices.setRefreshTokenValiditySeconds(259200);
        return defaultTokenServices;
    }
}
```

* 关于三个configure方法

  * **configure(ClientDetailServiceConfigurer clients)**

    用来配置客户端详情服务（ClientDetailService），客户端详情信息在 这⾥进⾏初始化，你 能够把客户端详情信息写死在这⾥或者是通过数据库来存储调取详情信息

  * **configure(AuthorizationServerEndpointsConfigurer endpoints)**

    ⽤来配置令牌（token）的访问端点和令牌服务(token services)

  * **configure(AuthorizationServerSecurityConfigurer oauthServer)**

    ⽤来配置令牌端点的安全约束.

* 关于TokenStore

  * InMemoryTokenStore

    默认采⽤，它可以完美的⼯作在单服务器上（即访问并发量 压⼒不⼤的情况下，并且它 在失败的时候不会进⾏备份），⼤多数的项⽬都可以使⽤这个版本的实现来进⾏ 尝试， 你可以在开发的时候使⽤它来进⾏管理，因为不会被保存到磁盘中，所以更易于调试。

  * JdbcTokenStore

    这是⼀个基于JDBC的实现版本，令牌会被保存进关系型数据库。使⽤这个版本的实现 时， 你可以在不同的服务器之间共享令牌信息，使⽤这个版本的时候请注意把"springjdbc"这个依赖加⼊到你的 classpath当中。

  * JwtTokenStore

    这个版本的全称是 JSON Web Token（JWT），它可以把令牌相关的数 据进⾏编码（因此对于后端服务来说，它不需要进⾏存储，这将是⼀个重⼤优势），缺 点就是这个令牌占⽤的空间会⽐较⼤，如果你加⼊了⽐较多⽤户凭证信息， JwtTokenStore 不会保存任何数据。

5. 认证服务器安全配置类

```java
@Configuration
public class SecurityConfigure extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 注册一个认证管理器对象到容器
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 处理用户名和密码验证事宜
     * 1. 客户端传递username和password 参数到认证服务器
     * 2. 一般来说username 和password 会存储在数据库中的用户表
     * 3. 根据用户表中数据，验证当前传递过来的用户信息的合法性
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //在这个方法中就可以去关联数据库了，当前先把用户信息配置在内存中
        //实例化一个用户对象（相当于数据表中的一条用户记录）
        UserDetails user = new User("admin","123456",new ArrayList<>());
        auth.inMemoryAuthentication()
                .withUser(user)
                .passwordEncoder(passwordEncoder);
    }
}
```

6. 请求测试

```xml
#获取token
GET http://localhost:9999/oauth/token?client_secret=abcxyz&grant_type=password&username=admin&password=123456&client_id=client_edm
Accept: application/json
#返回信息
{
  "access_token": "04639609-681d-48bf-97c6-27745de02d6c",
  "token_type": "bearer",
  "refresh_token": "116de92c-5a7d-4e10-b0d0-c6bccc814814",
  "expires_in": 19,
  "scope": "all"
}
```

```xml
#校验token
GET http://localhost:9999/oauth/check_token?token=5f757406-e9bf-43ac-9549-3e886e830727
Accept: application/json
#返回信息
{
  "aud": [
    "autodeliver"
  ],
  "active": true,
  "exp": 1607312081,
  "user_name": "admin",
  "client_id": "client_edm",
  "scope": [
    "all"
  ]
}
```

```xml
#刷新token
GET http://localhost:9999/oauth/token?grant_type=refresh_token&client_edm&client_secret=abcxyz&refresh_token=43189dc1-fd13-4a12-ba71-a865544d95b8
Accept: application/json
```

7. 资源服务器（希望访问被认证的微服务）Resource Server配置

```java
@Configuration
@EnableResourceServer //开启资源服务器功能
@EnableWebSecurity //开启web访问安全
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

    private String sing_key = "edm123"; //jwt签名密钥

    /**
     * 该方法用于定义资源服务器向远程认证服务器发起请求，进行token校验等事宜
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        //设置当前资源服务器id
        resources.resourceId("autodeliver");
        //定义token服务对象（token校验靠token服务器对象）
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        //校验端点/接口设置
        remoteTokenServices.setCheckTokenEndpointUrl("http://localhost:9999/oauth/check_token");
        //携带客户端id和客户端安全码
        remoteTokenServices.setClientId("client_edm");
        remoteTokenServices.setClientSecret("abcxyz");
        //设置参数
        resources.tokenServices(remoteTokenServices);
    }

    /**
     * 场景：⼀个服务中可能有很多资源（API接⼝）
     * 某⼀些API接⼝，需要先认证，才能访问
     * 某⼀些API接⼝，压根就不需要认证，本来就是对外开放的接⼝
     * 我们就需要对不同特点的接⼝区分对待（在当前configure⽅法中完成），设置
     * 是否需要经过认证
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                //需要认证
                .authorizeRequests()
                .antMatchers("/autodeliver/**").authenticated()
                .antMatchers("/demo/**").authenticated()
                //不需要认证
                .anyRequest().permitAll();
    }
}
```

当我们第⼀次登陆之后，认证服务器颁发token并将其存储在认证服务器中，后期我们访问资源服务器时会携带token，资源服务器会请求认证服务器验证token有效性，如果资源服务器有很多，那么认证服务器压力会很⼤.......

另外，资源服务器向认证服务器check_token，获取的也是⽤户信息UserInfo，能否把⽤户信息存储到令牌中，让客户端⼀直持有这个令牌，令牌的验证也在资源服务器进⾏，这样避免和认证服务器频繁的交互...... 我们可以考虑使⽤ JWT 进行改造，使⽤JWT机制之后资源服务器不需要访问认证服务器......

### JWT 改造统一认证授权中心的令牌存储机制

**JWT令牌介绍**

通过上边的测试我们发现，当资源服务和授权服务不在⼀起时资源服务使用RemoteTokenServices 远程请求授权服务验证token，如果访问量较大将会影响系统的性能。

解决上边问题： 令牌采用JWT格式即可解决上边的问题，⽤户认证通过会得到⼀个JWT令牌，JWT令牌中已经包括了用户相关的信息，客户端只需要携带JWT访问资源服务，资源服务根据事先约定的算法自行完成令牌校验，无需每次都请求认证服务完成授权。

1. 什么是JWT?

   JSON Web Token（JWT）是⼀个开放的⾏业标准（RFC 7519），它定义了⼀种简介的、⾃包含的协议 格式，用于在通信双方传递json对象，传递的信息经过数字签名可以被验证和信任。JWT可以使用HMAC算法或使用RSA的公钥/私钥对来签名，防止被篡改。

2. JWT令牌结构

   JWT令牌由三部分组成，每部分中间使用点（.）分隔，⽐如：xxxxx.yyyyy.zzzzz

   * Header

     头部包括令牌的类型（即JWT）及所使用的哈希算法（如HMAC SHA256或RSA），例如

     ```json
     {
     	"alg": "HS256",
     	"type": "JWT"
     }
     ```

     将上边的内容使用Base64Url编码，得到的一个字符串就是JWT令牌的第一部分

   * Payload

     第二部分是负载，内容也是一个json对象，它是存放有效信息的地⽅，它可以存放jwt提供的现成字段，比如：iss（签发者）,exp（过期时间戳）, sub（面向的⽤户）等，也可⾃定义字段。 此部分不建议存放敏感信息，因为此部分可以解码还原原始内容。 最后将第⼆部分负载使用Base64Url 编码，得到⼀个字符串就是JWT令牌的第⼆部分。 ⼀个例⼦：

     ```json
     {
         "sub": "1234567890",
         "name": "Mark",
         "iat": 1516239022
     }
     ```

   * Signature

     第三部分是签名，此部分⽤于防止jwt内容被篡改。 这个部分使用base64url将前两部分进⾏编 码，编码后使用点（.）连接组成字符串，最后使⽤header中声明签名算法进行签名。

     ```
     HMACSHA256(
     	base64UrlEncode(header) + "." +
     	base64UrlEncode(payload),
     	secret)
     ```

     base64UrlEncode(header)：jwt令牌的第⼀部分

     base64UrlEncode(payload)：jwt令牌的第⼆部分

     secret：签名所使⽤的密钥

**认证服务器端JWT改造**

1. 改造主配置类 OauthServerConfigurer

```java
/**
     * 创建tokenStore对象
     * @return
     */
    public TokenStore getTokenStore() {
        //return new InMemoryTokenStore();
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    private static final String SIGN_KEY = "dweqe";

    /**
     * 返回jwt令牌转换器
     * @return
     */
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(SIGN_KEY);
        jwtAccessTokenConverter.setVerifier(new MacSigner(SIGN_KEY));
        return jwtAccessTokenConverter();
    }
```

2. 修改JWT令牌服务方法 OauthServerConfigurer

```java
/**
     * 该方法用户获取一个token服务对象（该对象描述了一个token有效期等信息）
     * @return
*/
public AuthorizationServerTokenServices getAuthorizationServerTokenServices() {
        //使用默认实现
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        //是否开启令牌刷新
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setTokenStore(getTokenStore());
        
        //针对JWT令牌的添加
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        
        //设置令牌有效时间（一般设置为20小时）
        //access_token就是我们请求资源需要携带的令牌
        defaultTokenServices.setAccessTokenValiditySeconds(20);
        //3天
        defaultTokenServices.setRefreshTokenValiditySeconds(259200);
        return defaultTokenServices;
}
```

3. 资源服务器校验JWT令牌 ResourceServerConfigurer

   不需要和远程认证服务器交互，添加本地 tokenStore

```java
private static final String SIGN_KEY = "dweqe";

    /**
     * 该方法用于定义资源服务器向远程认证服务器发起请求，进行token校验等事宜
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("autodeliver").tokenStore(getTokenStore()).stateless(true);
    }

    /**
     * 创建tokenStore对象
     * @return
     */
    public TokenStore getTokenStore() {
        //return new InMemoryTokenStore();
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 返回jwt令牌转换器
     * @return
     */
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(SIGN_KEY);
        jwtAccessTokenConverter.setVerifier(new MacSigner(SIGN_KEY));
        return jwtAccessTokenConverter();
    }
```

### 从数据库加载 Oauth2 客户端信息

1. 创建数据库并初始化数据（表名及字段保持固定）

```sql
SET NAMES utf8mb4; 
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for oauth_client_details -- ---------------------------
DROP TABLE IF EXISTS `oauth_client_details`; CREATE TABLE `oauth_client_details` ( `client_id` varchar(48) NOT NULL, `resource_ids` varchar(256) DEFAULT NULL, `client_secret` varchar(256) DEFAULT NULL, `scope` varchar(256) DEFAULT NULL, `authorized_grant_types` varchar(256) DEFAULT NULL, `web_server_redirect_uri` varchar(256) DEFAULT NULL, `authorities` varchar(256) DEFAULT NULL, `access_token_validity` int(11) DEFAULT NULL, `refresh_token_validity` int(11) DEFAULT NULL, `additional_information` varchar(4096) DEFAULT NULL, `autoapprove` varchar(256) DEFAULT NULL, PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; -- ----------------------------- Records of oauth_client_details -- ---------------------------
BEGIN; INSERT INTO `oauth_client_details` VALUES ('client_lagou123', 'autodeliver,resume', 'abcxyz', 'all', 'password,refresh_token', NULL, NULL, 7200, 259200, NULL, NULL); COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
```

2. 配置数据源

```xml
<dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.10</version>
        </dependency>
        <!--操作数据库需要事务控制-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
        </dependency>
		<dependency>
      <groupId>cn.chuchin</groupId>
      <artifactId>service-common</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/springcloud?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true
    username: root
    password: 123456
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 30
      max-wait: 50000
```

3. 认证服务器主配置类改造

```java
@Autowired
    private DataSource dataSource;

    /**
     * 客户端详情配置
     * 比如client_id, secret
     * 当前这个服务就如同QQ平台，网站作为客户端需要QQ平台进行登录授权认证等，提前需要到QQ平台注册
     * QQ平台会给网站
     * 颁发client_id等必要参数，说明客户端是谁
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);
        clients.withClientDetails(createJdbcClientDetailsService());
    }

    @Bean
    public JdbcClientDetailsService createJdbcClientDetailsService() {
        JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(
                dataSource);
        return jdbcClientDetailsService;
    }
```

### 从数据库验证用户合法性

1. 创建数据表users，初始化数据

```sql
SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------- Table structure for users -- ---------------------------
DROP TABLE IF EXISTS `users`; CREATE TABLE `users` ( `id` int(11) NOT NULL AUTO_INCREMENT, `username` char(10) DEFAULT NULL, `password` char(100) DEFAULT NULL, PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
-- ----------------------------- Records of users
-- ---------------------------
BEGIN; INSERT INTO `users` VALUES (4, 'lagou-user', 'iuxyzds'); COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
```

2. 操作数据表的JPA配置以及针对表的操作的Dao接口
3. 开发UserDetailService接口的实现类，根据用户名从数据库加载用户信息

```java
@Service
public class JdbcUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    /**
     * 根据username查询出该用户的所有信息，封装成UserDetails类型的对象返回，至于密码，框架会自动匹配
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByUsername(username);
        return new User(users.getUsername(),users.getPassword(),new ArrayList<>());
    }
}
```

4. 使用自定义的用户详情服务对象

```java
@Autowired
    private JdbcUserDetailsService jdbcUserDetailsService;

    /**
     * 处理用户名和密码验证事宜
     * 1. 客户端传递username和password 参数到认证服务器
     * 2. 一般来说username 和password 会存储在数据库中的用户表
     * 3. 根据用户表中数据，验证当前传递过来的用户信息的合法性
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jdbcUserDetailsService).passwordEncoder(passwordEncoder);
    }
```

### 基于Oauth2 的 JWT 令牌信息拓展

OAuth2帮我们⽣成的JWT令牌载荷部分信息有限，关于⽤户信息只有⼀个user_name，有些场景下我 们希望放⼊⼀些扩展信息项，比如，之前我们经常向session中存⼊userId，或者现在我希望在JWT的载 荷部分存⼊当时请求令牌的客户端IP，客户端携带令牌访问资源服务时，可以对比当前请求的客户端真实IP和令牌中存放的客户端IP是否匹配，不匹配拒绝请求，以此进⼀步提⾼安全性。那么如何在OAuth2 环境下向JWT令牌中存如扩展信息？

**认证服务器生成JWT令牌时存入拓展信息（比如ClientIp）**

1. 继承DefaultAccessTokenConverter类，重写convertAccessToken⽅法存⼊扩展信息

```java
@Component
public class AccessTokenConvertor extends DefaultAccessTokenConverter {


    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        // 获取到request对象
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes())).getRequest();
        // 获取客户端ip（注意：如果是经过代理之后到达当前服务的话，那么这种方式获取的并不是真实的浏览器客户端ip）
        String remoteAddr = request.getRemoteAddr();
        Map<String, String> stringMap = (Map<String, String>) super.convertAccessToken(token, authentication);
        stringMap.put("clientIp",remoteAddr);
        return stringMap;
    }
}
```

2. 将自定义的转换器对象注入（OauthServerConfiger）

```java
/**
     * 返回jwt令牌转换器（帮助我们生成jwt令牌的）
     * 在这里，我们可以把签名密钥传递进去给转换器对象
     * @return
     */
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(sign_key);  // 签名密钥
        jwtAccessTokenConverter.setVerifier(new MacSigner(sign_key));  // 验证时使用的密钥，和签名密钥保持一致
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConvertor);

        return jwtAccessTokenConverter;
    }
```

**资源服务器取出 JWT 令牌拓展信息**

资源服务器也需要⾃定义⼀个转换器类，继承DefaultAccessTokenConverter，重写 extractAuthentication提取方法，把载荷信息设置到认证对象的details属性中

```java
@Component
public class AccessTokenConvertor extends DefaultAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication oAuth2Authentication =
                super.extractAuthentication(map);
        oAuth2Authentication.setDetails(map);
        // 将map放⼊认证对象中，认证对象在controller中可以拿到 return oAuth2Authentication;
    }
}
```

将自定义的转换器注入

```java
/**
     * 返回jwt令牌转换器（帮助我们生成jwt令牌的）
     * 在这里，我们可以把签名密钥传递进去给转换器对象
     * @return
     */
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(sign_key);  // 签名密钥
        jwtAccessTokenConverter.setVerifier(new MacSigner(sign_key));  // 验证时使用的密钥，和签名密钥保持一致
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConvertor);

        return jwtAccessTokenConverter;
    }
```

业务类⽐如Controller类中，可以通过SecurityContextHolder.getContext().getAuthentication()获取到认证对象，进⼀步获取到扩展信息

### 其他

关于JWT令牌我们需要注意

* WT令牌就是⼀种可以被验证的数据组织格式，它的玩法很灵活，这里是基于Spring Cloud Oauth2 创建、校验JWT令牌
* 我们也可以自己写⼯具类⽣成、校验JWT令牌
* JWT令牌中不要存放过于敏感的信息，因为我们知道拿到令牌后，我们可以解码看到载荷部分的信息
* JWT令牌每次请求都会携带，内容过多，会增加网络带宽占用