# Excutor 框架的成员

主要成员有：ThreadPoolExecutor、ScheduledThreadPoolExecutor、Future 接口、Runnable 接口、Callable 接口和 Executors。

## ThreadPoolExecutor

ThreadPoolExecutor 通常使用工厂类 Executors 来创建。Executors 可以创建3中类型的 ThreadPoolExecutor：SignleThreadExecutor、FixedThreadPool 和 CachedThreadPool。

### FixedThreadPool

Executors提供的，创建适用固定线程数的 FixedThreadPool 的 API。

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads,nThreads,0L,
                              TimeUnit.MILLISECONDS,
                              new LinkedBlockingQueue<Runnable>);
}
```

FixedThreadPool 的 corePoolSize 和 maximumPoolSize 都被设置为创建 FixedThreadPool 时指定的参数 nThreads。

当线程中的线程数大于 corePoolSize 时，keepAliveTime 为多余的空闲线程等待新任务的最长时间，超过这个时间后多余的线程将被终止。这里把 keepAliveTime 设置为 0L，意味着多余的空闲线程会被立即终止。

1. 如果当前运行的线程数少于 corePoolSize，则创建新线程来执行任务。
2. 在线程池完成预热之后（当前运行的线程数等于 corePoolSize），将任务加入 LinkedBlockingQueue。
3. 线程执行完 1 中的任务后，会在循环中反复从 LinkedBlockingQueue 获取任务来执行。

FixedThreadPool 使用无界队列 LinkedBlockingQueue 作为线程池的工作队列（队列的容量为Integer.MAX_VALUE）。使用无界队列作为工作队列会对线程池带来如下影响：

- 当线程池中的线程数达到 corePoolSize 后，新任务将在无界队列中等待，因此线程池中的线程数不会超过 corePoolSize。
- 由于 1，使用无界队列时 maximumPoolSize 将是一个无效参数。
- 由于 1 和 2，使用无界队列时 keepAliveTime 将是一个无效参数。
- 由于使用无界队列，运行中的 FixedThreadPool（未执行 shutdown()或shutdownNow()）不会拒绝任务。

### SignleThreadExecutor
是使用单个 worker 线程的 Executor。下面是 SignleThreadExecutor 的实现：

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1,1,0L,TimUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()));
}
```

SignleThradExecutor 的 corePoolSize 和 maximumPoolSize 被设置为 1。其他参数与 FixedThreadPool 相同。SignleThreadExecutor 使用无界队列 LinkedBlockingQueue 作为线程池的工作队列（队列的容量为 Integer.MAX_VALUE）。SignleThreadExecutor使用无界队列作为工作队列对线程池带来的影响与 FixedThreadPool相同。

1. 如果当前运行的线程数少于 corePoolSize（即线程池中午运行的线程），则创建一个新的线程来执行任务。
2. 在线程池完成预热之后（当前线程池中有个运行的线程），将任务加入 LinkedBlockingQueue。
3. 线程执行完 1 中的任务后，会在一个无限循环中反复从 LinkedBlockingQueue 或取任务来执行。

### CachedThreadPool
是一个会根据需要创建新线程的线程池。虾米那是创建 CachedThreadPool 的源代码。

```java
public static ExectorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.Max_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runable>());
}
```

CachedThreadPool 的 corePoolSize 被设置为 0，即 corePool 为空；maximumPoolSize 被设置为 Integer.MAX_VALUE,即 maximumPool 是无界的。这里把 keepAliveTime 设置为 60L，意味着 CachedThreadPool 中的空闲线程等待新任务的最长时间为 60 秒，空闲线程超过 60 秒后将会被终止。

CachedThreadPool 使用没有容量的 SynchronousQueue 作为线程池的工作队列，但 CachedThreadPool 的 maximumPool 是无界的。这意味着，如果主线程提交任务的速度高于 maximumPool 中线程处理任务的速度时，CachedThreadPool 会不断创建新线程。极端情况下，CachedThreadPool 会因为创建过多线程而耗尽 CPU 和内存资源。

SynchronousQueue 是一个没有容量的阻塞队列。每个插入操作必须等待另一个线程的对应移除操作。反之亦然。CachedThreadPool 使用 SychronousQueue，把主线程提交的任务传递给空闲线程执行。