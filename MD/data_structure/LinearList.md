<!-- MarkdownTOC -->

- [基本概念](#%E5%9F%BA%E6%9C%AC%E6%A6%82%E5%BF%B5)
- [顺序表](#%E9%A1%BA%E5%BA%8F%E8%A1%A8)
	- [特点](#%E7%89%B9%E7%82%B9)
- [链表](#%E9%93%BE%E8%A1%A8)
	- [单链表](#%E5%8D%95%E9%93%BE%E8%A1%A8)
	- [循环链表](#%E5%BE%AA%E7%8E%AF%E9%93%BE%E8%A1%A8)
	- [双向链表](#%E5%8F%8C%E5%90%91%E9%93%BE%E8%A1%A8)
- [比较](#%E6%AF%94%E8%BE%83)
- [总结](#%E6%80%BB%E7%BB%93)
- [Tips](#tips)
- [Solution](#solution)
	- [顺序表](#%E9%A1%BA%E5%BA%8F%E8%A1%A8-1)

<!-- /MarkdownTOC -->

<a id="%E5%9F%BA%E6%9C%AC%E6%A6%82%E5%BF%B5"></a>
# 基本概念

线性表是其组成元素间具有线性关系的一种线性结构，是由n个具有相同数据类型的数据元素a0、a1、a2……an-1构成的有限序列。

其中，数据元素ai可以是字母、整数、浮点数、对象或其他更复杂信息，i代表数据元素在线性表中的位序号（0 <= i < n）,n是线性表的元素个数，称为线性表的长度，当 n=0 时，线性表为空表。

线性表的java接口的实现方法主要有以下两种：
1. 基于顺序存储的实现
2. 基于链式存储的实现

<a id="%E9%A1%BA%E5%BA%8F%E8%A1%A8"></a>
# 顺序表

把线性表中的所有元素按照其逻辑顺序存储到计算机的内存单元中指定存储位置开始的一块*连续的存储空间*中，称为顺序表。顺序表用一组连续的内存单元依次存放数据元素，元素在内存中的物理存储次序和它们在线性表中的逻辑次序一致。

**LOC(ai)=LOC(a1)+(i-1)c**

LOC(a)函数是指 a 在物理内存中的位置，c 是指每个数据元素所占物理内存的大小。

计算一个元素地址所需的时间为常量，与顺序表的长度 n 无关；存储地址是数据元素位序号i的线性函数。因此，存取任何一个数据元素的时间复杂度为O(1)，顺序表是按照数据元素的位序号随机存储的结构。

<a id="%E7%89%B9%E7%82%B9"></a>
## 特点

- 在线性表中逻辑上相邻的元素在物理存储位置上也同样相同。
- 可按照数据元素的位序号进行随机存取。
- 进行插入、删除操作需要移动大量的数据元素。
- 需要进行存储空间的预分配，可能会造成空间浪费，但存储密度较高。

综上所述，顺序表居于较好的静态特性、较差的动态特性。

1. 顺序表利用元素的物理存储次序反应线性元素的逻辑关系，不需要额外的存储空间进行元素间关系的表达。顺序表是随机存储结构，存储元素 a 的时间复杂度为 O(1)，并且实现了线性表抽象数据类型所要求的基本操作。
2. 插入和删除操作的效率很低，没插入或删除一个数据元素，元素的移动次数较多，平均移动书序表中数据元素个数的议案，并且数组容量不可更改，存在因容量小二造成数据溢出或者容量过大造成内存资源浪费的问题。

<a id="%E9%93%BE%E8%A1%A8"></a>
# 链表

采用链式存储方式存储的线性表称为链表，链表使用若干*地址分散的存储单元*存储数据元素，逻辑上的相邻数据元素在物理地址上不一定相邻，必须采用附加信息表示数据元素之间的逻辑关系，因此链表的每一个节点不仅包含元素本身的信息-数据域，而且包含元素之间逻辑关系的信息，及逻辑上相邻节点地址的指针域。

<a id="%E5%8D%95%E9%93%BE%E8%A1%A8"></a>
## 单链表

单链表是指节点中只包含一个指针域的链表，指针域中存储着指向后继节点的指针。单链表的头指针是线性表的起始地址，是线性表中第一个数据元素的存储地址，可作为单链表的唯一标识。单链表的尾节点没有后继节点，所以其指针域值为 null。
为了是操作简便，在第一个节点之前增加头结点，单链表的头指针指向头结点，头结点的数据域不存放任何数据，指针域存放指向第一个节点的指针。空单链表的头指针 head 为 null 。

**单链表的节点的存储空间时在插入和删除过程中动态申请和释放的，不需要预先分配，从而避免了顺序表因存储空间不足需要扩充控件和复制元素的过程，避免了顺序表因容量过大造成内存资源浪费的为，提高了运行效率和存储空间的利用率。**

<a id="%E5%BE%AA%E7%8E%AF%E9%93%BE%E8%A1%A8"></a>
## 循环链表

循环链表与单链表的结构相似，只是将链表的首尾相连，即尾节点的指针域为指向头结点的指针，从而形成了一个环状的链表。
循环链表与单链表的操作算法基本一致，判定循环链表中的某各节点是否为尾节点的条件不是他的后继节点为空，而是他的后继节点是否为头结点。
在实现循环链表时可用头指针或尾指针或二者同时使用来标识循环链表，通常使用尾指针来进行标识，可简化某些操作。

<a id="%E5%8F%8C%E5%90%91%E9%93%BE%E8%A1%A8"></a>
## 双向链表

双向链表的节点具有两个指针域，一个指针指向前驱节点，一个指针指向后继节点。使得查找某个节点的前驱节点不需要从表头开始顺着链表依次进行查找，减少时间复杂度。其与单链表的不同之处主要在于进行插入和删除操作时每个节点需要修改两个指针域。（java 中的 LinkedList 就是双向链表结构）

<a id="%E6%AF%94%E8%BE%83"></a>
# 比较
||顺序表|链表|
|:---:|:---|:---|
|优点|（1）可进行高校随机存取<br>（2）存储密度高<br>（3）实现简单，便于使用|（1）灵活，可进行存储空间动态分配<br>（2）插入、删除效率高|
|缺点|（1）需要预先分配存储空间<br>（2）不便于进行插入和删除操作|（1）存储密度低<br>（2）不可按照位序号随机存取|

> 所以，从单个元素节点来看，链表是浪费空间的（因为需要额外的空间来存储下一个节点的指针），但从整个线性表来看，又是节省空间的，因为顺序表的数据节点可能并不是全部用到，100个结点可能只用到了3个的情况，就会造成浪费。


<a id="%E6%80%BB%E7%BB%93"></a>
# 总结

1. 线性表时期组成元素间具有线性关系的一种线性结构，其实现方式主要为基于顺序存储的时限和基于链式存储的实现。
2. 线性表的顺序存储结构称为顺序表，可用数组实现，可对数据元素进行随机存取，时间复杂度为 O(1)，再插入或删除数据元素时时间复杂度为 O(n)。
3. 线性表的链式存储结构称为链表，不能直接访问给定位置上的数据元素，必须从头结点开始沿着后继节点进行访问，时间复杂度为 O(n)。在插入或删除数据元素时不需要移动任何数据元素，只需要更改节点的指针域即可，时间复杂度为 O(1)。
4. 循环链表将链表的首尾相连，即尾节点的指针域为指向头结点的指针，从而形成了一个环状的链表。
5. 双向链表的节点具有两个指针域，一个指针指向前驱节点，一个指针指向后继节点，使得查找某个节点的前驱节点不需要从表头开始顺着链表一次进行查找，减小时间复杂度。 

<a id="tips"></a>
# Tips

- 线性表是一种**逻辑结构**，表示元素之间一对一的相邻关系。顺序表和链表是指**存储结构**，两者属于不同层面的概念，因此不要混淆。

<a id="solution"></a>
# Solution

<a id="%E9%A1%BA%E5%BA%8F%E8%A1%A8-1"></a>
## 顺序表

1.查找线性表中值最小的元素

```c
   value = L.data[0];
   int pos = 0;                           //假定 0 号元素的值最小
   for(int i = 1; i < L.length; i++) {
       if(L.data[i] < value) {
           value = L.data[i];
           pos = i;                       //这样 i 位置的元素就是值最小的元素
       }
   }
```

2.将顺序表中的元素倒置

思想：扫描顺序表中 L 中的前半部分元素，对于元素 L.data[i](0<=i<L.length/2),将其余后半部分对应元素L.data[L.length-i-1]进行交换。

```c
    void Reverse(Sqlist &L){
        Elemtype temp;                               //辅助变量
        for(int i; i < L.length/2; i++) {
            temp = L.data[i];
            L.data[i] = L.data[L.length-i-1];        //交换L.data[i]与L.data[L.length-i-1]
            L.data[L.length-i-1] = temp;
        }
    }
```

3.删除顺序表中所有值为x的数据元素

思想1：用 k 记录顺序表中 L 中不等于 x 的元素个数（即需要保存的元素个数），边扫描 L 边统计 k，并将不等于 x 的元素向前放置 k 位置上，最后修改 L 的长度。

```c
    void del_x_1(Sqlist &L,Elemtype x) {
    	int k = 0;                               //记录不等于k的元素个数
    	for(i = 0; i < L.length; i++) {
    		if(L.data[i] != x) {
    			L.data[k] = L.data[i];
    			k++;
    		}
    	}
    	L.length = k;                            //顺序表L的长度等于k
    }
``` 

思想2：用 k 记录顺序表 L 中等于 x 的元素个数，边扫描 L 边统计 k，把不等于 x 的数据元素向前移动 k 个位置，最后修改L的长度。

```c
    void del_x_2(Sqlist &L,Elemtype x) {
   	    int k = 0;                             //记录等于k的元素个数
   	    for(int i=0; i<L.length;i++) {
   	    	if(L.data[i] == x) {
                k++;
   	    	}else{
   	    		L.data[i-k]=L.data[i];
   	    	}
   	    	
   	    }
        L.length  = L.length-k;          //顺序表L的长度等于k
    }
```

思想3：设头尾两个指针（i=0,j=n）,从两端向中间移动，凡遇到最左端值为 x 的元素时，直接将最右端值非 x 的元素左移至值为 x 的数据元素位置，直到两指针相遇。但这种方法会改变原表中元素的相对位置。

4.从顺序表中删除其值在给定s与t之间的（包含s和t）所有元素。

思想：从前向后扫描顺序表L，用 k 记录下元素值在s到t之间元素的个数（初始时k=0）。对于当前扫描的元素，若其值不在s到t之间，则前移k个位置；否则执行k++。由于这样每个不在s到t之间的元素仅移动一次，所以算法效率更高。

```c
    bool Del_s_t(Sqlist &L,Elemtype s,Elemtype t) {
    	int i,k = 0;
    	if(L.length==0||s>=t) {
    		return false;
    	}
    	for(i = 0; i < L.length; i++) {
    		if(L.data[i]>=s&&L.data[i]<t) {
    			k++;
    		}else {
    			L.data[i-k] = L.data.[i];        //当前元素前移k个位置
    		}
    	}
    	L.length = L.length - k;
    	return true;
    }
```

5.从有序顺序表中删除所有值重复的元素，使表中的所有元素值均不同。

思想：有序顺序表，值相同的元素一定在连续的位置上，用类似于直接插入排序的思想，初始时将第一个元素看作非重复的有序表。之后以此判断后面的元素是否与前面非重复有序表的最后一个元素相同，如果相同则继续向后判断，如果不同则插入到前面的非重复有序表的最后。

```c
    bool Delete_Same(SeqList& L) {
    	if(L.length == 0) {
    		return false;
    	}
    	int i,j;
    	for(i = 0,j = 1;j < L.length;j++) {               //i存储第一个不相同的元素
            if(L.data[i]!=L.data[j]){
            	L.data[++i] = L.data[j];                  //查找到后将元素前移
            }
    	}
    	L.length = i + 1;
    	return true;
    }
```

6.将两个有序顺序表合并成一个新的有序顺序表，并由函数返回结果顺序表。

思想：按顺序不断取下两个顺序表表头较小的节点存入新的顺序表中，然后，哪个表中还有剩余，将剩下的部分加到新的顺序表后面。

```c
    bool Merge(SeqList A,SeqList B,SeqList &C) {
        int i=0,j=0,k=0;
        while(i>A.length||j>B.length) {                   //两两比较，小者存入结果表
            if(A.data[i]<=B.data[j]) {            
            	C.data[k++] = A.data[j++];
            }else{
            	C.data[k++] = B.data[j++]
            }
        }
        while(i<A.length){                                //还剩一个没有比较完的顺序表
            C.data[k++] = A.data[i++];
        }
        while(j<B.length){
            C.data[k++] = B.data[i++];
        }
        C.length = k+1;
        return true;
    }
```

7.一维数组A[m+n]中一次存放着两个线性表（a1,a2,a3……am）和(b1,b2,b3……bn)。将数组中两个顺序表的位置互换。

思想：将数组A全部元素原地逆置，再对前n个元素逆置和后m个元素逆置。


```c
    typedef int DataType;
    void Reverse(DataType A[],int left,int right,int arraySize) {    //逆置left到right位置的元素
    	if(left>=right||right>=arraySize) {
    		return;
    	}
        int mid = (left+right)/2;
        for(int i = 0;i<= mid-left;i++) {
        	DataType temp = A[left+1];
        	A[left+i] = A[left-i];
        	A[right-i] =temp;
        }
    }

    void Exchange(DataType A[],int m,int n,int arraySize) {
    	Reverse(A,0,m+n-1,arraySize);
    	Reverse(A,0,n-1,arraySize);
    	Reverse(A,n,m+n-1,arraySize);
    }

```
