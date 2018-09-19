# 二叉树

1. 普通二叉树
二叉树是特殊的有序树，它也是由 n 个结点构成的有限集合。当 n=0 时称为空二叉树。二叉树的每个节点最多只有两个子树，子树也为二叉树，互不相交且有左右之分，分别称为左二叉树和右二叉树。

二叉树也是递归定义的，在书中定义的度，层次等属于同样也适用于二叉树。

2. 满二叉树
满二叉树是特殊的二叉树，他要求除叶结点外的其他结点都具有两棵子树，并且所有的叶结点都在同一层上。

3. 完全二叉树
完全二叉树是特殊的二叉树，若二叉树具有 n 个结点，他要求 n 个结点与满二叉树的前 n 个结点将拥有完全相同的逻辑结构。

![图片来自网络](https://gss1.bdstatic.com/9vo3dSag_xI4khGkpoWK1HF6hhy/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=8d434f8b349b033b3885f48874a75db6/6609c93d70cf3bc7c6549f63d100baa1cc112aec.jpg)

## 性质

性质1：二叉树中第 i 层的结点数最多为 2<sup>i</sup>。
性质2：深度为 h 的二叉树最多有 2<sup>h</sup>-1个结点。
性质3：若二叉树的叶结点的个数为 n，度为 2 的结点个数为 m，有 n=m+1。
性质4：具有 n 个结点的完全二叉树，其深度为 log<sub>n</sub> +1或者log<sub>2</sub><sup>n+1</sup>。
性质5：具有 n 个结点的完全二叉树，从根结点开始自上而下、从左向右对结点从 0 开始编号。对于任意一个编号为 i 的结点：
    - 若 i=0,结点为根结点，没有父结点；若 i>0,则父结点的编号为 （i-1）/2。
    - 若 2i+1>=n,该结点无左孩子，否则左孩子结点的编号为 2i+1。
    - 若 2i+2>=n,该结点无右孩子，否则右孩子结点的编号为 2i+2。

## 存储结构

### 顺序存储结构

二叉树的顺序存储结构是指将二叉树的各个节点存放在一组地址连续的存储单元中，所有结点按节点序号进行顺序存储。因为二叉树为非线性结构，所以必须先将二叉树的结点排成线性序列再进行存储，实际上是对二叉树先进行了一次层次遍历。二叉树的各节点之间的逻辑关系由节点在线性序列中的相对位置确定。

为了存储非完全二叉树，需要在树中添加徐节点使其成为完全二叉树后进行存储，这样又会造成存储空间的浪费。

![图片来自网络](http://s3.51cto.com/wyfs02/M02/6E/04/wKiom1VxE9ay_VjbAADLEAsJrFA338.gif)

### 链式存储结构

二叉树的链式存储结构是指将二叉树的各个节点随机存放在存储空间中，二叉树的各个节点的逻辑关系由指针确定。每个节点至少要有两条链分别连接左右还自己诶点才能表达二叉树的层次关系。

根据指针域个数的不同，二叉树的链式存储结构由分一下两种：

1. 二叉树链式存储结构
   二叉树的每个节点设置两个指针域和一个数据与。数据域中存放节点的值，指针域中存放左右孩子结点的存储地址。
   采用二叉链表存储二叉树，每个节点只存储到了其孩子节点的单向关系，没有存储到其父节点的关系，因此要获得父节点将花费较多的时间，需要从根结点开始在二叉树中进行查找，所花费的时间是遍历部分二叉树的时间，且与查找节点所处的位置有关
2. 三叉链式存储结构
   二叉树的每个节点设置3个指针域和一个数据域。数据域中存放节点的值，指针域中存放左右节点和父节点的存储地址

   lchild->data->rchild  二叉链表节点
   parent->lchild->data->rchild  三叉链表节点

两种链式存储结构各有优缺点，二叉链式存储结构空间利用率高，而三叉链式存储结构既便于查找孩子结点，又便于查找父结点。在实际应用中，二叉链式结构存储结构更加常用。

## 遍历方法

### 二叉树的遍历方法

1. 层次遍历
自上而下、从左到右访问每层的节点。
2. 先序遍历
先访问根节点，在先序遍历左子树，最后先序遍历右子树。
3. 中序遍历
先中序遍历左子树，在访问根节点，最后中序遍历右子树。
4. 后序遍历
先后序遍历左子树，最后遍历右子树，最后访问根节点。


仅以先序遍历为例子：

1. 二叉树遍历操作实现的递归算法

```java
public void preOrder(BiTreeBode root){
    System.out.println(root.data+" ");  //访问根节点
    preOrder(root.lchild);              //先序遍历左子树
    preOrder(root.rchild);              //先序遍历右子树
}
```

2. 二叉树遍历操作实现的非递归算法

二叉树遍历操作的递归算法结构简洁，亦易于实现，但是在时间上的开销很大，运行效率低，为了解决这个问题，可以将递归算法转换为非递归算法，转换方式有以下两种：

- 使用临时遍历保存中间结果，用循环遍历结构代替递归过程；
- 利用栈保存中间结果

二叉树遍历操作实现的非递归算法利用栈结构通过回溯访问二叉树的每个节点。

- 先序遍历

现需比那里从二叉树的根节点触发，沿着该节点的左子树向下搜索，每遇到一个节点先访问该节点，并将该节点的右子树入栈。先序遍历左子树完成后再从栈顶弹出有字数的根节点，然后采用相同的方法遍历右子树，知道二叉树的所有结点都被访问。其主要步骤如下：
1. 将二叉树的根节点入栈。
2. 若栈非空，将节点从栈中弹出并访问。
3. 依次访问当前节点的左孩子节点，并将当前节点的有孩子节点入栈。
4. 重复步骤2,3.

先序遍历：
```java
public void preOrder2() throws Exception{
    BiTreeNode p =root;
    if(p!=null){
        LinkStack s= new LinkStack();     //构造存储节点的栈
        s.push(p);
        while(!s.isEmpty()){
            p=(BiTreeNode)s.pop();
            System.out.println(p.data+" ");   //访问当前节点
            while(p!=null){
                if(p.lchild!=null)            //访问左孩子结点
                    System.out.println(p.lchild.data+" ");
                if(p.rchild!=null)            //将右孩子结点入栈
                    s.push(p.rchild);
                p=p.lchild;
            }
        }
    }
}
```

- 层次遍历

层次遍历操作是从根节点触发，自上而下，从左到右依次遍历每层的节点，可以利用队列先进先出的特性进行实现。先将根节点入队，然后将队首节点出队并访问，都将其孩子结点依次入队：

1. 将根节点入队。
2. 若队非空，取出队首节点并访问，将队首节点的孩子结点入队。
3. 重复执行步骤2直到队为空。
层次遍历

```java
public void order() throws Exception{
    BiTreeNode p = root;
    while(p!=null){
        LinkQueue q = new LinkQueue();
        q.offer(p);
        while(!q.isEmpty()){
            p = (BiTreeNode)q.poll();
            System.out.println(p.data+" ");
            if(p.lchild!=null)
                q.offer(p.lchild);
            if(p.rchild!=null)
                q.offer(p.rchild);
        }
    }
}
```

对于有n个结点的二叉树，因为每个节点都只访问一次，所以算法时间复杂度为O(n)。
