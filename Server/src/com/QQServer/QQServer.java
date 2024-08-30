package com.QQServer;

import com.Comm.HistoryMessage;
import com.Comm.Message;
import com.Comm.MessageType;
import com.Comm.User;
import com.Dao_.UserDao;
import com.Utils.JDBCUtilsDruid;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class QQServer {
    private static ServerSocket sockets;
    private static Connection connection;
    private static QueryRunner queryRunner;
    private static QQControl qqControl;
    private static UserDao userDao;
    private static OfflineMessage offlineMessage;

    //验证账号密码登录
    public static User CheckLogin(int id, String pwd) throws SQLException {
        Connection connect = JDBCUtilsDruid.getConnect();
        String sql = "select * from users where id=? and password=md5(?)";
        User user = queryRunner.query(connect, sql, new BeanHandler<User>(User.class), id, pwd);
        return user;
    }

    static {
        try {
            sockets = new ServerSocket(9999);
            queryRunner = new QueryRunner();
            connection = JDBCUtilsDruid.getConnect();
            userDao = new UserDao();
            offlineMessage = new OfflineMessage();
            System.out.println("服务端已建立，正在等待接收。。。。");
            qqControl = new QQControl(sockets);
            qqControl.start();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    public QQServer() throws IOException {
        try {
            while (true) {
                //得到一个客户端输入的Socket
                Socket socket = sockets.accept();
                //得到一个对象流
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                User user = (User) objectInputStream.readObject();

                //这里是注册操作
                //检查是否是要操作注册的用户:看看这个用户的id是否为空，如果为0，则是
                if (user.getId() == 0) {
                    //得到对应的Socket
                    System.out.println("用户请求注册账户");
                    RegisterAccount(user, socket);
                    continue;
                }

                //下面是登陆操作
                //验证账号密码
                int id = user.getId();
                String password = user.getPassword();
                Message message = new Message();
                User newUser = CheckLogin(id, password);
                message.setUser(newUser);
                if (newUser != null) {
                    //下面是验证这个用户是否在线，如果在线，那么在线的那一端就被强制下线，这一端就是新登录的线程
                    if (CheckOnline(user) != null) {
                        //将原有的给退出
                        QQClientConnectServerThread qqClientConnectServerThread = ManageConnectThread.getHashMapSocket(newUser.getId());
                        Socket socket1 = qqClientConnectServerThread.getSocket();
                        Message message1 = new Message();
                        message1.setMessageType(MessageType.MESSAGE_EXIT);
                        new ObjectOutputStream(socket1.getOutputStream()).writeObject(message1);

                        //强制终止其在服务端的线程
                        qqClientConnectServerThread.setL(false);
                    }
                    message.setMessageType(MessageType.MESSAGE_LOGIN_SUCCESS);
                    //创建一个线程类
                    QQClientConnectServerThread qqClientConnectServerThread = new QQClientConnectServerThread(id, socket);
                    //将该线程放到hashmap
                    ManageConnectThread.addHashMapSocket(id, qqClientConnectServerThread);

                    //将该登陆的账号和姓名插入到OnlineUser里面去
                    String sql = "insert into onlineUser values(?,?)";
                    userDao.QueryDML(sql, newUser.getId(), newUser.getName());

                    //启动该线程
                    new Thread(qqClientConnectServerThread).start();

                    //新建一个对象输出流，将message发送过去
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(message);

                    System.out.println(user.getId() + "已登录系统");

                    //此时，查看这个用户有没有离弦的的消息，如果有，那么就一个个发送过去
                    ArrayList<Message> arrayList = offlineMessage.getArrayList(id);
                    if (arrayList != null) {
                        for (Message message1 : arrayList) {
                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.writeObject(message1);
                        }
                        //发送完之后，清空
                        arrayList.clear();
                    }

                } else {
                    message.setMessageType(MessageType.MESSAGE_LOGIN_FAIL);
                    //新建一个对象输出流，将message发送过去
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(message);
                    socket.close();
                }
            }

        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                sockets.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static QQClientConnectServerThread CheckOnline(User user) throws IOException {
        QQClientConnectServerThread qqClientConnectServerThread = ManageConnectThread.getHashMapSocket(user.getId());
        return qqClientConnectServerThread;
    }

    public static void showInformation(Message message, Socket socket) throws SQLException, IOException {
        //得到该用户的id
        int id = message.getUser().getId();
        //查找并得到对应的类对象
        String sql = "select * from users where id=?";
        User user = queryRunner.query(connection, sql, new BeanHandler<User>(User.class), id);

        //封装到message类中
        Message message1 = new Message();
        message1.setUser(user);
        message1.setMessageType(MessageType.MESSAGE_SHOW_INFORMATION);

        //获取对那对象输出流，发送出去
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message1);
    }

    public static void ExitSystem(int id, Socket socket) throws SQLException {
        //执行t退出
        ManageConnectThread.RemoveHashMapSocket(id);
        //在OnlineUser里面删除对应的行
        String sql = "delete from onlineUser where id=?";
        new UserDao().QueryDML(sql, id);
        //关闭对应的Socket
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("用户" + id + "已下线");
    }

    public static void ModifyInfo(Message message, Socket socket) throws SQLException, IOException {
        //1.先返回一个原来的User对象
        User user = message.getUser();
        String sql = "select * from Users where id=?";
        int id = message.getUser().getId();
        User oldUser = queryRunner.query(connection, sql, new BeanHandler<User>(User.class), id);
        //修改这个user对象的信息
        //如果做了改动的话
        if (!user.getName().equals("")) {
            oldUser.setName(user.getName());
        }
        if (!(user.getDates() == null)) {
            oldUser.setDates(user.getDates());
        }
        if (!user.getSex().equals("")) {
            oldUser.setSex(user.getSex());
        }
        System.out.println("User" + oldUser);
        System.out.println("oldUser" + oldUser);
        //修改完之后得到一个修改后的oldUser
        //执行sql语句
        sql = "update users set name=?,sex=?,dates=? where id=?";
        int i = userDao.QueryDML(sql, oldUser.getName(), oldUser.getSex(), oldUser.getDates().toString(), id);
        //将修改成功的消息发送给客户端
        Message message1 = new Message();
        message1.setMessageType(MessageType.MESSAGE_MODIFY_INFO);
        //找到修改后的User
        sql = "select * from users where id =?";
        User user1 = userDao.QuerySignal(sql, User.class, id);
        System.out.println("i=" + i);
        if (i >= 1) {
            message1.setUser(user1);
        } else {
            message.setUser(null);
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message1);
    }

    public static void RegisterAccount(User user, Socket socket) throws SQLException, IOException {
        //先设置位数,账号为：
        int id = (int) (Math.random() * 10000) + 1;
        //sql插入语句
        user.setId(id);
        String sql = "insert into users values(?,?,md5(?),?,?)";
        int i = userDao.QueryDML(sql, user.getId(), user.getName(), user.getPassword(), user.getDates().toString(), user.getSex());
        //新建message对象，将结果发送给客户端
        Message message1 = new Message();
        if (i >= 1) {
            System.out.println("注册账户成功!");
            message1.setUser(user);
        }
        message1.setMessageType(MessageType.MESSAGE_REGISTER);
        //发送
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message1);

    }

    public static void OnlineFriends(Message message, Socket socket) throws SQLException, IOException {
        String sql = "select * from onlineUser";
        //得到一个结果集
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        StringBuffer stringBuffer = new StringBuffer("");
        //将result的内容存放到String Content中
        while (resultSet.next()) {
            stringBuffer.append(resultSet.getString(1).toString() + "\t");
            stringBuffer.append(resultSet.getString(2).toString() + "\n");
        }
        //初始化message
        Message message1 = new Message();
        message1.setContent(stringBuffer.toString());
        message1.setMessageType(MessageType.MESSAGE_ONLINE_FRIEND);

        //发送
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message1);
    }

    public static void CheckAllFriends(Socket socket) throws SQLException, IOException {
        //查找，得到属性id，name
        String sql = "select id,name from users";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        StringBuffer stringBuffer = new StringBuffer("");
        while (resultSet.next()) {
            stringBuffer.append(resultSet.getString(1) + "\t");
            stringBuffer.append(resultSet.getString(2) + "\n");
        }
        //新建message类对象并发送出去
        Message message = new Message();
        message.setContent(stringBuffer.toString());
        message.setMessageType(MessageType.MESSAGE_ALL_FRIEND);
        //发送
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
    }

    public static void PrivateChat(Message message) throws IOException, SQLException {
        //得到接受者的id

        //查看是否由该用户
        String sql = "select name from users where id=?";
        String name = (String) userDao.QueryOne(sql, message.getUser().getId());
        //发送者的id
        sql = "select id from users where name=?";
        int SenderId = (int) userDao.QueryOne(sql, message.getSender());

        if (name == null) {
            message.setMessageType(MessageType.MESSAGE_PRIVATE_CHAT);
            message.setContent("不存在该用户");
            new ObjectOutputStream(ManageConnectThread.getHashMapSocket(SenderId).getSocket().getOutputStream()).writeObject(message);
            return;
        }
        //得到接受者的线程
        QQClientConnectServerThread hashMapSocket = ManageConnectThread.getHashMapSocket(message.getUser().getId());
        //如果该用户离线的话
        if (hashMapSocket == null) {
            //添加离线消息
            offlineMessage.addOfflineMessage(message.getUser().getId(), message);
            return;
        }
        //如果不是离线的话
        //将这个原有的message发送给id的用户
        Socket socket = hashMapSocket.getSocket();
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);

        //保存聊天记录到数据库中
        sql = "insert into historyMessage values(?,?,?,?,?)";
        int update = queryRunner.update(connection, sql, SenderId, message.getUser().getId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), message.getContent(), "私聊");
        if (update <= 0) {
            System.out.println("更新聊天记录失败");
        }
    }

    public static void GroupChat(Message message) throws IOException, SQLException {
        //查找发送者的用户id和姓名
        String sql = "select id from users where name=?";
        int SenderId = (int) userDao.QueryOne(sql, message.getSender());

        //我们得到所有用户的id，因为要群体发送
        sql = "select id from users";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        int id;
        //逐个遍历，逐个发送
        QQClientConnectServerThread hashMapSocket;
        while (resultSet.next()) {
            //如果这个id存在于在线用户的话
            id = Integer.parseInt(resultSet.getString(1));
            //不能等于自己
            if (id == SenderId) {
                continue;
            }
            if ((hashMapSocket = ManageConnectThread.getHashMapSocket(id)) != null) {
                //发送给群体用户
                new ObjectOutputStream(hashMapSocket.getSocket().getOutputStream()).writeObject(message);
            } else {
                offlineMessage.addOfflineMessage(id, message);
            }
        }

        //将消息保存在数据库中
        //0000代表群体
        sql = "insert into historyMessage values(?,?,?,?,?)";
        int update = queryRunner.update(connection, sql, SenderId, 0000,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), message.getContent(), "群聊");
        if (update <= 0) {
            System.out.println("更新聊天记录失败");
        }
    }

    public static void PrivateFile(Message message) throws IOException, SQLException {
        //得到接收者的线程
        //用户是否存在
        String sql = "select name from users where id=?";
        String name = (String) userDao.QueryOne(sql, message.getUser().getId());
        if (name == null) {
            message.setMessageType(MessageType.MESSAGE_PRIVATE_CHAT);
            message.setContent("不存在该用户");
            sql = "select id from users where name=?";
            int SenderId = (int) userDao.QueryOne(sql, message.getSender());
            //清空文件,提高效率
            message.setBytes(null);
            new ObjectOutputStream(ManageConnectThread.getHashMapSocket(SenderId).getSocket().getOutputStream()).writeObject(message);
            return;
        }
        //如果对方在线的话，将对应的线程信息发送出去
        if (ManageConnectThread.getHashMapSocket(message.getUser().getId()) != null) {
            Socket socket = ManageConnectThread.getHashMapSocket(message.getUser().getId()).getSocket();
            new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
        } else {
            offlineMessage.addOfflineMessage(message.getUser().getId(), message);
        }


    }

    public static void ModifyPassword(Message message) throws SQLException, IOException {
        //查看原密码对不对
        String sql = "select * from users where id=? and password=md5(?)";
        User user = queryRunner.query(connection, sql, new BeanHandler<User>(User.class), message.getUser().getId(), message.getUser().getPassword());
        int update = 0;
        if (user != null) {
            //执行修改语句
            sql = "update users set password=md5(?) where id=?";
            update = queryRunner.update(connection, sql, message.getPassword(), user.getId());

        }
        message.setUser(user);
        System.out.println(update >= 1 ? "修改成功" : "修改失败");
        //发送
        new ObjectOutputStream(ManageConnectThread.getHashMapSocket(message.getUser().getId()).getSocket().getOutputStream()).writeObject(message);
    }

    public static void LogoutAccount(Message message) throws SQLException, IOException {
        String sql = "delete from users where id=?";
        User user = message.getUser();
        System.out.println(queryRunner.update(connection, sql, user.getId()) >= 1 ? "注销成功!" : "注销失败");
        message.setUser(null);
        //删除之后
        message.setMessageType(MessageType.MESSAGE_LOGOUT);

        //发送
        new ObjectOutputStream(ManageConnectThread.getHashMapSocket(user.getId()).getSocket().getOutputStream()).writeObject(message);


        ManageConnectThread.RemoveHashMapSocket(user.getId());
    }

    public static void ServerSendMessageToClient(Message message) throws SQLException, IOException {
        //先得到所有人的id号码
        String sql = "select id from users";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        //将获取的线程或者离线发送
        QQClientConnectServerThread qqClientConnectServerThread;
        while (resultSet.next()) {
            int id = Integer.parseInt(resultSet.getString(1));
            qqClientConnectServerThread = ManageConnectThread.getHashMapSocket(id);
            if (qqClientConnectServerThread != null) {
                //如果在在线的话
                new ObjectOutputStream(qqClientConnectServerThread.getSocket().getOutputStream()).writeObject(message);
            } else {
                //如果离线的话
                offlineMessage.addOfflineMessage(id, message);
            }
        }
        //发送成功
    }

    //历史消息
    public static void HistoryMessage(Message message, Socket socket) throws SQLException, IOException {
        User user = message.getUser();
        StringBuffer stringBuffer = new StringBuffer("");
        //判断是查找与某个人的消息
        //如果不是群聊消息的话。-1表是群聊
        List<HistoryMessage> historyMessageList = null;
        //如果是私聊的话
        if (user != null && user.getId() != -1) {
            //查找此人
            String sql = "select * from users where id=?";
            //得到对方的user1
            User user1 = userDao.QuerySignal(sql, User.class, user.getId());
            sql = "select id from users where name=?";
            //得到自己的user2
            User user2 = userDao.QuerySignal(sql, User.class, message.getSender());
            //如果这个id存在
            if (user1 != null && user1.getId() != null) {
                sql = "select * from historyMessage where (SenderId=? and ReceiveId=?) or (ReceiveId=? and SenderId=?)";
                historyMessageList = queryRunner.query(connection, sql, new BeanListHandler<HistoryMessage>(HistoryMessage.class), user1.getId(), user2.getId(), user1.getId(), user2.getId());


                if (historyMessageList != null) {
                    for (HistoryMessage historyMessage : historyMessageList) {
                        //如果发送者是自己的话
                        if ((int) historyMessage.getSenderId() == user2.getId()) {
                            stringBuffer.append("=====时间:" + historyMessage.getDates().toString() + " 我对" + user1.getId() + "说=====\n");
                        } else {
                            stringBuffer.append("=====时间:" + historyMessage.getDates().toString() + user1.getId() + "对我说=====\n");
                        }
                        stringBuffer.append(historyMessage.getContent() + "\n");
                    }
                }
            }
            //如果此人不存在，就反馈信息
            else {
                stringBuffer.append("无法查到id为" + user.getId() + "的用户");
            }

        }
        //如果是群聊的话
        else {
            //先找到发送消息的id
            String sql = "select * from historyMessage where ReceiveId=0";
            historyMessageList = queryRunner.query(connection, sql, new BeanListHandler<HistoryMessage>(HistoryMessage.class));
            if (historyMessageList != null) {
                for (HistoryMessage historyMessage : historyMessageList) {
                    stringBuffer.append("\n=====时间: " + historyMessage.getDates().toString() + " 对大家说=====\n");
                    stringBuffer.append(historyMessage.getContent() + "\n");
                }
            }
        }
        message.setContent(stringBuffer.toString());
        //将消息发送给客户端
        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
    }
}
