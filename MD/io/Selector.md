# 选择器

选择器是 SelectableChannel 对象的多路复用，Selector 可以同时监控多个 SelectableChannel 的 IO 状况，也就是说，利用 Selector 可使一个单独的线程管理多个 Channel ，selector 是非阻塞 IO 的核心。

![图片来自网络](https://upload-images.jianshu.io/upload_images/5408072-5b76ec216c32aa39.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/408/format/webp)

SelectableChannel 的继承树如下图：

![图片来自网络](https://images2015.cnblogs.com/blog/1072224/201704/1072224-20170405202107910-1004090576.png)

# 创建

通过调用 `Selector.open()` 方法创建一个 `Selector`,如下：

```java
Selector selector = Selector.open();
```

## 注册通道

为了将 `Channel` 和 `Selector` 配合使用，必须将 `Channel` 注册到 `Selector` 上。通过 `SelectableChannel.register()` 方法来实现，如下： 

```java
channel.configureBlocking(false);
SelectionKey key = channel.register(selector,Selectionkey.OP_READ);
```

与 `Selector` 一起使用时，`Channel` 必须处于非阻塞模式下。这意味着不能讲 `FileChannel` 与 `Selector` 一起使用，因为 `FileChannel` 不能切换到非阻塞模式。而套接字通道都可以。

`Channel` 能够触发一个时间，意思是该事件已经就绪。所以，某个 `channel` 成功连接到另一个服务器称为“<strong>连接就绪</strong>”。一个 `server socket channel` 准备好接受新进入的连接称为“<strong>接受就绪</strong>”。一个有数据可读的通道可以说是“<strong>读就绪</strong>”。等待写数据的通道可以说是“<strong>写就绪</strong>”。

这四种事件用SelectionKey的四个常量来表示：

- 读：SelectionKey.OP_READ
- 写：SelectionKey.OP_WRITE
- 连接：SelectionKey.OP_CONNECT
- 接受：SelectionKey.OP_ACCEPT

如果需要监听多个事件是：

```java
int key = SelectionKey.OP_READ|SelectionKey.OP_WRITE; //表示同时监听读写操作
```
