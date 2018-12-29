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
