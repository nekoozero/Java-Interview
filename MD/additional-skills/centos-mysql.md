# 在centos6.4上搭建mysql

`仅记录个人所为`。

搭建之前先将以前的mysql删除干净。

本人在自己虚拟机上搭建mysql，由于自己当时装的centos是32位的（同学给我装的，我也不大懂，以后会自己搞搞的）。关于怎么查看centos的位数，打开终端，输入 # uname -i，如果是x86_64就是64位，如果是i386那么就是32位。

所以我找了这个 mysql-5.7.23-1.el6.i686.rpm-bundle.tar 来安装mysql，通过WinSCP将其上传到/usr/local/mysql目录下，mysql目录为自己创建的。

解压此文件 #tar xvf mysql-5.7.23-1.el6.i686.rpm-bundle.tar 

在安装之前请先安装几个依赖:

   yum install libaio

   yum  install    numactl
   

安装：

 #rpm -ivh mysql-community-common-5.7.23-1.el6.i686.rpm

 #rpm -ivh mysql-community-libs-5.7.23-1.el6.i686.rpm

 #rpm -ivh mysql-community-client-5.7.23-1.el6.i686.rpm

 #rpm -ivh mysql-community-server-5.7.23-1.el6.i686.rpm

安装时请按顺序输入。

初始化数据库：

为了保证数据库目录为与文件的所有者为mysql登录用户，如果你是以root身份运行mysql服务，需要执行下面的命令初始化：

 #mysqld --initialize --user=mysql     
或
 #mysqld --initialize-insecure --user=mysql    

上面一个会生成一个默认密码需要到/var/log/mysqld.log找，而且输入的时候非常麻烦，所以就是用下面的命令初始化，不会为root用户生成一个密码，默认密码为空。

启动mysql：

 #service mysqld start 重启的命令为 #service mysqld restart

登录并修改密码：

 #mysql -u root -p

登陆进入mysql，修改密码，

mysql> set password for root@localhost = password('123');  

记得
mysql> flush privileges;

刷新一下权限

设置mysql字符集：

修改配置文件 #vim /etc/my.cnf  加入下面代码：

```
[client]
default-character-set=utf8
[mysqld]
character-set-server=utf8
collation-server=utf8_general_ci
```

保存，重启MySQL服务，进入mysql输入命令：mysql> status 可以查看字符集设置。

设置mysql的远程访问：

进入数据库，对系统数据库的root账户设置远程访问的密码，与本地的root访问密码并不冲突。

mysql>grant all privileges on *.* to 'root'@'%' identified by '123456' with grant option; 
 #123456为你需要设置的密码.

设置防火墙，不然3306端口还是无法访问。

退出mysql。输入# iptables -I INPUT -p tcp -m state --state NEW -m tcp --dport 3306 -j ACCEPT。

设置完之后，查看一下是否能通过。

 #iptables -L -n
看到state NEW tcp dpt:3306 就说明设置好了。

如果想要限制访问# iptables -D INPUT -p tcp -m state --state NEW -m tcp --dport 3306 -j ACCEPT

我的总的步骤到这边就差不多了 。中间还遇到过mysql用户组的问题，当时也就放着不做了，以后有机会会尝试做做看。就酱。
