# 阻塞队列

是一个支持两个附加操作的队列。这两个附加操作支持阻塞的插入和移除。
- 支持阻塞的插入方法：当队列满时，队列会阻塞插入元素的线程，直到队列不满。
- 支持阻塞的移除方法：当队列为空时，获取元素的线程会等待队列变为非空。

`阻塞队列适用于生产者、消费者的场景`

## 7种队列

1. ArrayBlockingQueue: 一个由数组结构组成的有界阻塞队列。
2. LinkedBlockingQueue: 一个由链表结构组成的有界阻塞队列。
3. PriorityBlockingQueue: 一个支持优先级排序的无界阻塞队列。
4. DelayQueue: 一个优先级队列实现的无界阻塞队列。
5. SynchronousQueue: 一个不存储元素的阻塞队列。
6. LinkedTransferQueue: 一个由链表结构组成的无界阻塞队列。
7. LinkedBlockingQueue: 一个由链表结构组成的双向阻塞队列

## 说明

### ArrayBlockingQueue

此队列按照先进先出（FIFO）的原则对元素进行排序。默认情况下不保证线程公平（即按照阻塞的先后顺序访问队列，即先阻塞线程线访问队列）的访问队列。非公平性是指，阻塞的线程可以争夺访问队列。为了保证公平性，通常会降低吞吐量。可以使用以下代码创建一个公平的阻塞队列。

```java
ArrayBlockingQueue fairQueue = new ArrayBlockingQueue(1000,true);
```

### LinkedBlockingQueue

是一个用链表实现的有界阻塞队列。此队列的默认和最大长度为 `Inter.MAX_VALUE`。此队列按照先进先出的原则对元素进行排序。

### PriorityBlockingQueue

是一个支持优先级的无界阻塞队列。默认情况下采取自然顺序升序排列。也可以自定义类实现 compareTo() 方法来指定元素排序规则，或者初始化 PriorityBlockingQueue 时，指定构造参数 Coomparator 来对元素进行排序。需要注意的是不能保证优先级元素的顺序。

### DelayQueue

是一个支持延时获取元素的无界阻塞队列。队列使用 PriorityQueue 来实现。队列中的元素必须实现 Delay 接口，在创建元素时可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。

DelayQueue 非常有用，可以将 DelayQueue运用自一下应用场景。
    - 缓存系统的设计：可以用 DelayQueue 保存缓存元素的有效期，使用一个线程循环查询 DelayQueue，一旦能从 DelayQueue 中获取元素时，表示缓存有效期到了。
    - 定时任务调度：使用 DelayQueue 保存当天将会执行的任务和执行时间，一旦从 DelayQueue 中获取到了任务就开始执行，比如 TimerQueue 就是使用 DelayQueue 实现的。

### SynchronousQueue

SynchronousQueue 是一个不存储元素的阻塞队列。每一个 put 操作必须等待一个 take 操作，否则不能继续添加元素。

它支持公平访问队列。<strong>默认情况下线程采用非公平性策略访问队列</strong>。使用一下构造方法可以创建公平性访问的 Synchronous，如果设置为 true，则等待的线程会采用先进先出的顺序访问队列。

```java
public SynchronousQueue(boolean fair) {
    transferer = fair ? new TransferQueue() : new TransferStack();
}
```

SynchronousQueue 可以看成是一个传球手，负责把生产者线程处理的数据直接传递给消费者线程。队列本省不存储任何元素，非常适合传递性场景。Synchronous 的吞吐量高于 LinkedBlockingQueue 和 ArrayBlockingQueue。
