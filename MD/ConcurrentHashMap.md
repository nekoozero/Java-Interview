**更多 HashMap 与 ConcurrentHashMap 相关请查看[这里](https://crossoverjie.top/2018/07/23/java-senior/ConcurrentHashMap/)。**

# ConcurrentHashMap 实现原理

由于 `HashMap` 是一个线程不安全的容器，主要体现在容量大于`总量*负载因子`发生扩容时会出现环形链表从而导致死循环。

因此需要支持线程安全的并发容器 `ConcurrentHashMap` 。

## JDK1.7 实现

### 数据结构
![](https://ws2.sinaimg.cn/large/006tNc79ly1fn2f5pgxinj30dw0730t7.jpg)

如图所示，是由 `Segment` 数组、`HashEntry` 数组组成，和 `HashMap` 一样，仍然是数组加链表组成。

`ConcurrentHashMap` 采用了分段锁技术，其中 `Segment` 继承于 `ReentrantLock`。不会像 `HashTable` 那样不管是 `put` 还是 `get` 操作都需要做同步处理，理论上 ConcurrentHashMap 支持 `CurrencyLevel` (Segment 数组数量)的线程并发。每当一个线程占用锁访问一个 `Segment` 时，不会影响到其他的 `Segment`。

### get 方法
`ConcurrentHashMap` 的 `get` 方法是非常高效的，因为整个过程都不需要加锁。

只需要将 `Key` 通过 `Hash` 之后定位到具体的 `Segment` ，再通过一次 `Hash` 定位到具体的元素上。由于 `HashEntry` 中的 `value` 属性是用 `volatile` 关键词修饰的，保证了内存可见性，所以每次获取时都是最新值([volatile 相关知识点](https://github.com/crossoverJie/Java-Interview/blob/master/MD/Threadcore.md#%E5%8F%AF%E8%A7%81%E6%80%A7))。

### put 方法

内部 `HashEntry` 类 ：

```java
    static final class HashEntry<K,V> {
        final int hash;
        final K key;
        volatile V value;
        volatile HashEntry<K,V> next;

        HashEntry(int hash, K key, V value, HashEntry<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
```

虽然 HashEntry 中的 value 是用 `volatile` 关键词修饰的，但是并不能保证并发的原子性，所以 put 操作时仍然需要加锁处理。

首先也是通过 Key 的 Hash 定位到具体的 Segment，在 put 之前会进行一次扩容校验。这里比 HashMap 要好的一点是：HashMap 是插入元素之后再看是否需要扩容，有可能扩容之后后续就没有插入就浪费了本次扩容(扩容非常消耗性能)。

而 ConcurrentHashMap 不一样，它是先将数据插入之后再检查是否需要扩容，之后再做插入。

### size 方法

每个 `Segment` 都有一个 `volatile` 修饰的全局变量 `count` ,求整个 `ConcurrentHashMap` 的 `size` 时很明显就是将所有的 `count` 累加即可。但是 `volatile` 修饰的变量却不能保证多线程的原子性，所有直接累加很容易出现并发问题。

但如果每次调用 `size` 方法将其余的修改操作加锁效率也很低。所以做法是先尝试两次将 `count` 累加，如果容器的 `count` 发生了变化再加锁来统计 `size`。

至于 `ConcurrentHashMap` 是如何知道在统计时大小发生了变化呢，每个 `Segment` 都有一个 `modCount` 变量，每当进行一次 `put remove` 等操作，`modCount` 将会 +1。只要 `modCount` 发生了变化就认为容器的大小也在发生变化。



## JDK1.8 实现

![](https://ws3.sinaimg.cn/large/006tNc79gy1fthpv4odbsj30lp0drmxr.jpg)

1.8 中的 ConcurrentHashMap 数据结构和实现与 1.7 还是有着明显的差异。

其中抛弃了原有的 Segment 分段锁，而采用了 `CAS + synchronized` 来保证并发安全性。

### 重要概念
1. table: 默认为null，初始化发生在第一次插入操作，默认大小为16的数组，用来存储Node节点，扩容时大小总是2的幂次方。
2. nextTable：默认为null，扩容时新生成的数组，其大小为原数组的两倍。
3. sizeCtl： 默认为0，用来控制table的初始化和扩容操作，具体应用在后续会体现出来。
4. **-1**: 代表table正在初始化
5. **-N**：表示有N-1个线程正在进行扩容操作
6. 其余情况：如果table未初始化，表示table需要初始化的大小。
             如果table初始化完成，表示table的容量，默认是table大小的0.75倍。
7. Node：保存key，value及key的hash值的数据结构。 

![](https://ws3.sinaimg.cn/large/006tNc79gy1fthq78e5gqj30nr09mmz9.jpg)

也将 1.7 中存放数据的 HashEntry 改为 Node，但作用都是相同的。

其中的 `val next` 都用了 volatile 修饰，保证了可见性。

8. ForwardingNode：一个特殊的Node节点，hash值为-1，其中存储nextTable的引用。

```java
final class ForwardingNode<K,V> extends Node<K,V>{
    final Node<K,V> nextTable;
    ForwardingNode(Node<K,V>[] tab){
        super(MOVED,null,null,null);
        this.nextTable = tab;
    }
}
```
只有table发生扩容的时候，ForwardingNode才会发挥作用，作为一个占位符放在table中表示当前节点为null或者已经被移动。

### 实例初始化
实例初始化ConcurrentHashMap是带参数时，会根据参数调整table的大小，假设参数为，最终会调整成256，确保table的大小总是2的幂次方，算法如下：

```java
ConcurrentHashMap<String,String> hashMap = new ConcurrentHashMap<>(100);
private static final int tableSizeFor(int c){
    int n = c-1;
    n |= n>>>1;
    n |= n>>>2;
    n |= n>>>4;
    n |= n>>>8;
    n |= n>>>16;
    return (n<0)?1:(n>=MAIMUM_CAPACITY)?MAXIMUM_CAPACITY:n+1;
}
```

注意，ConcurrentHashMap在构造函数中只会初始化sizeCtl值，并不会直接初始化table，而是延缓到第一次put操作。

### table初始化
table初始化操作会延缓到第一次put行为，但是put是可以并发执行的，Doug Lea是如何实现table只初始化一次的：

```java
private final Node<K,V>[] initTable(){
    Node<K,V>[] tab;
    int sc;
    while((tab=table)==null||tab.length==0){
        //如果一个线程发现sizeCtl<0，意味着另外的线程执行CAS操作成功，当前线程只需要让出cpu时间片
        if((sc=sizeCtl)<0)
            Thread.yield();//lost,initialization race;just spin
        else if(U.compareAndSwapInt(this,SIZECTL,sc,-1)){
            try{
                if((tab=table)==null||tab.length==0){
                    int n = (sc>0)?sc:DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab =nt;
                    sc = n-(n>>>2);
                }
            }finally{
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

sizeCtl默认为0，如果ConcurrentHashMap实例化时有传参数，sizeCtl会是一个2的幂次方的值。所以执行第一次put操作的线程会执行Unsafe.compareAndSwapInt方法修改sizeCtl为-1，有且只有一个线程能修改成功，其他线程通过Thread.yield()让出CPU时间片等待table初始化完成并返回table。


### put 方法

重点来看看 put 函数：

![](https://ws3.sinaimg.cn/large/006tNc79gy1fthrz8jlo8j30oc0rbte3.jpg)

- 根据 key 计算出 hashcode 。
- 判断是否需要进行初始化。
- `f` 即为当前 key 定位出的 Node，如果为空表示当前位置可以写入数据，利用 CAS 尝试写入，失败则自旋保证成功。
- 如果当前位置的 `hashcode == MOVED == -1`,则需要进行扩容。
- 如果都不满足，则利用 synchronized 锁写入数据。
- 如果数量大于 `TREEIFY_THRESHOLD` 则要转换为红黑树。

#### 详细
1. hash算法：
    ```java
    static final int spread(int h){return (h^(h>>>16))&HASH_BITS;}
    ```
2. table中定位索引的位置，n是table的大小。
   ```java
   int index = (n-1)&hash;
   ```
3. 获取table中对应的索引的元素f。
   Doug Lea采用Unsafe.getObjectVolatile来获取，为什么不直接table[index]？
   在JMM中，每个线程都有一个工作内存，里面存储着table的副本，虽然table是volatile修饰的，但不能保证每次都能拿到table中的最新元素（volatile不能保证原子性），Unsafe.getObjectVolatile可以直接获取指定内存的数据，保证了每次拿到数据都是最新的。
4. 如果f为null，说明table中这个位置第一次插入元素，利用Unsafe.compareAndSwapObject方法插入Node节点。
   - 如果CAS成功，说明节点已经插入，随后addCount(1L,binCount)方法会检查当前容量是否需要扩容。
   - 如果CAS失败，说明其他线程提前插入了节点，自旋重新尝试在这个位置插入节点。
5. 如果f的hash值为-1，说明当前f是ForwardingNode节点，意味着有其他线程正在扩容，则一起进行扩容操作。
6. 其余情况把新的Node节点把链表或红黑树的方式插入到合适的位置，这个过程采用同步内置锁实现并发。

在节点f上进行同步，节点插入之前，再次利用tabAt(tab,i)==f判断，防止被其他线程修改。

1. 如果f.hash>=0,说明f是链表结构的头结点，遍历链表，如果找到对应的node节点，则修改value，否则在链表尾部加入节点。
2. 如果f是TreeBiin类型节点，说明f是红黑树根节点，则在树结构上遍历元素，更新或者增加节点。
3. 如果链表中节点数binCount>=TREEIFY_THRESHOLD(默认是8)，则把链表转化为红黑树结构。


### get 方法

![](https://ws1.sinaimg.cn/large/006tNc79gy1fthsnp2f35j30o409hwg7.jpg)

- 根据计算出来的 hashcode 寻址，如果就在桶上那么直接返回值。
- 如果是红黑树那就按照树的方式获取值。
- 就不满足那就按照链表的方式遍历获取值。

### table扩容

当table容量不足时，即table的元素数量达到容量阈值sizeCtl，需要对table进行table进行扩容。
整个扩容分为两部分：

1. 构建一个nextTable，大小为table的两倍。
2. 把table的数据复制到nextTable。

这两个过程在单线程下实现很简单，但是ConcurrentHashMap是支持并发插入的，扩容操作自然也会有并发的出现，这种情况下，第二步可以支持节点的并发复制，这样性能自然提升不少，但实现的复杂度也上升了一个台阶。

构建nextTable，这个过程只能只有单个线程进行nextTable的初始化，具体实现：

```java
private final void addCount(long x,int check){
    //...省略部分代码
    if(check>0){
        Node<K,V>[] tab,nt;
        int n,sc;
        while(s>=(long)(sc=sizeCtl)&&(tab=table)!=null&&(n=tab.length)<MAXIMUM_CAPACITY){
            int rs = resizeStamp(n);
            if(sc<0){
                if((sc>>>RESIZE_STAMP_SHIFT)!=rs||sc=rs+1||sc==rs+MAX_RESIZERS||(nt=nextTable)==null||transferIndex<=0)
                break;
            }
            if(U.compareAndSwapInt(this,SIZECTL,sc,sc+1))
                transfer(tab,nt);
        }else if(U.compareAndSwapInt(this,SIZECTL,sc,(rs<<RESIZE_STAMP_SHIFT)+2))
            transfer(tab,null);
        s = sumCount();
    }
}
```

通过Unsafe.compareAndSwapInt修改sizeCtl值，保证只有一个线程能够初始化nextTable,
扩容后的数组长度为原来的两倍，但是容量是原来的1.5.

节点从table移动到nextTable，大体思想是遍历复制的过程。

1. 首先根据运算得到需要遍历的次数i，然后利用tabAt方法获得i位置的元素f，初始化一个forwardNode实例fwd。
2. 如果f==null，则在table中的i位置放入fwd，这个过程是采用Unsafe.compareAndSwapObject方法实现的，很巧妙地实现了节点的并发移动。
3. 如果f是链表的头结点，就构造一个反序链表，把他们分别放在nextTable的i和i+n的位置上，移动完成，采用Unsafe.putObjectVolatile方法给table原位置复制fwd。
4. 如果f是TreeBin节点，也做一个反序处理，并判断是否需要untreeify，吧处理的结果分别放在nextTable的i和i+n的位置上，移动完成，同样采用Unsafe.putObjectVolatile方法给table原位置复制fwd。

遍历过所有的节点以后就完成了复制工作，吧table指向nextTable，并更新sizeCtl为新数组大小的0.75，扩容完成。

#### 红黑树构造
如果链表结构中元素超过TREEIFY_THRESHOLD阈值，默认为8个，则把链表转化为红黑树，提高遍历查询效率。
```java
if(binCount!=0){
    if(binCount>=TREEIFY_THRESHOLD)
        treeifyBin(tab,i);
    if(oldVal!=null){
        return oldVal;
        break;
    }
}
```

树结构：
```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            tryPresize(n << 1);
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            synchronized (b) {
                if (tabAt(tab, index) == b) {
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        TreeNode<K,V> p =
                            new TreeNode<K,V>(e.hash, e.key, e.val,
                                              null, null);
                        if ((p.prev = tl) == null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}

```

可以看出，生成树节点的代码块是同步的，进入同步块之后，再次验证table中index位置元素是否被修改过。
1. 根据table中index位置Node链表，重新生成一个hd为头结点的TreeNode链表。
2. 根据hd头结点，生成TreeBin树结构，并把树结构的root节点写到table的index位置的内存中。

主要根据Node节点的hash值大小构建二叉树。

## 总结

1.8 在 1.7 的数据结构上做了大的改动，采用红黑树之后可以保证查询效率（`O(logn)`），甚至取消了 ReentrantLock 改为了 synchronized，这样可以看出在新版的 JDK 中对 synchronized 优化是很到位的。
