package com.crossoverjie.ConsumerAndProducer;

public class Clerk {
    private int product = -1;   //只持有一个产品 -1表示没有产品
    public synchronized  void setProduct(int product) throws InterruptedException{
        waitIfFull();   //看看店员有没有空间收产品  没有的话就稍后
        this.product = product;  //店员收货
        System.out.printf("生产者设定（%d）%n",this.product);
        notify();   //通知等待集合中的线程（例如消费者）

    }

    private synchronized  void waitIfFull() throws InterruptedException{
        while(this.product !=-1){
            wait();
        }
    }

    public synchronized  int getProduct() throws InterruptedException{
        waitIfEmpty();   //看看目前店员有没有货，没有的话就稍候
        int  p = this.product;  //准备交货
        this.product  = -1;    //表示货物被取走
        System.out.printf("消费者取走(%d)%n",p);
        notify();   //通知等待集合中的线程（例如生产者 ）
        return p;
    }

    private synchronized void waitIfEmpty() throws InterruptedException {
        while(this.product==-1){
            wait();
        }
    }
}

