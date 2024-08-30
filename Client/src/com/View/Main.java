package com.View;

import com.ClientServer.ClientServer;
import com.Comm.User;
import com.Utils.ScannerUser_;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().MainView();
    }

    public void MainView() throws IOException, InterruptedException {
        ClientServer clientServer = new ClientServer();
        System.out.println("===============欢迎进入QQ系统===============");
        boolean l = true;
        String key;
        while (l) {
            System.out.println("\t\t1.登录QQ");
            System.out.println("\t\t2.退出QQ");
            System.out.println("\t\t3.注册账号");
            System.out.print("请输入您的选择：");
            key = ScannerUser_.StringInput();
            switch (key) {
                case "1":
                    System.out.print("请输入账号：");
                    int id = ScannerUser_.NumberIntInput();
                    System.out.print("请输入密码：");
                    String pwd = ScannerUser_.StringInput();
                    User user1 = clientServer.CheckLogin(id, pwd);
                    if (user1 != null) {
                        System.out.println("登陆成功!");
                        menuView(clientServer, user1);
                    } else {
                        System.out.println("登录失败!");
                    }
                    break;
                case "2":
                    l = false;
                    System.out.println("要退出QQ程序");
                    break;
                case "3":
                    System.out.print("请输入你的姓名：(-1)退出");
                    String newName = ScannerUser_.StringInput();
                    if (newName.equals("-1")) {
                        break;
                    }
                    System.out.print("请输入您不少于六位数的密码：(-1退出)");
                    String newPwd = ScannerUser_.StringInput();
                    if (newPwd.equals("-1")) {
                        break;
                    }
                    String BirthDay = BirthDayInput();
                    String sex;
                    while (true) {
                        System.out.print("请输入您的性别：");
                        sex = ScannerUser_.StringInput();
                        if (sex.equals("男") || sex.equals("女")) {
                            break;
                        }
                        System.out.println("您输入错误，请重新输入");
                    }
                    clientServer.RegisterAccount(newName, newPwd, BirthDay, sex);
                    break;
                default:
                    System.out.print("你的选择不在范围内，请重新选择");
            }
        }
    }

    public void menuView(ClientServer clientServer, User user) throws IOException, InterruptedException {
        System.out.println("===============欢迎登录二级页面(" + user.getName() + ")===============");
        boolean l = true;
        int key;
        int id = 0;
        while (l) {
            Thread.sleep(10);
            System.out.println("\t\t1.显示个人资料");
            System.out.println("\t\t2.修改个人资料");
            System.out.println("\t\t3.显示他人资料");
            System.out.println("\t\t4.显示在线好友");
            System.out.println("\t\t5.显示所有好友");
            System.out.println("\t\t6.私聊好友");
            System.out.println("\t\t7.群发消息");
            System.out.println("\t\t8.私发文件");
            System.out.println("\t\t9.查看历史消息纪录");
            System.out.println("\t\t10.注销");
            System.out.println("\t\t11.修改密码");
            System.out.println("\t\t12.退出QQ系统");
            System.out.print("请输入你的选择: ");
            key = ScannerUser_.NumberIntInput();
            switch (key) {
                case 1:
                    clientServer.CheckInformation(user.getName(), user.getId(), user.getId());
                    break;
                case 2:
                    System.out.print("请输入您的姓名：(按回车跳过/按-1退出)");
                    String newName = ScannerUser_.StringInputEnter();
                    String BirthDay = BirthDayInput();
                    System.out.print("请输入您的性别：(按回车跳过)");
                    String sex = ScannerUser_.StringInputEnter();
                    clientServer.ModifyInformation(user.getName(), user.getId(), newName, BirthDay, sex);
                    break;
                case 3:
                    System.out.println("请输入您要查看资料的用户id: (-1退出)");
                    id = ScannerUser_.NumberIntInput();
                    if (id == -1) {
                        break;
                    }
                    clientServer.CheckInformation(user.getName(), user.getId(), id);
                    break;
                case 4:
                    clientServer.CheckOnlineFriend(user);
                    break;
                case 5:
                    clientServer.CheckAllFriend(user);
                    break;
                case 6:
                    System.out.print("输入您要发送的id号：(-1退出)");
                    id = ScannerUser_.NumberIntInput();
                    if (id == -1) {
                        break;
                    }
                    System.out.print("输入你要发送的内容: (-1)退出");
                    String content = ScannerUser_.StringInput();
                    if (content.equals("-1")) {
                        break;
                    }
                    clientServer.PrivateChat(id, content, user);
                    break;
                case 7:
                    System.out.print("请输入您要发送的内容(-1退出)");
                    content = ScannerUser_.StringInput();
                    if (content.equals("-1")) {
                        break;
                    }
                    clientServer.GroupChat(content, user);
                    break;
                case 8:
                    //用于在用户取消使用该功能
                    boolean flag=false;
                    System.out.print("请输入你要发送的id：(-1退出)");
                    if (id == -1) {
                        break;
                    }
                    String srcPah;
                    id = ScannerUser_.NumberIntInput();
                    while(true) {
                        System.out.print("请输入您要发送的源文件的路径：(-1退出)");
                        srcPah = ScannerUser_.StingPath();
                        if (srcPah.equals("-1")) {
                            flag=true;
                            break;
                        }
                        if(CheckPath(srcPah)){
                           break;
                        }else{
                            System.out.println("=====您输入的路径有误，请重新输入！=====");
                        }
                    }
                    if(flag){
                        break;
                    }
                    String desPah;
                    while(true) {
                        System.out.print("请输入要发送目的文件文件路径: (-1退出)");
                        //一定要注意这里，因为Scanner.nextLine()是可以吸收回车字符的
                        //所以在我们第一次利用nextLine输入的时候，已经把回车干掉了，所以这里的destPath不需要再吸收一次回车字符,直接用next
                        desPah = ScannerUser_.StringInput();
                        if (desPah.equals("-1")) {
                            flag=true;
                            break;
                        }if(CheckPath(desPah)){
                            break;
                        }else{
                            System.out.println("=====您输入的路径有误，请重新输入！=====");
                        }
                    }
                    if (flag){
                        break;
                    }
                    clientServer.FileSend(user, id, srcPah, desPah);
                    break;
                case 9:
                    System.out.print("请输入您要查找聊天记录的相关人(1)用户(2)群聊(-1)退出");
                    id=-1;
                    String str=null;
                    boolean loop=true;
                    while (loop) {
                        str = ScannerUser_.StringInput();
                        switch (str) {
                            case "1":
                                System.out.print("请输入id号：");
                                id = ScannerUser_.NumberIntInput();
                                loop=false;
                                break;
                            case "2":
                                str="群聊";
                                loop=false;
                                break;
                            case "-1":
                                loop=false;
                                break;
                            default:
                                System.out.println("您的输入不在范围内，请重新输入");
                                break;
                        }
                    }
                    if(str.equals("-1")){
                        break;
                    }
                    clientServer.CheckHistoryMessage(user,id,str);
                    break;
                case 10:
                    clientServer.LogoutAccount(user);
                    break;
                case 11:
                    System.out.print("请输入原密码：(-1退出)");
                    String originalPwd = ScannerUser_.StringInput();
                    if (originalPwd.equals("-1")) {
                        break;
                    }
                    System.out.print("请输入新密码：(-1退出)");
                    String newPwd = ScannerUser_.StringInput();
                    if (newPwd.equals("-1")) {
                        break;
                    }
                    clientServer.ModifyPassword(user, originalPwd, newPwd);
                    break;
                case 12:
                    clientServer.ExitSystem(user.getId());
                    l = false;
                    System.exit(0);
                    break;
            }
        }
    }
    //正则表达式处理输入路径异常问题
    public String BirthDayInput() {
        String BirthDay;
        while (true) {
            System.out.print("请输入您的生日：(YYYY—MM-dd 按回车跳过)");
            BirthDay = ScannerUser_.StringInput();
            if (BirthDay.equals("")) {
                return null;
            }
            String regx = "^(\\d{4})-(\\d{1,2})-(\\d{1,2})$";
            Pattern compile = Pattern.compile(regx);
            Matcher matcher = compile.matcher(BirthDay);
            if (matcher.find()) {
                if (!(Calendar.getInstance().get(Calendar.YEAR) >= Integer.parseInt(matcher.group(1)) &&
                        12 >= Integer.parseInt(matcher.group(2)) && 31 >= Integer.parseInt(matcher.group(3)))) {
                    System.out.println("您输入的格式错误，请重新输入");
                    continue;
                }
                break;
            }
        }
        return BirthDay;
    }
    //正则表达式处理路径问题
    public boolean CheckPath(String path){
        String regx="[\\w\\(\\)\\[\\]]+:((\\\\)[\\w\\s\\(\\)\\[\\]\\.]+)+.(\\w)+";
        return Pattern.matches(regx, path);
    }
}
