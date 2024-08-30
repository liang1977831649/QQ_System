package com.QQServer;

import com.Comm.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class OfflineMessage {
    private static HashMap<Integer, ArrayList<Message>> hashMap=new HashMap<>();
    public void addArrayList(int id){
        hashMap.put(id,new ArrayList<Message>());
    }
    public void RemoveArrayList(int id){
        hashMap.remove(id);
    }
    public void addOfflineMessage(int id,Message message){
        if(hashMap.get(id)==null){
            //如果不存在就直接添加了
            addArrayList(id);
        }
        ArrayList<Message> messages = hashMap.get(id);
        messages.add(message);
    }
    public ArrayList<Message> getArrayList(int id){
        return hashMap.get(id);
    }
}
