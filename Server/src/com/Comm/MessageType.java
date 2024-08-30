package com.Comm;

public interface MessageType {
    String MESSAGE_LOGIN_SUCCESS="1";//登陆成功
    String MESSAGE_LOGIN_FAIL="2";//登录失败
    String MESSAGE_MODIFY_INFO="3";//修改个人资料
    String MESSAGE_SHOW_INFORMATION="4";//显示个人资料
    String MESSAGE_ONLINE_FRIEND="5";//显示在线好友
    String MESSAGE_ALL_FRIEND="6";//显示所有好友
    String MESSAGE_PRIVATE_CHAT="7";//私聊好友
    String MESSAGE_GROUP_CHAT="8";//群发消息;
    String MESSAGE_PRIVATE_FILE="9";//私发文件
    String MESSAGE_HISTORY_MEG="10";//查看历史聊天记录
    String MESSAGE_MODIFY_PASSWORD="14";//修改密码
    String MESSAGE_REGISTER="11";//注册
    String MESSAGE_LOGOUT="12";//注销
    String MESSAGE_EXIT="13";//退出
}
