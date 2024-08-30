package com.View;


import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String name="34543@qq.com";
        String regx="[\\w]+@[\\w]+.[\\w]+";
        if(Pattern.matches(regx, name)) {
            System.out.printf("true");
        }else{
            System.out.printf("false");
        }
    }
}
