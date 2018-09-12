# 基本概念

线性表是其组成元素间具有线性关系的一种线性结构，是由n个具有相同数据类型的数据元素a0、a1、a2……an-1构成的有限序列。

其中，数据元素ai可以是字母、整数、浮点数、对象或其他更复杂信息，i代表数据元素在线性表中的位序号（0 <= i < n）,n是线性表的元素个数，称为线性表的长度，当 n=0 时，线性表为空表。

线性表的java接口的实现方法主要有以下两种：
1. 基于顺序存储的实现
2. 基于链式存储的实现

# 顺序表

把线性表中的所有元素按照其逻辑顺序存储到计算机的内存单元中指定存储位置开始的一块*连续的存储空间*中，称为顺序表。顺序表用一组连续的内存单元依次存放数据元素，元素在内存中的物理存储次序和他们在线性表中的逻辑次序一致。

计算一个元素地址所需的时间为常量，与顺序表的长度 n 无关；存储地址是数据元素位序号i的线性函数。因此，存取任何一个数据元素的时间复杂度为O(1)，顺序表是按照数据元素的位序号随机存储的结构。

## 特点

- 在线性表中逻辑上相邻的元素在物理存储位置上也同样相同。
- 可按照数据元素的位序号进行随机存取。
- 进行插入、删除操作需要移动大量的数据元素。
- 需要进行存储空间的预分配，可能会造成空间浪费，但存储密度较高。

综上所述，顺序表居于较好的静态特性、较差的动态特性。

1. 顺序表利用元素的物理存储次序反应线性元素的逻辑关系，不需要额外的存储空间进行元素间关系的表达。顺序表是随机存储结构，存储元素 a 的时间复杂度为 O(1)，并且实现了线性表抽象数据类型所要求的基本操作。
2. 插入和删除操作的效率很低，没插入或删除一个数据元素，元素的移动次数较多，平均移动书序表中数据元素个数的议案，并且数组容量不可更改，存在因容量小二造成数据溢出或者容量过大造成内存资源浪费的问题。

# 链表

采用链式存储方式存储的线性表称为链表，链表使用若干*地址分散的存储单元*存储数据元素，逻辑上的相邻数据元素在物理地址上不一定相邻，必须采用附加信息表示数据元素之间的逻辑关系，因此链表的每一个节点不仅包含元素本身的信息-数据域，而且包含元素之间逻辑关系的信息，及逻辑上相邻节点地址的指针域。

## 单链表

单链表是指节点中只包含一个指针域的链表，指针域中存储着指向后继节点的指针。单链表的头指针是线性表的起始地址，是线性表中第一个数据元素的存储地址，可作为单链表的唯一标识。单链表的尾节点没有后继节点，所以其指针域值为 null。
为了是操作简便，在第一个节点之前增加头结点，单链表的头指针指向头结点，头结点的数据域不存放任何数据，指针域存放指向第一个节点的指针。空单链表的头指针 head 为 null 。

单链表的节点的存储空间时在插入和删除过程中动态申请和释放的，不需要预先分配，从而避免了顺序表因存储空间不足需要扩充控件和复制元素的过程，避免了顺序表因容量过大造成内存资源浪费的为，提高了运行效率和存储空间的利用率。

## 循环链表

循环链表与单链表的结构相似，只是将链表的首尾相连，即尾节点的指针域为指向头结点的指针，从而形成了一个环状的链表。
循环链表与单链表的操作算法基本一致，判定循环链表中的某各节点是否为尾节点的条件不是他的后继节点为空，而是他的后继节点是否为头结点。
在实现循环链表时可用头指针或尾指针或二者同时使用来标识循环链表，通常使用尾指针来进行标识，可简化某些操作。

## 双向链表

双向链表的节点具有两个指针域，一个指针指向前驱节点，一个指针指向后继节点。使得查找某个节点的前驱节点不需要从表头开始顺着链表依次进行查找，减少时间复杂度。其与单链表的不同之处主要在于进行插入和删除操作时每个节点需要修改两个指针域。（java 中的 LinkedList 就是双向链表结构）

# 比较
||顺序表|链表|
|:---:|:---|:---|
|优点|（1）可进行高校随机存取<br>（2）存储密度高<br>（3）实现简单，便于使用|（1）灵活，可进行存储空间动态分配<br>（2）插入、删除效率高|
|缺点|（1）需要预先分配存储空间<br>（2）不便于进行插入和删除操作|（1）存储密度低<br>（2）不可按照位序号随机存取|

# 总结

1. 线性表时期组成元素间具有线性关系的一种线性结构，其实现方式主要为基于顺序存储的时限和基于链式存储的实现。
2. 线性表的顺序存储结构称为顺序表，可用数组实现，可对数据元素进行随机存取，时间复杂度为 O(1)，再插入或删除数据元素时时间复杂度为 O(n)。
3. 线性表的链式存储结构称为链表，不能直接访问给定位置上的数据元素，必须从头结点开始沿着后继节点进行访问，时间复杂度为 O(n)。在插入或删除数据元素时不需要移动任何数据元素，只需要更改节点的指针域即可，时间复杂度为 O(1)。
4. 循环链表将链表的首尾相连，即尾节点的指针域为指向头结点的指针，从而形成了一个环状的链表。
5. 双向链表的节点具有两个指针域，一个指针指向前驱节点，一个指针指向后继节点，使得查找某个节点的前驱节点不需要从表头开始顺着链表一次进行查找，减小时间复杂度。 
