# IO 的模型

根据 UNIX 网络编程对 I/O 模型的分类，在 UNIX 可以归纳成5种 I/O 模型：

- <strong>阻塞I/O</strong>
- <strong>非阻塞I/O</strong>
- <strong>I/O多路复用</strong>
- 信号驱动I/O
- 异步I/O

## 基础

### 文件描述符

Linux 的内核将所有外部设备都看作一个文件来操作，对一个文件的读写操作会<strong>调用内核提供的系统命令（api）</strong>，返回一个 `file descriptor` (fd,文件描述符)。而对一个 socket 的读写也会有相应的描述符，称为 `socket fd`（socket文件描述符），描述符就是一个数字，<strong>指向内核中的一个结构体</strong>(文件路径，数据区等一些属性)。

> 所以说：在 Linux 下对文件的操作是<strong>利用文件描述符来实现的</strong>。

### 用户空间和内核空间

为了保证用户进程不能直接操作内核（kernel），<strong>保证内核的安全</strong>，操作系统将虚拟空间划分为两部分。

- 一部分为<strong>内核空间</strong>
- 一部分为<strong>用户空间</strong>

### I/O运行过程

I/O在系统中的运行是怎么样的(read 为例)

![图片来自网络](https://upload-images.jianshu.io/upload_images/5291509-31c4a3c772cff4d5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

当应用程序调用 read 方法时，是需要等待的--->从内核空间中找数据，再将内核空间的数据拷贝到用户空间的。

- <strong>这个等待是必要的过程。</strong>

## 常用的I/O模型

- 阻塞I/O
- 非阻塞I/O
- I/O多路复用

### 阻塞I/O模型

在进程（用户）空间中调用 `recvfrom`,其系统调用知道数据包到达且<strong>被复制到应用进程的缓冲区中或者发生错误时才返回</strong>,在此期间一直等待。

![图片来自网络](https://upload-images.jianshu.io/upload_images/5291509-8666e940ae1f5e8d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/493/format/webp)

### 非阻塞I/O模型

`recvfrom` 从应用层到内核的时候，如果没有数据就直接返回一个 EWOULDBLOCK 错误，一般都对非阻塞I/O模型进行轮询检查这个状态，看内核是不是有数据到来。

![图片来自网络](https://upload-images.jianshu.io/upload_images/5291509-a093199b083a737d?imageMogr2/auto-orient/strip%7CimageView2/2/w/493/format/webp)

### I/O复用模型

在 Linux 下对文件的操作是利用文件描述符（file descriptor）来实现的。

在 Linux 下它是这样子实现 I/O 复用模型的：

- 调用 `select/poll/epoll/pselect`其中一个函数，传入多个文件描述符，如果有一个文件描述符就绪，则返回，否则阻塞到超时。

比如 `poll()` 函数是这样子的： `int poll(struct pollfd *fds,nfds_t nfds,int timeout);`

其中 `pollfd`结构定义如下：

```c
struct pollfd {
	int fd;     /* 文件描述符 */
	short events; /* 等待的事件 */
	short revents; /* 实际发生了的事件 */
};
```

![图片来自网络](https://upload-images.jianshu.io/upload_images/5291509-589e6e3543c00c5b?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)
![图片来自网络](https://upload-images.jianshu.io/upload_images/5291509-7a9d2c5369a8e5e4?imageMogr2/auto-orient/strip%7CimageView2/2/w/493/format/webp)

- （1）当用户进程调用了 select，那么整个进程会被 block;
- （2）而同时，kernel 会“监视”所有 select 负责的 socket;
- （3）当任何一个 socket 中的数据准备好了，select 就会返回;
- （4）这个时候用户进程再调用 read 操作，将数据从 kernel 拷贝到用户进程（空间）。
- 所以，I/O多路复用的特点就是<strong>通过一种机制一个进程能同时等待多个文件描述符</strong>，而这些文件描述符其中的任意一个进入度就绪状态，select() 函数就可以返回。

select/epoll 的优势并不是对于单个连接能处理得更快，而是<strong>在于能处理更多的连接</strong>。
