package com.ovozz.domain.utils;

import java.net.*;

public class GetPublicIP {

    public static String URL = "http://myip.dnsomatic.com/";

    public static void main(String[] args) throws Exception {
        System.out.println(getWebIp());
        System.out.println(getIp());
        System.out.println(getHostName());
    }

    /**
     * 获取本机名称
     * @return
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 获取内网ip地址
     * @return
     */
    public static String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取外网ip地址
     * @return
     */
    public static String getWebIp() throws Exception {
        String ip= HttpUtils.getResult(URL,null,"UTF-8");
        return ip;
    }
}