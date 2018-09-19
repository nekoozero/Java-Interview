# 快速排序

快速排序也属于交换排序的一类。

快速排序的基本思想：通过一趟排序将待排记录分隔成独立的两部分，其中一部分记录的关键字均比另一部分的关键字小，则可分别对这两部分记录继续进行排序(递归)，已达到整个序列有序。

基本步骤：
1. 设置两个变量low、high,分别表示待排序序列的起始下标和终止下标。
2. 设置变量p=list[low]。
3. 从下标为high的位置从后向前依次搜索，当找到第1个比p的关键字值小的记录时将该数据移动到下表为low的位置上，low加1。
4. 从下标为low的位置从前向后依次搜索，当找到第1个比p的关键字值大的记录时将该数据移动到小标为high的位置上，high-1.
5. 重复步骤（3）和（4），直到high=low位置。
6. list[low]=p。

![图片来自网络](https://images2017.cnblogs.com/blog/849589/201710/849589-20171015230936371-1413523412.gif)

具体代码：

```java
public void qSort(int low,int high){
    if(low<high){
        int p=Partition(low,high);          //将排序表分为两个部分，返回支点位置
        qSort(low,p-1);             //递归对两个部分进行快速排序
        qSort(p+1,high);
    }
}

public int Partiotion(int low,int high){
    RecordNode p =list[low];                   //第一个元素作为支点
    while(low<high){                           //从顺序表的两端交替扫描
        while(low<high&&list[high].key>p.key){
            high--;
        }
        if(low<high){
            list[low] = list[high];
            low++;
        }
        while(low<high&&list[low].key<p.key){
            low++
        }
        if(low<high){
            list[high]=list[low];
            high--;
        }
    }
    list[low]=p;
    return low;
}
```

时间复杂度：
快速排序的执行时间与数据元素序列的初始排列以及基准值的选取有关。最坏情况下待排序序列基本有序，每次划分只能得到一个字序列，等同于冒泡排序，时间复杂度为O(n<sup>2</sup>),一般来说，对于具有n条记录的序列来说，一次划分需要进行n次关键字的比较，其时间复杂度为O(n)=O(nlog<sub>2</sub>n);

快速排序的基准值的选择方法有很多种，可以选取序列的中间值等，但由于数据元素序列的初始排列是随机的，不管如何选择基准值总会存在最坏情况。总之，当n较大并且数据元素序列随机排列时快速排序是快速的；当n很小或者基准值选取不合适时快速排序较慢。

空间复杂度：
快速排序需要额外存储空间来实现递归，递归调用的指针的参数都要存放到栈中。快速排序的递归过程可用递归树来表示。最坏情况下树为单支树，高度为O(n)，其空间复杂度为O(n)。若划分较为均匀，二叉树的高度为O(log<sub>2</sub>n),其空间复杂度也为O(log<sub>2</sub>n)。

算法稳定性：

快速排序是一种不稳定的排序算法。