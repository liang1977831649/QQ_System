package com.QQServer;

import com.Comm.Message;
import com.Comm.MessageType;
import com.Utils.ScannerUser_;
import jdk.net.Sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QQControl extends Thread{
    ServerSocket sockets;

    public QQControl(ServerSocket sockets) {
        this.sockets = sockets;
    }

    @Override
    public void run() {
        boolean l=true;
        while(l){
            int key;
            System.out.println("等待输入: (1)退出服务端(2)群发消息");
            key=ScannerUser_.NumberIntInput();
            if(key==1){
                //结束掉所有线程
                ManageConnectThread.ClearThread();
                l=false;
                System.exit(0);
            }else if(key==2){
                System.out.println("请输入发送内容: ");
                String content=ScannerUser_.StringInputEnter();
                Message message = new Message();
                message.setContent(content);
                message.setMessageType(MessageType.MESSAGE_GROUP_CHAT);
                message.setSender("服务端");
                message.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                try {
                    QQServer.ServerSendMessageToClient(message);
                } catch (SQLException | IOException throwables) {
                    throwables.printStackTrace();
                }
            }

        }
    }
}
