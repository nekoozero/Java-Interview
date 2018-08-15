package com.crossoverjie.ConsumerAndProducer;

public class ProducerConsumerDemo {
    public static void main(String arg[]){
        Clerk clerk = new Clerk();
        new Thread(new Producer(clerk)).start();
        new Thread(new Consumer(clerk)).start();
    }
}
