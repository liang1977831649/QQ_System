package com.ClientServer;

import com.Comm.Message;
import com.Comm.MessageType;
import com.Comm.User;

import javax.jws.soap.SOAPBinding;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//用于发出请求动作的类
public class ClientServer {
    public  Socket   socket;
    public void RegisterAccount(String name,String pwd,String birthday,String sex) throws IOException {
        //这里先不设置id,等到服务端那边相应之后在设置id
        java.sql.Date date1;
        //定义一个新的用户
        User user=null;
        try {
            Date date;
            date=new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
            date1=new java.sql.Date(date.getTime());
            user=new User(name,pwd,date1,sex);
            user.setId(0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //发送
        //1.因为在登陆之前，所以没有Socket
        //因为没有一个Socket,所以要新建一个
        Socket socket1=new Socket("169.254.82.247",9999);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
        objectOutputStream.writeObject(user);

        //在添加之前，是没有对应的线程开启这一事件的，所以不能在QQThread接收到服务端的信息，故故只能在这里接受一次，用一次就走人
        ObjectInputStream objectInputStream = new ObjectInputStream(socket1.getInputStream());
        Message message ;
        try {
            message = (Message) objectInputStream.readObject();
            new ClientThread().RegisterAccount(message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public  User CheckLogin(int id,String pwd){
        boolean l=false;
        User user1=null;
        try {
            User user=new User();
            user.setId(id);
            user.setPassword(pwd);

            //创建输出流，将user对象发送给服务端
            socket=new Socket("169.254.82.247",9999);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(user);

            //等待服务端发送的消息回来验证
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Message message=(Message) objectInputStream.readObject();
            user1 = message.getUser();

            if(message.getMessageType().equals(MessageType.MESSAGE_LOGIN_SUCCESS)){
                //创建一个线程类
                ClientThread clientThread = new ClientThread(id,socket);
                //将该线程存放在hashmap中
                ManageClientThread.addThread(id,clientThread);
                //启动该线程
                new Thread(clientThread).start();
            }else{
               socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return user1;
    }
    //显示用户资料
    public void CheckInformation(String sender,int id ,int InfoId) throws IOException {
        //将User类对象放进message里面，这样在服务端查询id时，就可以通过user知道返回的是谁的信息，返回后一定要判空存不存在
        User user = new User();
        user.setId(InfoId);
        Message message = new Message();
        message.setUser(user);
        message.setSender(sender);
        message.setMessageType(MessageType.MESSAGE_SHOW_INFORMATION);
        //得到了对应的Socket
        Socket socket=ManageClientThread.getThread(id).getSocket();

        //创建一个流对象，将message发送过去
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);

    }
    //修改个人资料
    public void ModifyInformation(String name,int id ,String newName,String Birthday,String sex) throws IOException {
        //1.先得到这个原有的User类对象
        Message message = new Message();
        User user = new User();
        user.setId(id);
        user.setName(newName);
        if(!Birthday.equals("")){
            user.setDates(java.sql.Date.valueOf(Birthday));
        }
        //将该类包装在message
        user.setSex(sex);
        message.setUser(user);
        message.setSender(name);
        message.setMessageType(MessageType.MESSAGE_MODIFY_INFO);

        //发送出去
        Socket socket = ManageClientThread.getThread(id).getSocket();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);

    }
    //显示在线好友
    public void CheckOnlineFriend(User user) throws IOException {
        Message message = new Message();

        message.setMessageType(MessageType.MESSAGE_ONLINE_FRIEND);
        message.setSender(user.getName());
        System.out.println("userName="+user.getName());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
    }
    //显示所有好友
    public void CheckAllFriend(User user) throws IOException {
        Message message = new Message();
        message.setSender(user.getName());
        message.setMessageType(MessageType.MESSAGE_ALL_FRIEND);

        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);

    }
    //私聊好友
    public void PrivateChat(int id,String content, User user) throws IOException{
        Message message = new Message();
        message.setSender(user.getName());
        //user作为他要发送的人
        User user1 = new User();
        user1.setId(id);
        message.setUser(user1);
        message.setContent(content);
        message.setSender(user.getName());
        //得到发送的时间
        message.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        //发送
        message.setMessageType(MessageType.MESSAGE_PRIVATE_CHAT);
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
        System.out.println("=====发送成功=====");
    }
    //群发消息
    public void GroupChat(String content,User user) throws IOException {
        Message message = new Message();
        message.setContent(content);
        message.setSender(user.getName());
        message.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        message.setMessageType(MessageType.MESSAGE_GROUP_CHAT);
        //发送
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
        System.out.println("=====发送成功=====");
    }
    //私发文件
    public void FileSend(User user,int id,String srcPath,String dscPath) throws IOException {
        //如果文件不存在
        File file = new File(srcPath);
        if(!file.exists()){
            System.out.println("您的源文件路径错误或不存在！");
            return;
        }
        //使用缓冲输入流，读取文件
        int length = (int) file.length();
        byte[] bytes=new  byte[length];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(srcPath));
        bufferedInputStream.read(bytes,0,length);

        //新建message，并初始化
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_PRIVATE_FILE);
        message.setSender(user.getName());
        User user1 = new User();
        user1.setId(id);
        message.setUser(user1);
        message.setBytes(bytes);
        message.setDesPath(dscPath);
        message.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

        //发送
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);

        System.out.println("=====发送成功====");

    }
    //查看历史纪录
    public void HistoryMessage(int id){

    }
    //注销
    public void LogoutAccount(User user) throws IOException {
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_LOGOUT);
        message.setUser(user);
        message.setSender(user.getName());

        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);

    }
    //退出系统
    public void ExitSystem(int id) throws IOException {
        //1.创建一个message类对象,将message对象发送出去
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_EXIT);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);

        ManageClientThread.removeThread(id);

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void ModifyPassword(User user,String originalPwd,String newPwd) throws IOException {
        //将这个用户的id和原始密码存放在user中，在封装到message中
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_MODIFY_PASSWORD);
        message.setPassword(newPwd);
        User user1 = new User();
        user1.setPassword(originalPwd);
        user1.setId(user.getId());
        message.setUser(user1);
        message.setSender(user.getName());
        //发送
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
    }
    public void CheckHistoryMessage(User user,int id,String str) throws IOException {
        Message message = new Message();
        //-1表示要查找的记录是群聊
        if(id!=-1){
        message.setMessageType(MessageType.MESSAGE_HISTORY_MEG);
        message.setSender(user.getName());
        User user1 = new User();
        user1.setId(id);
        message.setUser(user1);}
        //如果是群聊的话
        else if(str.equals("群聊")){
            message.setMessageType(MessageType.MESSAGE_HISTORY_MEG);
        }
        //发送
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
    }
}
