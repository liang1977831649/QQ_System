package com.QQServer;

import java.net.Socket;
import java.util.HashMap;

public class ManageConnectThread {
    private static HashMap<Integer,QQClientConnectServerThread> hashMap=new HashMap<>();

    public static HashMap<Integer, QQClientConnectServerThread> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<Integer, QQClientConnectServerThread> hashMap) {
        this.hashMap = hashMap;
    }

    public static void addHashMapSocket(int id,QQClientConnectServerThread qqClientConnectServerThread){
        hashMap.put(id,qqClientConnectServerThread);
    }
    public static QQClientConnectServerThread getHashMapSocket(int id){
        return hashMap.get(id);
    }
    public static void RemoveHashMapSocket(int id){
        hashMap.remove(id);
    }
    public static void ClearThread(){
        hashMap.clear();
    }
    public static void ReplaceThread(int id, QQClientConnectServerThread qqClientConnectServerThread){
        hashMap.put(id,qqClientConnectServerThread);
    }
}
