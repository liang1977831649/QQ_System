package com.QQServer;

import com.Comm.Message;
import com.Comm.MessageType;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.SQLException;

public class QQClientConnectServerThread implements  Runnable{
    private int id;
    private Socket socket;
    private boolean l=true;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public QQClientConnectServerThread(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public boolean isL() {
        return l;
    }

    public void setL(boolean l) {
        this.l = l;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        while (l) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) objectInputStream.readObject();
                //判断数据类型
                //显示用户信息
                if(message.getMessageType().equals(MessageType.MESSAGE_SHOW_INFORMATION)){
                    System.out.println("用户："+message.getSender()+"请求显示用户信息");
                    QQServer.showInformation(message,socket);
                }else if(message.getMessageType().equals(MessageType.MESSAGE_MODIFY_INFO)){
                    System.out.println("用户"+message.getSender()+"请求修改信息");
                    QQServer.ModifyInfo(message,socket);
                }
                //显示在线好友
                else if(message.getMessageType().equals(MessageType.MESSAGE_ONLINE_FRIEND)){
                    System.out.println("用户"+ message.getSender()+"显示在线好友");
                    QQServer.OnlineFriends(message,socket);
                }
                //显示所有好友
                else if(message.getMessageType().equals(MessageType.MESSAGE_ALL_FRIEND)){
                    System.out.println("用户"+message.getSender()+"显示所有好友");
                    QQServer.CheckAllFriends(socket);
                }//私聊
                else if(message.getMessageType().equals(MessageType.MESSAGE_PRIVATE_CHAT)){
                    System.out.println("用户"+message.getSender()+"私聊用户"+message.getUser().getId());
                    QQServer.PrivateChat(message);
                }//群聊
                else if(message.getMessageType().equals(MessageType.MESSAGE_GROUP_CHAT)){
                    System.out.println("用户"+message.getSender()+"群聊");
                    QQServer.GroupChat(message);
                }//发送文件
                else if(message.getMessageType().equals(MessageType.MESSAGE_PRIVATE_FILE)){
                    System.out.println("用户"+message.getSender()+"向用户"+message.getUser().getId()+"发送文件");
                    QQServer.PrivateFile(message);
                }//注销
                else if(message.getMessageType().equals(MessageType.MESSAGE_LOGOUT)){
                    System.out.println("用户"+message.getSender()+"请求注销账户");
                    QQServer.LogoutAccount(message);
                    l=false;
                }
                //修改密码
                else if(message.getMessageType().equals(MessageType.MESSAGE_MODIFY_PASSWORD)){
                    System.out.println("用户"+message.getSender()+"修改密码");
                    QQServer.ModifyPassword(message);
                }
                //查看历史纪录
                else if(message.getMessageType().equals(MessageType.MESSAGE_HISTORY_MEG)){
                    QQServer.HistoryMessage(message,socket);
                }//退出
                else if(message.getMessageType().equals(MessageType.MESSAGE_EXIT)){
                    QQServer.ExitSystem(id,socket);
                    l=false;
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
