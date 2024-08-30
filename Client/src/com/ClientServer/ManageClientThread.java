package com.ClientServer;

import java.util.HashMap;

public class ManageClientThread {
    private static HashMap<Integer,ClientThread> hashMap=new HashMap<>();
    public static void addThread(int id,ClientThread clientThread){
        hashMap.put(id,clientThread);
    }
    public static void removeThread(int id){
        hashMap.remove(id);
    }
    public static ClientThread getThread(int id){
        return hashMap.get(id);
    }
}
