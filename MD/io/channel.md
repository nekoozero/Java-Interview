# 基础

在标准的IO当中，都是基于字节流/字符流进行操作的，而在NIO中则是基于 Channel 和 Buffer进行操作，其中的 Channel 虽然模拟了流的概念，实则大不相同。

|区别|Stream|Channel|
|--|--|--|
|支持异步|不支持|支持|
|是否可双向传输数据|不能，只能单向|可以，既可以从通道读取数据，也可以向通道写入数据|
|是否结合 Buffer 使用|不|必须结合 Buffer 使用|
|性能|较低|较高|

Channel 用于在字节缓冲区和位于通道另一侧的实体（通常是文件或者套接字）之间以便有效的进行数据传输。借助通道，可以用最小的总开销来访问操作系统本身的I/O服务。

结构如下图：

![图片来自网络](https://img-blog.csdn.net/20151216134020281)

- `inChannel.read(byteBuffer)`,将 Channel 的字节读到 Buffer 中去。
- `inChannel.write(byteBuffer)`,将 Channel 的字节写到 Buffer中去。

# 分类

广义上来说通道可以分为两类：File I/O和Stream I/O，也就是文件通道和套接字通道，或者：

- FileChannel             从文件读写数据
- SocketChannel           通过TCP读写网络数据
- ServerSocketChannel     可以监听新进来的TCP连接，并对每个连接创建对应的SocketChannel
- DatagramChannel         通过UDP读写网络中的数据

具体应用放到之后再说。
