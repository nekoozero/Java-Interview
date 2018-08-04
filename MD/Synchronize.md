# synchronized 关键字原理

众所周知 `synchronized` 关键字是解决并发问题常用解决方案，有以下三种使用方式:

- 同步普通方法，锁的是当前对象。
- 同步静态方法，锁的是当前 `Class` 对象。
- 同步块，锁的是 `()` 中的对象。


实现原理：
`JVM` 是通过进入、退出对象监视器( `Monitor` )来实现对方法、同步块的同步的。

具体实现是在编译之后在同步方法调用前加入一个 `monitor.enter` 指令，在退出方法和异常处插入 `monitor.exit` 的指令。

其本质就是对一个对象监视器( `Monitor` )进行获取，而这个获取过程具有排他性从而达到了同一时刻只能一个线程访问的目的。

而对于没有获取到锁的线程将会阻塞到方法入口处，直到获取锁的线程 `monitor.exit` 之后才能尝试继续获取锁。

流程图如下:

![](https://ws2.sinaimg.cn/large/006tNc79ly1fn27fkl07jj31e80hyn0n.jpg)


通过一段代码来演示:

```java
    public static void main(String[] args) {
        synchronized (Synchronize.class){
            System.out.println("Synchronize");
        }
    }
```

使用 `javap -c Synchronize` 可以查看编译之后的具体信息。

```
public class com.crossoverjie.synchronize.Synchronize {
  public com.crossoverjie.synchronize.Synchronize();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: ldc           #2                  // class com/crossoverjie/synchronize/Synchronize
       2: dup
       3: astore_1
       **4: monitorenter**
       5: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
       8: ldc           #4                  // String Synchronize
      10: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      13: aload_1
      **14: monitorexit**
      15: goto          23
      18: astore_2
      19: aload_1
      20: monitorexit
      21: aload_2
      22: athrow
      23: return
    Exception table:
       from    to  target type
           5    15    18   any
          18    21    18   any
}
```

可以看到在同步块的入口和出口分别有 `monitorenter,monitorexit`
指令。

## 什么是monitor

每一个线程都有一个可用monitor列表，同时还有一个全局的可用列表，先来看monitor的内部

<div align="center">
  <img src="https://upload-images.jianshu.io/upload_images/2184951-c1fc7a8eee6d5d64.png?imageMogr2/auto-orient/">
</div>

- Owner：初始时为NULL表示当前没有任何线程拥有该monitor，当线程成功拥有该锁后保存线程唯一标识，当锁被释放时又设置为NULL；
- EntryQ：关联一个系统互斥锁（semaphore），阻塞所有试图锁住monitor失败的线程。
- RcThis：表示blocked或waiting在该monitor上的所有线程的个数。
- Nest：用来实现重入锁的计数。
- HashCode：保存从对象头拷贝过来的HashCode值（可能还包含GC age）。
- Candidate：用来避免不必要的阻塞或等待线程唤醒，因为每一次只有一个线程能够成功拥有锁，如果每次前一个释放锁的线程唤醒所有正在阻塞或等待的线程，会引起不必要的上下文切换（从阻塞到就绪然后因为竞争锁失败又被阻塞）从而导致性能严重下降。Candidate只有两种可能的值：0表示没有需要唤醒的线程，1表示要唤醒一个继任线程来竞争锁。

那么monitor的作用是什么呢？<strong>在 java 虚拟机中，线程一旦进入到被synchronized修饰的方法或代码块时，指定的锁对象通过某些操作将对象头中的LockWord指向monitor的起始地址与之关联，同时monitor中的Owner存放拥有该锁的线程的唯一标识，确保一次只能有一个线程执行该部分的代码，线程在获取锁之前不允许执行该部分的代码。</strong>

## 锁优化
`synchronized`  很多都称之为重量锁，`JDK1.6` 中对 `synchronized` 进行了各种优化，为了能减少获取和释放锁带来的消耗引入了`偏向锁`和`轻量锁`。


### 轻量锁
当代码进入同步块之前，如果同步对象为无锁状态时，当前线程会在栈帧中创建一个锁记录(`Lock Record`)区域，同时将锁对象的对象头中 `Mark Word` 拷贝到锁记录中，再尝试使用 `CAS` 将 `Mark Word` 更新为指向锁记录的指针。

如果更新**成功**，当前线程就获得了锁。

如果更新**失败** `JVM` 会先检查锁对象的 `Mark Word` 是否指向当前线程的锁记录。

如果是则说明当前线程拥有锁对象的锁，可以直接进入同步块。

不是则说明有其他线程抢占了锁，如果存在多个线程同时竞争一把锁，**轻量锁就会膨胀为重量锁**。

#### 解锁
轻量锁的解锁过程也是利用 `CAS` 来实现的，会尝试锁记录替换回锁对象的 `Mark Word` 。如果替换成功则说明整个同步操作完成，失败则说明有其他线程尝试获取锁，这时就会唤醒被挂起的线程(此时已经膨胀为`重量锁`)

轻量锁能提升性能的原因是：

认为大多数锁在整个同步周期都不存在竞争，所以使用 `CAS` 比使用互斥开销更少。但如果锁竞争激烈，轻量锁就不但有互斥的开销，还有 `CAS` 的开销，甚至比重量锁更慢。

### 偏向锁

为了进一步的降低获取锁的代价，`JDK1.6` 之后还引入了偏向锁。

偏向锁的特征是:锁不存在多线程竞争，并且应由一个线程多次获得锁。

当线程访问同步块并获取锁时，会使用 `CAS` 将线程 ID 更新到锁对象的 `Mark Word` 中，如果更新成功则获得偏向锁，并且之后每次进入这个对象锁相关的同步块时都不需要再次获取锁了。

#### 释放锁
当有另外一个线程获取这个锁时，持有偏向锁的线程就会释放锁，释放时会等待全局安全点(这一时刻没有字节码运行)，接着会暂停拥有偏向锁的线程，根据锁对象目前是否被锁来判定将对象头中的 `Mark Word` 设置为无锁或者是轻量锁状态。

偏向锁可以提高带有同步却没有竞争的程序性能，但如果程序中大多数锁都存在竞争时，那偏向锁就起不到太大作用。可以使用 `-XX:-userBiasedLocking=false` 来关闭偏向锁，并默认进入轻量锁。


### 其他优化

#### 适应性自旋
在使用 `CAS` 时，如果操作失败，`CAS` 会自旋再次尝试。由于自旋是需要消耗 `CPU` 资源的，所以如果长期自旋就白白浪费了 `CPU`。`JDK1.6`加入了适应性自旋:

> 如果某个锁自旋很少成功获得，那么下一次就会减少自旋。

