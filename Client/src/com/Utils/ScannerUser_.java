package com.Utils;

import java.util.Scanner;

public class ScannerUser_ {
    public static Scanner scanner=new Scanner(System.in);
    //输入不带小数的数字，防止输入字符串
    public static int NumberIntInput(){
        String str;
        int i=0;
        while(true){
            str=scanner.next();
            try {
                i=Integer.parseInt(str);
                return i;
            } catch (NumberFormatException e) {
                System.out.println("输入错误，请重新输入");
            }
        }
    }
    //输入带小数点的数字，防止输入字符串
    public static double NumberDoubleInput(){
        String str;
        double i=0;
        while(true){
            str=scanner.nextLine();
            try {
                i=Double.parseDouble(str);
                return i;
            } catch (NumberFormatException e) {
                System.out.println("输入错误，请重新输入");
            }
        }
    }
    //输入字符串,并指限制长度
    public static String StringInput(int len)
    {
        String str;
        str=scanner.nextLine();
        if(str.length()<=len){
            return str;
        }else{
            return str.substring(0, len);
        }
    }
    //输入字符串,不指限制长度
    public static String StringInput()
    {
        String str;
        str=scanner.next();
        return str;
    }
    public static String StringInputEnter(){
        scanner.nextLine();
        String str;
        str=scanner.nextLine();
        return str;
    }
    public static String StingPath(){
        scanner.nextLine();
        String string;
        string=scanner.nextLine();
        return string;
    }
}
