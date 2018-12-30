# 在创建redis主从复制的时候遇到的问题与解决方案

环境：分别是在三个虚拟机上的进行的，redis的版本是4.0以上的。

首先就是端口的问题，这个确实会有问题，测试中我为了方便将iptables关闭，也就是关闭了防火墙，但猜测其实每个虚拟机防火墙只要开启6379（主从复制需要的）和26379（哨兵需要的）端口即可。

redis.conf修改的：

1. bind 127.0.0.1 删除;
2. 都要添加redis密码requirepass和masterauth,最好这两个密码一样，貌似新版本不加密码不好设置主从;
3. 关闭保护模式 protected no;
4. 可以尝试将redis的日志文件记录下来，默认是不开启的：logfile "/usr/local/redis4.0/redis.log"，这样日志文件将会被记录下来，查看日志还是很有用处的。
5. 哨兵也是要做集群的。配置文件中 sentinel monitor mymaster 127.0.0.1 6000（主redis的ip和端口号）  2   这个后面的数字2,是指当有两个及以上的sentinel服务检测到master宕机，才会去执行主从切换的功能。



参考文章：
https://blog.csdn.net/pujiao5201314/article/details/51820979
https://blog.csdn.net/liangwenmail/article/details/79579979
https://bbs.csdn.net/topics/392374837

## 持久化方式
1. 快照方式：（默认持久化方式）
  这种方式就是将内存中数据以快照的方式写入到二进制文件中 ，默认的文件名为dump.rdb。

  客户端也可以使用 save 或者 bgsave 命令通知 redis 做一次快照持久化。save 操作是在主线程中保存快照的，由于 redis 是用一个主线程来处理所有客户端的请求，这种方式会阻塞所有客户端请求。所以不推荐使用。另一点需要注意的是，每次快照持久化都是将内存数据完整写入到磁盘一次，并不是增量的只同步增量数据。如果数据量大的话，写操作会比较多，必然会引起起大量的磁盘 IO 操作，可能会严重影响性能。

  注意：由于快照方式是在一定间隔时间做一次的，所以如果 redis 意外当机的话，就会丢失最后一次快照后的所有数据修改。

 注意：由于快照方式是在一定间隔时间做一次的，所以如果 redis 意外当机的话，就会
丢失最后一次快照后的所有数据修改
2. 日志追加方式：
  这种方式 redis 会将每一个收到的写命令都通过 write 函数追加到文件中(默认appendonly.aof)。当 redis 重启时会通过重新执行文件中保存的写命令来在内存中重建整个数据库的内容。当然由于操作系统会在内核中缓存 write 做的修改，所以可能不是立即写到磁盘上。这样的持久化还是有可能会丢失部分修改。不过我们可以通过配置文件告诉redis 我们想要通过 fsync 函数强制操作系统写入到磁盘的时机。有三种方式如下（默认是：每秒 fsync 一次）。

  appendonly yes //启用日志追加持久化方式
  #appendfsync always //每次收到写命令就立即强制写入磁盘，最慢的，但是保证完全的持久化，不推荐使用
  appendfsync everysec //每秒钟强制写入磁盘一次，在性能和持久化方面做了很好的折中，推荐
  #appendfsync no //完全依赖操作系统，性能最好,持久化没保证

  日志追加方式同时带来了另一个问题。持久化文件会变的越来越大。例如我们调用 incrtest 命令 100 次，文件中必须保存全部 100 条命令，其实有 99 条都是多余的。因为要恢复数据库状态其实文件中保存一条 set test 100 就够了。为了压缩这种持久化方式的日志文件。redis 提供了 bgrewriteaof bgrewriteaof bgrewriteaof bgrewriteaof 命令。收到此命令 redis 将使用与快照类似的方式将内存中的数据以命令的方式保存到临时文件中，最后替换原来的持久化日志文件。


## redis集群的搭建
https://www.cnblogs.com/mafly/p/redis_cluster.html

yum install ruby
yum install rubygems
gem install redis 

注意的几点，安装ruby版本要2.2.2以上的，通过rvm安装（会比较慢，可尝试国内镜像---没找到），安装之后
https://www.cnblogs.com/carryping/p/7447823.html，就是移除之前的ruby，将新安装的ruby作为默认的使用。

简单说一下自己的配置。


将redis（4.0）下src下的redis.conf复制到usr/local/redis-cluster/6001，修改6001下的redis.conf

```
port 9001（每个节点的端口号）
daemonize yes  (后台启动)
bind 192.168.31.131（绑定当前机器 IP）
dir /usr/local/redis-cluster/6001/（数据文件存放位置）
pidfile /var/run/redis_6001.pid（pid 6001和port要对应）
cluster-enabled yes（启动集群模式）
cluster-config-file nodes6001.conf（6001和port要对应）
cluster-node-timeout 15000
appendonly yes   (启动aof)

```
之后将6001复制5份改名为6002,6003,6004,6005,6006并修改里面的配置，主要就是4个配置,以6002配置为例

```
port 6002
dir /usr/local/redis-cluster/6002/
cluster-config-file nodes-6002.conf
pidfile /var/run/redis_6002.pid
```

之后逐个启动，利用ps -el | grep redis查看是否全都启动。

之后启动ruby脚本来创建集群：
/usr/local/redis4.0/src/redis-trib.rb create --replicas 1 192.168.31.131:6001 192.168.31.131:6002 192.168.31.131:6003 192.168.31.131:6004 192.168.31.131:6005 192.168.31.131:6006

1代表主从的比例是一，也就是一主一从，默认6001,6002,6003为主，6004（6001的从）,6005（6002的从）,6006（6002的从）为从。

启动客户端进行操作：redis-cli -c -h 192.168.119.131 -p 9001  （-c代表以集群的方式启动客户端，不然会出错）