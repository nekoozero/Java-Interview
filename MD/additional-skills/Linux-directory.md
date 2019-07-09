# 系统目录介绍

## 目录

1. / 表示根目录
2. ~ 表示 /root
3. etc 存放系统配置目录
4. home 除了 root，所有用户默认在 home 下新建一个以用户名做为文件夹名称的文件夹
5. usr 所有用户安装的软件都放入到这个文件夹下。在 usr/local下新建一个 tmp，所有压缩包都上传到 tmp 中。

## 命令

linux 所有需要写路径的地方都支持两种写法

- 绝对路径：写出目录的全路径 cd  /usr/local
- 相对路径：相对与当前路径的路径(假设当前目录为/usr) cd local 

1. pwd: 查看当前所在目录位置
2. cd： 进入到文件夹
3. mkdir： 新建空文件夹
4. ls： 显示目录下的所有文件（夹）
5. ll： 显示目录下所有文件（夹）的详细信息
6. vi/vim: 编辑文件，如果文件不存在就（在保存了的情况下）新建一个
7. touch： 创建空文件
8. cat： 查看文件全部
9. head [-n] 文件名： 查看文件前n行，默认前 10 行。
10. tail [-n] 文件名： 查看文件后n行，默认后 10 行.
11. tailf： 动态显示后 n 行内容。 就是在一个窗口向文件里写东西，另一个窗口可以看到文件动态的输出。常常用在查看tomcat的启动日志的时候。
12. echo'内容'>>文件名： 向文件中添加一些内容
13. ifconfig： 打印网卡信息
14. reboot： 重启
15. tar zxvf 文件名： 解压
16. cp [-r] 源文件 新文件目录： 复制文件 -r表示复制文件夹
17. mv 源文件 新文件 ： 剪切（具备重命名的作用）
18. rm [-r] 文件名: 删除文件，-r说明删除文件夹 -rf 强制删除文件夹
19. clear： 清屏
20. chown -R mysql:mysql .
    chown [选项]... [所有者][:[组]] 文件...

## 安装jdk

下载linux的.tar.gz包，上传到centos上的/usr/local/temp(这个文件夹放要安装软件的临时文件，确认安装完成后可删除里面的内容)，在temp中 ```tar zxvf ```解压后复制一份到上一层目录，重命名jdk8。

编辑profile文件（/etc/profile）,注释掉有**export**的那一行，在其下面输入以下内容：

```
export JAVA_HOME=/usr/local/jdk8
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```

之后输入命令**source profile**,即可。可输入```#java -version``` 和 ```#javac``` 测试。

## 安装tomcat

下载解压步骤和jdk一样,放在/usr/local/tomcat目录下

```#vim/etc/profile```并添加内容：

```
export TOMCAT_HOME=/usr/local/tomcat
export CATALINA_HOME=/usr/local/tomcat
```

放行8080端口

```#vim /etc/sysconfig/iptables```

> 此过程可能会没有iptables文件，说明可能没有安装iptables软件，默认使用的是firewall作为防火墙。
```
//停掉firewall
systemctl stop firewalld 
systemctl mask firewalld
yum install -y iptables 
yum install iptables-services
```

把包含22（linux默认22端口是放行的）行复制一行，修改22为8080。（8080:9000，从8080到9000全部放行）。

然后重启服务-->```#service iptables restart```(start：启动 stop：停止)

这里还有使用firewall的方法：

启动防火墙：```# systemctl start firewalld.service```;

开启端口：```# firewall-cmd --zone=public --add-port=8080/tcp --permanent```;

重启防火墙：```# firewall-cmd --reload```;

启动tomcat，进入tomcat的bin文件下，命令：```#./startup.sh``` 

启动tomcat并打印启动信息，```#./startup.sh & tailf ../logs/catalina.out```，ctrl+c会停止输出日志，但不会停掉tomcat。


## 配置网络（centos7 vm12 其他配置省略）

因为重装了一个Centos，没网络，只能重新配置，又搞了不少时间，先记录下来，不一定在所有情况下正确，但是目前在我这个虚拟机上有效：

1. 配置文件 /etc/sysconfig/network-scripts/ifcfg-ens33。
2. 
```
XY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=dhcp
DEFROUTE=yes
IPV4_FAILURE_FATAL=yes
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=ens33
UUID=4109cfab-9d1c-4384-a170-b802f5d954f3
DEVICE=ens33

<--onboot改为yes

ONBOOT=yes
IPADDR=192.168.31.100
PREFIX=24
GATEWAY=192.168.31.2
DNS1=202.207.0.6

-->

IPV6_PRIVACY=no
```
之后输入```# service network restart```重启网络配置即可

## 安装mysql5.7

[原文连接](https://blog.csdn.net/bao19901210/article/details/51917641)
[原文连接](https://blog.csdn.net/Nicolas12/article/details/81813682)


字符集的设置查看另一篇mysql安装的文章。

进入数据库可以直接使用source执行脚本sql，比如```source /usr/local/demo.sql```。
