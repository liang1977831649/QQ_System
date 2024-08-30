package com.ClientServer;

import com.Comm.Message;
import com.Comm.MessageType;
import com.Comm.User;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientThread implements Runnable {
    private int id;
    private Socket socket;

    public ClientThread() {
    }

    public ClientThread(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    //这里是接受服务端发送来的处理结果，并作出相应的回应
    @Override
    public void run() {
        boolean l = true;
        while (l) {
            try {
                System.out.println("==========线程（" + id + "）正在监听==========");
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) objectInputStream.readObject();
                //修改个人资料
                if (message.getMessageType().equals(MessageType.MESSAGE_MODIFY_INFO)) {
                    ModifyInfo(message);
                }
                //显示用户资料
                else if (message.getMessageType().equals(MessageType.MESSAGE_SHOW_INFORMATION)) {
                    System.out.println("=========查看用户信息=========");
                    CheckInformation(message);
                }//显示在线好友
                else if (message.getMessageType().equals(MessageType.MESSAGE_ONLINE_FRIEND)) {
                    ShowOnlineFriend(message);
                }//显示所有好友
                else if (message.getMessageType().equals(MessageType.MESSAGE_ALL_FRIEND)) {
                    ShowAllFriends(message);
                }//私聊
                else if (message.getMessageType().equals(MessageType.MESSAGE_PRIVATE_CHAT)) {
                    ReceiveMessage(message);
                }//群聊
                else if (message.getMessageType().equals(MessageType.MESSAGE_GROUP_CHAT)) {
                    GroupChat(message);
                }
                //私发文件
                else if(message.getMessageType().equals(MessageType.MESSAGE_PRIVATE_FILE)){
                    PrivateFile(message);
                }
                //修改密码
                else if(message.getMessageType().equals(MessageType.MESSAGE_MODIFY_PASSWORD)){
                    ModifyPassword(message);
                }
                //查看历史纪录
                else if (message.getMessageType().equals(MessageType.MESSAGE_HISTORY_MEG)) {
                    HistoryMessage(message);
                }//注销
                else if (message.getMessageType().equals(MessageType.MESSAGE_LOGOUT)) {
                    LogoutAccount(message);
                    l=false;
                    System.exit(0);
                }//退出
                else if (message.getMessageType().equals(MessageType.MESSAGE_EXIT)) {
                    System.out.println("\n您已经下线");
                    socket.close();
                    l = false;
                    System.exit(0);
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //接收查看个人信息
    public void CheckInformation(Message message) {
        User user = message.getUser();
        if (user != null) {
            System.out.println("\n=====该用户的信息如下=====");
            System.out.println(user);
        }else{
            System.out.println("\n没有找到该用户信息");
        }
    }
    public void ModifyInfo(Message message){
        if(message.getUser()!=null){
            System.out.println("\n=====修改成功，您的资料如下=====");
            System.out.println(message.getUser().toString());
        }else{
            System.out.println("\n=====修改失败=====");
        }
    }
    public void RegisterAccount(Message message){
        if(message.getUser()!=null){
            System.out.println("\n添加成功，信息如下");
            System.out.println(message.getUser().toString());
        }else{
            System.out.println("添加失败");
        }
    }
    public void ShowOnlineFriend(Message message) throws SQLException {
        System.out.println("\n=====在线用户信息如下=====");
        System.out.println(message.getContent());
    }
    public void ShowAllFriends(Message message){
        System.out.println("\n=====所有用户的信息如下=====");
        System.out.println(message.getContent());
    }
    public void ReceiveMessage(Message message){
        System.out.println("\n=====时间:"+message.getSendTime()+message.getSender()+"对你说: =====");
        System.out.println(message.getContent());
    }
    public void GroupChat(Message message){
        System.out.println("\n=====时间: "+message.getSendTime()+message.getSender()+"对大家说");
        System.out.println(message.getContent());
    }
    public void PrivateFile(Message message) throws IOException {
        System.out.println("\n时间："+message.getSendTime()+"用户"+message.getSender()+"向你发送了文件");
        //创建输出流
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(message.getDesPath()));

        bufferedOutputStream.write(message.getBytes(),0,message.getBytes().length);
        //一定要关闭，否则传输未完成
        bufferedOutputStream.close();
        System.out.println("====接收成功=====");
    }
    public void ModifyPassword(Message message){
        User user = message.getUser();
        if(user!=null){
            System.out.println("=====修改密码成功=====");
        }else{
            System.out.println("=====修改密码失败=====");
        }
    }
    public void LogoutAccount(Message message){
        System.out.println(message.getUser()==null?"注销成功":"注销失败");
    }
    public void HistoryMessage(Message message){
        System.out.println("\n====您要查看的聊天记录情况如下=====");
        System.out.println(message.getContent());
    }
}
