# eshop-eshop

## 本项目商品详情多级缓存架构项目



缓存数据生产服务的工作流程分析

* （1）监听多个kafka topic，每个kafka topic对应一个服务（简化一下，监听一个kafka topic）
* （2）如果一个服务发生了数据变更，那么就发送一个消息到kafka topic中
* （3）缓存数据生产服务监听到了消息以后，就发送请求到对应的服务中调用接口以及拉取数据，此时是从mysql中查询的
* （4）缓存数据生产服务拉取到了数据之后，会将数据在本地缓存中写入一份，就是ehcache中
* （5）同时会将数据在redis中写入一份


### 测试1（ehcache）：

http://localhost:8080/testPutCache?id=1&name=shoes&price=55

http://localhost:8080/testGetCache?id=1


## 多级缓存架构

1、LRU算法概述

redis默认情况下就是使用LRU策略的，因为内存是有限的，但是如果你不断地往redis里面写入数据，那肯定是没法存放下所有的数据在内存的

所以redis默认情况下，当内存中写入的数据很满之后，就会使用LRU算法清理掉部分内存中的数据，腾出一些空间来，然后让新的数据写入redis缓存中

LRU：Least Recently Used，最近最少使用算法

将最近一段时间内，最少使用的一些数据，给干掉。比如说有一个key，在最近1个小时内，只被访问了一次; 还有一个key在最近1个小时内，被访问了1万次

这个时候比如你要将部分数据给清理掉，你会选择清理哪些数据啊？肯定是那个在最近小时内被访问了1万次的数据

2、缓存清理设置

redis.conf

maxmemory，设置redis用来存放数据的最大的内存大小，一旦超出这个内存大小之后，就会立即使用LRU算法清理掉部分数据

如果用LRU，那么就是将最近最少使用的数据从缓存中清除出去

对于64 bit的机器，如果maxmemory设置为0，那么就默认不限制内存的使用，直到耗尽机器中所有的内存为止; 但是对于32 bit的机器，有一个隐式的闲置就是3GB

maxmemory-policy，可以设置内存达到最大闲置后，采取什么策略来处理

* （1）noeviction: 如果内存使用达到了maxmemory，client还要继续写入数据，那么就直接报错给客户端
* （2）allkeys-lru: 就是我们常说的LRU算法，移除掉最近最少使用的那些keys对应的数据
* （3）volatile-lru: 也是采取LRU算法，但是仅仅针对那些设置了指定存活时间（TTL）的key才会清理掉
* （4）allkeys-random: 随机选择一些key来删除掉
* （5）volatile-random: 随机选择一些设置了TTL的key来删除掉
* （6）volatile-ttl: 移除掉部分keys，选择那些TTL时间比较短的keys

3、缓存清理的流程

* （1）客户端执行数据写入操作
* （2）redis server接收到写入操作之后，检查maxmemory的限制，如果超过了限制，那么就根据对应的policy清理掉部分数据
* （3）写入操作完成执行


4、redis的LRU近似算法

redis采取的是LRU近似算法，也就是对keys进行采样，然后在采样结果中进行数据清理。redis 3.0开始，在LRU近似算法中引入了pool机制，表现可以跟真正的LRU算法相当，但是还是有所差距的，不过这样可以减少内存的消耗。
redis LRU算法，是采样之后再做LRU清理的，跟真正的、传统、全量的LRU算法是不太一样的。

`maxmemory-samples`，比如5，可以设置采样的大小，如果设置为10，那么效果会更好，不过也会耗费更多的CPU资源



### kafka 接入

* （1）两种服务会发送来数据变更消息：商品信息服务，商品店铺信息服务，每个消息都包含服务名以及商品id
* （2）接收到消息之后，根据商品id到对应的服务拉取数据，这一步，我们采取简化的模拟方式，就是在代码里面写死，会获取到什么数据，不去实际再写其他的服务去调用了
* （3）商品信息：id，名称，价格，图片列表，商品规格，售后信息，颜色，尺寸
* （4）商品店铺信息：其他维度，用这个维度模拟出来缓存数据维度化拆分，id，店铺名称，店铺等级，店铺好评率
* （5）分别拉取到了数据之后，将数据组织成json串，然后分别存储到ehcache中，和redis缓存中


### 测试业务逻辑

本机hosts文件添加：

```text
192.168.2.201 eshop-cache01
192.168.2.202 eshop-cache02
192.168.2.203 eshop-cache03
192.168.2.204 eshop-cache04
```

* （1）创建一个kafka topic
* （2）在命令行启动一个kafka producer
* （3）启动系统，消费者开始监听kafka topic
* （4）在producer中，分别发送两条消息，一个是商品信息服务的消息，一个是商品店铺信息服务的消息
* （5）能否接收到两条消息，并模拟拉取到两条数据，同时将数据写入ehcache中，并写入redis缓存中
* （6）ehcache通过打印日志方式来观察，redis通过手工连接上去来查询


```bash
bin/kafka-console-producer.sh --broker-list 192.168.2.201:9092,192.168.2.202:9092,192.168.2.203:9092 --topic cache-message
> {"serviceId":"productInfoService","productId":1}
> {"serviceId":"shopInfoService","shopId":1}
```