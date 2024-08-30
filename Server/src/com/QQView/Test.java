package com.QQView;
import com.Comm.HistoryMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Test {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress inetAddress=InetAddress.getLocalHost();
        System.out.println(inetAddress.toString());
    }
}
