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
19. clear： 清屏。

## 安装jdk

下载linux的.tar.gz包，上传到centos上的/usr/local/temp(这个文件夹放要安装软件的临时文件，确认安装完成后可删除里面的内容)，在temp中 ```tar zxvf ```解压后复制一份到上一层目录，重命名jdk8。

编辑profile文件（/etc/profile）,注释掉有**export**的那一行，在其下面输入以下内容：

```
export JAVA_HOME=/usr/local/jdk8
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```

之后输入命令**source profile**,即可。可输入java -version 和 javac 测试。