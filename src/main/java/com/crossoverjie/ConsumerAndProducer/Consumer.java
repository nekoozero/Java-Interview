package com.crossoverjie.ConsumerAndProducer;

public class Consumer implements  Runnable {
    private Clerk clerk;
    public Consumer(Clerk clerk){
        this.clerk = clerk;
    }
    @Override
    public void run() {
        System.out.println("消费者开始消耗整数");
        for(int  i=1;i<=10;i++){      //消耗10次整数
            try{
                clerk.getProduct();  //从店员处取走产品
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
