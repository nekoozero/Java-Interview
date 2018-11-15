# 基础

Buffer,顾名思义，缓冲区，实际上是一个容器，是一个连续的数组。Channel 提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由 Buffer。

在 NIO 中，Buffer是一个顶层父类，他是一个抽象类，常用的 Buffer 的子类有：

- ByteBuffer
- IntBuffer
- CharBuffer
- LongBuffer
- DoubleBuffer
- FloatBuffer
- ShortBuffer

![图片来自网络](https://javadoop.com/blogimages/nio/6.png)

如果是对于文件读写，上面几种Buffer都可能会用到。但是对于网络读写来说，用的最多的是ByteBuffer。

## 属性

- <strong>容量（capacity）：</strong>缓冲区能够容纳的数据元素的最大数量。这一容量在缓冲区创建时被设定，并且永远不能被改变。
- <strong>上界（limit）：</strong>缓冲区的第一个不能被读或写的元素。或者说，缓冲区中现存元素的计数。
- <strong>位置（position）：</strong>下一个要被读或写的元素的索引。位置会自动由相应的get()和put()函数更新。
- <strong>标记（mark）：</strong>下一个要被读或写的元素的索引。位置会自动由相应的 get( )和 put( )函数更新一个备忘位置。调用 mark( )来设定 mark = postion。调用 reset( )设定 position =mark。标记在设定前是未定义的(undefined)。这四个属性之间总是遵循以下关系：0 <= mark <= position <= limit <= capacity 

![图片来自网络](https://javadoop.com/blogimages/nio/5.png)

# position和limit

capacity 的值是根据申请 Buffer 的大小和种类确定的，所以不能改变。而 position 和 limit 就可以根据需要而改变了，`buffer.limit()`、`buffer.position()`、`buffer.capacity()`,这三个方法和直观就不说了。

`buffer.flip()`一般用在<strong>写到读</strong>切换的时候。这个方法的能力就是将 limit 设为 position 的值，在是将 position 设为 0。在写数据的时候从 Buffer 的开始处——0 位置到 positn 位置之间写满了数据，如果这时候我们想要从开头读数据的话，就要将 positn 指向 0，以便可以读取 0 位置的数据，然后逐个向下读取。

![图片来自网络](https://javadoop.com/blogimages/nio/7.png)

`buffer.clear()`是清空 Buffer 的方法，但他没有真正的清除，只是将 position 置为 0，将 limit 置为 capacity；这样一来，你的写操作就可以将原来的数据覆盖了。
`buffer.rewind()`是将 positin 置为 0，这样一来就可以将 buffer 再重新读一遍，当然，它还可以做很多事。

# 常用

## 写buffer
对于 Buffer 来说，另一个常见的操作中就是，我们要将来自 Channel 的数据填充到 Buffer 中，在系统层面上，这个操作我们称为读操作，因为数据是从外部（文件或网络等）读到内存中。

```java
int num = channel.read(buf);
```

上述方法会返回从 Channel 中读入到 Buffer 的数据大小。

## 读buffer

前面介绍了写操作，每写入一个值，position 的值都需要加 1，所以 position 最后会指向最后一次写入的位置的后面一个，如果 Buffer 写满了，那么 position 等于 capacity（position 从 0 开始）。

如果要读 Buffer 中的值，需要切换模式，从写入模式切换到读出模式。注意，通常在说 NIO 的读操作的时候，我们说的是从 Channel 中读数据到 Buffer 中，对应的是对 Buffer 的写入操作，初学者需要理清楚这个。

调用 Buffer 的 flip() 方法，可以进行模式切换。其实这个方法也就是设置了一下 position 和 limit 值罢了。

```java
public final Buffer flip() {
    limit = position; // 将 limit 设置为实际写入的数据数量
    position = 0; // 重置 position 为 0
    mark = -1; // mark 之后再说
    return this;
}
```

附一个经常使用的方法：

```java
new String(buffer.array()).trim();
```

当然了，除了将数据从 Buffer 取出来使用，更常见的操作是将我们写入的数据传输到 Channel 中，如通过 FileChannel 将数据写入到文件中，通过 SocketChannel 将数据写入网络发送到远程机器等。对应的，这种操作，我们称之为写操作。

```java
int num = channel.write(buf);
```