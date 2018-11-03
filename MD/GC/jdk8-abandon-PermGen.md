<!-- MarkdownTOC -->

- [jvm 运行内存](#jvm-%E8%BF%90%E8%A1%8C%E5%86%85%E5%AD%98)
	- [关于永久代](#%E5%85%B3%E4%BA%8E%E6%B0%B8%E4%B9%85%E4%BB%A3)
- [jdk8 永久代的废弃](#jdk8-%E6%B0%B8%E4%B9%85%E4%BB%A3%E7%9A%84%E5%BA%9F%E5%BC%83)
- [什么移除永久代（PermGen）](#%E4%BB%80%E4%B9%88%E7%A7%BB%E9%99%A4%E6%B0%B8%E4%B9%85%E4%BB%A3%EF%BC%88permgen%EF%BC%89)
	- [官方说明](#%E5%AE%98%E6%96%B9%E8%AF%B4%E6%98%8E)
	- [现实问题](#%E7%8E%B0%E5%AE%9E%E9%97%AE%E9%A2%98)
- [什么是元空间（Metaspace）](#%E4%BB%80%E4%B9%88%E6%98%AF%E5%85%83%E7%A9%BA%E9%97%B4%EF%BC%88metaspace%EF%BC%89)
	- [常用配置参数](#%E5%B8%B8%E7%94%A8%E9%85%8D%E7%BD%AE%E5%8F%82%E6%95%B0)
- [测试](#%E6%B5%8B%E8%AF%95)
	- [测试字符串常量](#%E6%B5%8B%E8%AF%95%E5%AD%97%E7%AC%A6%E4%B8%B2%E5%B8%B8%E9%87%8F)
	- [测试元空间溢出](#%E6%B5%8B%E8%AF%95%E5%85%83%E7%A9%BA%E9%97%B4%E6%BA%A2%E5%87%BA)

<!-- /MarkdownTOC -->

<a id="jvm-%E8%BF%90%E8%A1%8C%E5%86%85%E5%AD%98"></a>
# jvm 运行内存

需要先明白一个概念：jvm 不仅仅是指我们常用的 jdk 里的 jvm，不能将他与 Oracle 公司的 HotSpot 虚拟机（这是我们常常“口中说的 jvm”）等同看待，HotSpot 只是 jvm 的一种实现，比较著名的有 Oralce-Sun Hotspot, Oralce JRockit, IBM J9, Taobao JVM 等等虚拟机，他们各自有各自对 jvm 规范的实现，所以这里指的 jvm 运行内存指的是 jvm 的规范，而不是 HotSpot 虚拟机的运行内存。下面是 jvm 规范的图。

![图片来自网络](https://images0.cnblogs.com/i/288799/201405/281726404166686.jpg)

下面是 HotSpot 的图：

![图片来自网络](https://images2015.cnblogs.com/blog/584866/201704/584866-20170426175411428-34722603.png)

我们所说的永久代也就是 HotSpot 中的方法区（jvm 规范中称为方法区），《Java虚拟机规范》只是规定了有方法区这个概念和它的作用，并没有规定如何去实现它，在其他 jvm 中不存在永久代。

<a id="%E5%85%B3%E4%BA%8E%E6%B0%B8%E4%B9%85%E4%BB%A3"></a>
## 关于永久代

持久代中包含了虚拟机中所有可通过反射获取到的数据，比如Class和Method对象。不同的Java虚拟机之间可能会进行类共享，因此持久代又分为只读区和读写区。

JVM用于描述应用程序中用到的类和方法的元数据也存储在持久代中。JVM运行时会用到多少持久代的空间取决于应用程序用到了多少类。除此之外，Java SE库中的类和方法也都存储在这里。

如果JVM发现有的类已经不再需要了，它会去回收（卸载）这些类，将它们的空间释放出来给其它类使用。Full GC会进行持久代的回收。

- JVM中类的元数据在Java堆中的存储区域。
- Java类对应的HotSpot虚拟机中的内部表示也存储在这里。
- 类的层级信息，字段，名字。
- 方法的编译信息及字节码。
- 变量
- 常量池和符号解析

<a id="jdk8-%E6%B0%B8%E4%B9%85%E4%BB%A3%E7%9A%84%E5%BA%9F%E5%BC%83"></a>
# jdk8 永久代的废弃

jdk8 永久代变化：

![图片来自网络](https://images2015.cnblogs.com/blog/584866/201704/584866-20170426154633834-741444326.jpg)

1. 新生代：Eden + From Survivor + To Survivor
2. 老年代：Olden
3. <strong>永久代（方法区的实现）：PermGen -----> 替换为 Metaspace</strong>
4. 方法区移至Metaspace，字符串常量移至Java Heap

<a id="%E4%BB%80%E4%B9%88%E7%A7%BB%E9%99%A4%E6%B0%B8%E4%B9%85%E4%BB%A3%EF%BC%88permgen%EF%BC%89"></a>
# 什么移除永久代（PermGen）

<a id="%E5%AE%98%E6%96%B9%E8%AF%B4%E6%98%8E"></a>
## 官方说明

>This is part of the JRockit and Hotspot convergence effort. JRockit customers do not need to configure the permanent generation (since JRockit does not have a permanent generation) and are accustomed to not configuring the permanent generation.

即：<span style="color:red">这是JRockit和Hotspot趋同努力的一部分。JRockit客户不需要配置永久代(因为JRockit没有永久代)，并且习惯于不配置永久代。</span>

<a id="%E7%8E%B0%E5%AE%9E%E9%97%AE%E9%A2%98"></a>
## 现实问题

由于永久代内存经常不够用或发生内存泄露，爆出异常`java.lang.OutOfMemoryError: PermGen`。

<a id="%E4%BB%80%E4%B9%88%E6%98%AF%E5%85%83%E7%A9%BA%E9%97%B4%EF%BC%88metaspace%EF%BC%89"></a>
# 什么是元空间（Metaspace）

<strong>元空间是方法区在 HotSpot jvm 中的实现，方法区主要用于存储类的信息、常量池、方法数据、方法代码等。方法区逻辑上属于堆的一部分，但是为了与对进行区分，通常又叫“非堆”。</strong>

元空间的本质和永久代很相似，都是对 JVM 规范中方法区的实现。不过，元空间与永久代之间最大的区别在于：<strong>`元空间并不在虚拟机中，而是使用的是本地内存。`</strong>理论上取决于 32位/64位系统可虚拟的内存大小。可见也不是无限制的，需要配置参数。

<a id="%E5%B8%B8%E7%94%A8%E9%85%8D%E7%BD%AE%E5%8F%82%E6%95%B0"></a>
## 常用配置参数

1. MetaspaceSize：
初始化的Metaspace大小，控制元空间发生GC的阈值。GC后，动态增加或降低MetaspaceSize。在默认情况下，这个值大小根据不同的平台在12M到20M浮动。使用Java -XX:+PrintFlagsInitial命令查看本机的初始化参数

2. MaxMetaspaceSize：
限制Metaspace增长的上限，防止因为某些情况导致Metaspace无限的使用本地内存，影响到其他程序。在本机上该参数的默认值为4294967295B（大约4096MB）。

3. MinMetaspaceFreeRatio：
当进行过Metaspace GC之后，会计算当前Metaspace的空闲空间比，如果空闲比小于这个参数（即实际非空闲占比过大，内存不够用），那么虚拟机将增长Metaspace的大小。默认值为40，也就是40%。设置该参数可以控制Metaspace的增长的速度，太小的值会导致Metaspace增长的缓慢，Metaspace的使用逐渐趋于饱和，可能会影响之后类的加载。而太大的值会导致Metaspace增长的过快，浪费内存。

4. MaxMetasaceFreeRatio：
当进行过Metaspace GC之后， 会计算当前Metaspace的空闲空间比，如果空闲比大于这个参数，那么虚拟机会释放Metaspace的部分空间。默认值为70，也就是70%。

5. MaxMetaspaceExpansion：
Metaspace增长时的最大幅度。在本机上该参数的默认值为5452592B（大约为5MB）。

6. MinMetaspaceExpansion：
Metaspace增长时的最小幅度。

<a id="%E6%B5%8B%E8%AF%95"></a>
# 测试

<a id="%E6%B5%8B%E8%AF%95%E5%AD%97%E7%AC%A6%E4%B8%B2%E5%B8%B8%E9%87%8F"></a>
## 测试字符串常量

```java
 public class StringOomMock {
    static String  base = "string";
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        for (int i=0;i< Integer.MAX_VALUE;i++){
            String str = base + base;
            base = str;
            list.add(str.intern());
        }
    }
 }
```
配置运行参数如下：

`-Xms20m -Xmx20m -XX:PermSize=8m -XX:MaxPermSize=8m`，运行结果如图所示：

![](https://images2015.cnblogs.com/blog/584866/201704/584866-20170426184206881-69720212.png)

可见：
1. 字符串常量由永久代转移到堆中。
2. 永久代已不存在，PermSize MaxPermSize参数已移除。

<a id="%E6%B5%8B%E8%AF%95%E5%85%83%E7%A9%BA%E9%97%B4%E6%BA%A2%E5%87%BA"></a>
## 测试元空间溢出

```java
package jdk8;
import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class OOMTest {  
    public static void main(String[] args) {  
        try {  
            //准备url  
            URL url = new File("D:/58workplace/11study/src/main/java/jdk8").toURI().toURL();  
            URL[] urls = {url};  
            //获取有关类型加载的JMX接口  
            ClassLoadingMXBean loadingBean = ManagementFactory.getClassLoadingMXBean();  
            //用于缓存类加载器  
            List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();  
            while (true) {  
                //加载类型并缓存类加载器实例  
                ClassLoader classLoader = new URLClassLoader(urls);  
                classLoaders.add(classLoader);  
                classLoader.loadClass("ClassA");  
                //显示数量信息（共加载过的类型数目，当前还有效的类型数目，已经被卸载的类型数目）  
                System.out.println("total: " + loadingBean.getTotalLoadedClassCount());  
                System.out.println("active: " + loadingBean.getLoadedClassCount());  
                System.out.println("unloaded: " + loadingBean.getUnloadedClassCount());  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  
```

为了快速溢出，设置参数：-XX:MetaspaceSize=8m -XX:MaxMetaspaceSize=80m，运行结果如下：

![](https://images2015.cnblogs.com/blog/584866/201704/584866-20170427095449428-1484864673.png)

上图证实了，我们的JDK8中类加载（方法区的功能）已经不在永久代PerGem中了，而是Metaspace中。