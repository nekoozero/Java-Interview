**更多 HashMap 与 ConcurrentHashMap 相关请查看[这里](https://crossoverjie.top/2018/07/23/java-senior/ConcurrentHashMap/)。**

# HashMap 底层分析


![](https://ws2.sinaimg.cn/large/006tNc79gy1fn84b0ftj4j30eb0560sv.jpg)

如图所示，HashMap 底层是基于数组和链表实现的。其中有两个重要的参数：

- 容量
- 负载因子

容量的默认大小是 16(容量大小都是2的幂)，负载因子是 0.75，当 `HashMap` 的 `size > 16*0.75` 时就会发生扩容(容量和负载因子都可以自由调整)。

举个栗子：当程序是```Map map = new HashMap(8);```时，map的容量并不是8，因为size要满足```8<size*0.75```,而且还要是2的幂,所以容量就应该为16。

## put 方法
```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);    ------（1）
}
 
// 第三个参数 onlyIfAbsent 如果是 true，那么只有在不存在该 key 时才会进行 put 操作
// 第四个参数 evict 我们这里不关心
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 第一次 put 值的时候，会触发下面的 resize()，类似 java7 的第一次 put 也要初始化数组长度
    // 第一次 resize 和后续的扩容有些不一样，因为这次是数组从 null 初始化到默认的 16 或自定义的初始容量
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 找到具体的数组下标，如果此位置没有值，那么直接初始化一下 Node 并放置在这个位置就可以了
    if ((p = tab[i = (n - 1) & hash]) == null)       ------（2）
        tab[i] = newNode(hash, key, value, null);
 
    else {// 数组该位置有数据
        Node<K,V> e; K k;
        // 首先，判断该位置的第一个数据和我们要插入的数据，key 是不是"相等"，如果是，取出这个节点
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 如果该节点是代表红黑树的节点，调用红黑树的插值方法，本文不展开说红黑树
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 到这里，说明数组该位置上是一个链表
            for (int binCount = 0; ; ++binCount) {
                // 插入到链表的最后面(Java7 是插入到链表的最前面)
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // TREEIFY_THRESHOLD 为 8，所以，如果新插入的值是链表中的第 9 个
                    // 会触发下面的 treeifyBin，也就是将链表转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                // 如果在该链表中找到了"相等"的 key(== 或 equals)
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    // 此时 break，那么 e 为链表中[与要插入的新值的 key "相等"]的 node
                    break;
                p = e;
            }
        }
        // e!=null 说明存在旧值的key与要插入的key"相等"
        // 对于我们分析的put操作，下面这个 if 其实就是进行 "值覆盖"，然后返回旧值
        if (e != null) {
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    // 如果 HashMap 由于新插入这个值导致 size 已经超过了阈值，需要进行扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

java7中使用了Entry来代表HashMap中的数据节点，java8中使用Node，基本没有区别，都是key，value，hash和next这四个属性，不过Node只能用于链表的情况，红黑苏的情况需要使用TreeNode。java7是先扩容后插入数值的，java8先插入值再扩容，不过这不重要。

jdk8中扩容的时候不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”。比如16扩展32，oldCap=16。

这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。有一点注意区别，JDK1.7中rehash的时候，旧链表迁移新链表的时候，如果在新表的数组索引位置相同，则链表元素会倒置，JDK1.8不会倒置。

（1）首先会将传入的 Key 做 `hash` 运算计算出 hashcode，纯粹的数学计算（jdk1.8）。
```
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);  
}
```
解读：key的hash值右移16位，在于其做异或运算（相同则为0，不同则为1），得到参与计算哈希桶位置的最终hash值。
（2）然后根据数组长度取模计算出在数组中的 index 下标。
```
static int indexFor(int h, int length) {
        return h & (length-1);    //与运算，length是table的容量，为2的幂
}
```

对于HashMap的table而言，数据分布需要均匀，不能太紧也不能太松，太紧会导致查询速度变慢，太松则浪费空间。计算hash之后，要保证table元素分布均匀。由于在计算中位运算比取模运算效率高的多，所以 HashMap 规定数组的长度为 `2^n` 。这样用 `2^n - 1` （二进制为全1，做与运算下来，只要计算的hash值最后几位相对均匀，得到的index分布也就均匀）做位运算与取模效果一致，并且效率还要高出许多。

由于数组的长度有限，所以难免会出现不同的 Key 通过运算得到的 index 相同（发生了碰撞），这种情况可以利用链表（链表法/拉链法，碰撞还可以用开放地址法解决）来解决，HashMap 会在 `table[index]`处形成链表，采用头插法将数据插入到链表中。jdk8中当链表的长度超过阈值8时，链表会被转化为红黑树。

## get 方法
（1）计算key的hash值，根据hash值找到对应数组下标：hash&（lengh-1）。
（2）判断数组该位置处的元素是否刚好就是我们要找的，如果不是，走第三步。
（3）判断该元素类型是否是TreeNode，如果是，用红黑树的方法取数据，如果不是，走第四步。
（4）遍历链表，知道找到相等的key，通过`key.equals(k)`

```
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```

```
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // 判断第一个节点是不是就是需要的
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            // 判断是否是红黑树
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
 
            // 链表遍历
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

## 遍历方式（jdk1.7）


```java
 Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Integer> next = entryIterator.next();
            System.out.println("key=" + next.getKey() + " value=" + next.getValue());
        }
```

```java
Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            System.out.println("key=" + key + " value=" + map.get(key));

        }
```

```java
map.forEach((key,value)->{
    System.out.println("key=" + key + " value=" + value);
});
```

**强烈建议**使用第一种 EntrySet 进行遍历。

第一种可以把 key value 同时取出，第二种还得需要通过 key 取一次 value，效率较低, 第三种需要 `JDK1.8` 以上，通过外层遍历 table，内层遍历链表或红黑树。 


## notice

在并发环境下使用 `HashMap` 容易出现死循环。

并发场景发生扩容，调用 `resize()` 方法里的 `rehash()` 时，容易出现环形链表。这样当获取一个不存在的 `key` 时，计算出的 `index` 正好是环形链表的下标时就会出现死循环。

![](https://ws2.sinaimg.cn/large/006tNc79gy1fn85u0a0d9j30n20ii0tp.jpg)

> 所以 HashMap 只能在单线程中使用，并且尽量的预设容量，尽可能的减少扩容。

在 `JDK1.8` 中对 `HashMap` 进行了优化：
当 `hash` 碰撞之后写入链表的长度超过了阈值(默认为8)，链表将会转换为**红黑树**。

假设 `hash` 冲突非常严重，一个数组后面接了很长的链表，此时重新的时间复杂度就是 `O(n)` 。

如果是红黑树，时间复杂度就是 `O(logn)` 。

大大提高了查询效率。

多线程场景下推荐使用 ConcurrentHashMap。

## jdk1.7与jdk1.8

|不同|JDK1.7|JDK1.8|
|:---:|:---:|:---:|
|存储结构|数组+链表|数组+链表+红黑树|
|初始化方式|单独函数：inflateTable()|直接继承到了扩容函数resize()中|
|hash值计算方式|扰动处理=9次扰动=4次位运算+5次异或运算|扰动处理=2次扰动=1次位运算+1次异或运算|
|存放数据的规则|头插法（先讲原位置的数据移到后一位，再插入数据到该位置）|尾插法（直接插入到链表尾部、/红黑树）|
|扩容后存储位置的计算法方式|全部按照原来方法进行计算（即hashCode->>扰动函数->>(h&length-1)）|按照扩容后的规律计算（即扩容后的位置=原位置or原位置+旧容量）|

## 为什么HashMap中的String、Integer这样的包装类适合作为k？

String、Integer等包装类的特性能够保证Hash值的不可更改性和九三准确性，能够有效的减少Hash碰撞的几率。
1. 都是final类型，即不可变性，保证key的不可更改性，不会存在获取hash值不同的情况。
2. 内部已重写了`equals()`、`hashCode()`等方法，遵守了HashMap内部的规范，不容易出现Hash值计算错误的情况；

## 如果让自己的Object作为k该怎么办？

重写`hashCode()`和`equals()`方法
1. 重写`hashCode()`是因为需要计算存储数据的存储位置，需要注意不要试图从散列码计算中排除掉一个对象的关键部分来提高性能，这样虽然能更快但可能会导致更多的Hash碰撞；
2. 重写`equals()`方法，需要遵守自反性、对称性、传递性、一致性以及对于任何非null的引用值x，x.equals(null)必须返回false的这几个特性，目的是为了保证key在哈希表中的唯一性。
