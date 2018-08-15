package com.crossoverjie.ConsumerAndProducer;

public class Producer implements Runnable {
    private Clerk clerk;
    public Producer(Clerk clerk){
        this.clerk = clerk;
    }
    @Override
    public void run() {
        System.out.println("生产开始生产整数");
        for (int product = 1 ;product<=10;product++){
            try{
                clerk.setProduct(product);   //将产品交给店员
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }
    }
}
