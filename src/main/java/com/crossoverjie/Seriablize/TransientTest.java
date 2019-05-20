package com.crossoverjie.Seriablize;

import com.crossoverjie.utils.User;

import java.io.*;

public class TransientTest {
    public static void main(String[] args) {
        User user  = new User();
        user.setUsername("qxnekoo");
        user.setPassword("2222");
        int i  =30;
        System.out.println(i>>1);

        System.out.println("序列化之前：");
        System.out.println("username:" +user.getUsername());
        System.out.println("password:" +user.getPassword());

        try{
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("C:/user.txt"));
            os.writeObject(user);
            os.flush();
            os.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        try{
            ObjectInputStream is = new ObjectInputStream(new FileInputStream("C:/user.txt"));
            user = (User) is.readObject();
            is.close();
            System.out.println("序列化之后");
            System.out.println("username:"+user.getUsername());
            System.out.println("password:"+user.getPassword());
            //System.out.println("sss:"+user.sss);
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}










