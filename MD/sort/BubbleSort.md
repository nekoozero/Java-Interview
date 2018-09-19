# 冒泡排序

数据类型：

```java
public class RecordNode {
    public int key;
    public Object data;
    public RecordNode(int key,Object data){
        this.key = key;
        this.data = data;
    }
}
```

冒泡排序是基于交换的排序算法。

冒泡算法是两两比较待排序记录的关键字，如果次序相反则交换两个记录的位置，知道序列中的所有记录有序。若按升序排序，每趟将数据元素序列中的最大元素交换到最后的位置，就像气泡从水里冒出一样，其主要步骤如下：

1. 比较相邻的元素，如果前一个比后一个大，就把他们两个调换位置。
2. 对每一对相邻元素做同样的工作，从开始第一队到结尾的最后一对。这步做完后，最后的元素会是最大的数。
3. 针对所有的元素重复以上的步骤，除了最后一个。
4. 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。

![图片来自网络](https://images2017.cnblogs.com/blog/849589/201710/849589-20171015223238449-2146169197.gif)

代码实现：

```java
public void bubbleSort(){
    RecordNode p =  new RecordNode();
    boolean flag = true;          //一趟排序中是否进行了交换
    for(int i=0;i<len&&flag;i++){   //最多进行len-1次
        flag =false;
        for(int j=0;j<=len-i-1;j++){    //冒泡排序
            if(list[j+i].key<list[j].key){
              p=list[j];
              list[j]=list[j+1];
              list[j+1]=p;
              flag=true;
            }    
        }
    }
}
```

时间复杂度：
最好的情况下排序表已经有序，只进行一趟冒泡排序，这次操作中发生了n-2次的比较；最坏的情况下排序表逆序，需要进行n-1趟排序，在第i趟排序中比较次数为n-i、移动次数为3(n-i)，总的比较和移动次数为2(n<sup>2</sup>-n);一般情况下排序记录是随机序列，冒泡排序的时间复杂度为O(n<sup>2</sup>)。

空间复杂度：
冒泡排序仅用了一个辅助单元p，所以其空间复杂度为O(1)。

算法稳定性：
冒泡排序是一种稳定的排序算法。