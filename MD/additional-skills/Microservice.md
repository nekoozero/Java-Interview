# 微服务

Mircroservice架构模式就是将整个Web应用组织为一系列小的 Web 服务。这些小的 Web 服务可以独立地编译及部署，并通过各自暴露的 API 接口相互通讯。他们彼此相互协作，作为一个整体为用户提供功能，却可以独立地进行。

微服务架构需要的功能或使用场景

1. 我们把整个系统根据业务拆分成几个子系统。
2. 每个子系统可以部署多个应用，多个应用之间使用负载均衡。
3. 需要一个服务注册中心，所有的服务都在注册中心注册，负载均衡也是用通过在注册中心注册的服务来使用一定策略来实现。
4. 所有的客户端都通过同一个网关地址访问后台的服务，通过路由配置，网关来判断一个 URL 请求由哪个服务处理。请求转发到服务上的时候也使用负载均衡。
5. 服务之间有时也需要互相访问。例如有一个用户模块，其他服务在处理一些业务的时候，要获取用户的用户数据。
6. 需要一个断路器，及时处理服务调用时的超时和错误，防止由于其中一个服务的问题而导致整体系统的瘫痪。
7. 还需要一个监护功能，监控每个服务调用花费的时间等。

SpringCloud是基于SpringBoot的一整套实现微服务的框架。他提供了微服务开发所需的配置管理、服务发现、断路器、智能路由、微代理、控制总线、全局锁、决策竞选、分布式会话和集群状态管理等组件。最重要的是，和spring boot框架一起使用的话，会让开发微服务架构的云服务非常方便。

![图片来自网络](http://img.ccblog.cn/img/20170118/181.jpg)

spring cloud子项目包括：

 

- Spring Cloud Config：配置管理开发工具包，可以让你把配置放到远程服务器，目前支持本地存储、Git以及Subversion。

- Spring Cloud Bus：事件、消息总线，用于在集群（例如，配置变化事件）中传播状态变化，可与Spring Cloud Config联合实现热部署。

- Spring Cloud Netflix：针对多种Netflix组件提供的开发工具包，其中包括Eureka、Hystrix、Zuul、Archaius等。

- Netflix Eureka：云端负载均衡，一个基于 REST 的服务，用于定位服务，以实现云端的负载均衡和中间层服务器的故障转移。

- Netflix Hystrix：容错管理工具，旨在通过控制服务和第三方库的节点,从而对延迟和故障提供更强大的容错能力。

- Netflix Zuul：边缘服务工具，是提供动态路由，监控，弹性，安全等的边缘服务。

- Netflix Archaius：配置管理API，包含一系列配置管理API，提供动态类型化属性、线程安全配置操作、轮询框架、回调机制等功能。

- Spring Cloud for Cloud Foundry：通过Oauth2协议绑定服务到CloudFoundry，CloudFoundry是VMware推出的开源PaaS云平台。

- Spring Cloud Sleuth：日志收集工具包，封装了Dapper,Zipkin和HTrace操作。

- Spring Cloud Data Flow：大数据操作工具，通过命令行方式操作数据流。

- Spring Cloud Security：安全工具包，为你的应用程序添加安全控制，主要是指OAuth2。

- Spring Cloud Consul：封装了Consul操作，consul是一个服务发现与配置工具，与Docker容器可以无缝集成。

- Spring Cloud Zookeeper：操作Zookeeper的工具包，用于使用zookeeper方式的服务注册和发现。

- Spring Cloud Stream：数据流操作开发包，封装了与Redis,Rabbit、Kafka等发送接收消息。

- Spring Cloud CLI：基于 Spring Boot CLI，可以让你以命令行方式快速建立云组件。

  
