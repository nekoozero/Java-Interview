# 解决centos sudo提示用户不在sudoers文件中

1. 进入超级用户模式。输入 `su -`,系统会让你输入超级用户密码，输入密码之后就进入了超级用户模式（当然也可以直接用root进入）。
2. 添加文件的写权限。输入命令 `chmod u+w /etc/sudoers`。
3. 编辑/etc/sudoers文件。在文末输入
   nekoo   ALL=(root)   ALL,!/usr/bin/passwd [A-Za-z]*,!/usr/bin/passwd root
   上面的指令就是说， 给账户 nekoo 除了修改其他账户密码之外的所有操作权限,然后保存退出即可。
4. 撤销文件的写权限。输入命令`chmod u-w /etc/sudoers`
5. roo切换普通用户 `su - nekoo`.
6. `sudo groupdd docker` 创建docker组
7. `sudo gpasswd -a ${USER} docker` 将当前用户加入docker组。
8. `sudo systemctl restart docker` 重新启动docker服务
9. 退出nekoo重新登录。
10. 运行docker命令 `docker ps`。 
