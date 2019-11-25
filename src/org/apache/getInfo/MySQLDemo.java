package org.apache.getInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;

public class MySQLDemo {

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    //static final String DB_URL = "jdbc:mysql://192.168.43.211:3306/ftp";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/ftp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "user1";
    static final String PASS = "123456";

    //读取文件内容
    public static void readTxt(File file) {
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "gbk");
            BufferedReader br = new BufferedReader(isr);
            StringBuffer resposeBuffer = new StringBuffer("");
            String lineTxt = null;
            lineTxt = br.readLine();
            String[] data=new String[22];
            String[] split = lineTxt.split(":");
            data[0] = split[1];//deviece
            int cnt = 0;
            while ((lineTxt = br.readLine()) != null) {
                split = lineTxt.split(": ");
                data[++cnt] = split[1];
            }
            for(int i=0;i<data.length;i++){
                System.out.println(data[i]);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //从linux上获取最新的ssd文件信息
//            String strCmd = "pscp.exe -l root -pw \"123123\" root@10.11.137.33:/root/sys_info/vnme0.txt \"D:/kylin\"";
//            Runtime runtime = Runtime.getRuntime();
//            try {
//                runtime.exec(strCmd);
//            } catch (Exception e) {
//                System.out.println("Get nvme0.txt Error!");
//            }
            String filePath = "D:/kylin/vnme0.txt";
            File file = new File(filePath);
            readTxt(file);
            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, name, url FROM websites";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                String name = rs.getString("name");
                String url = rs.getString("url");

                // 输出数据
                System.out.print("ID: " + id);
                System.out.print(", 站点名称: " + name);
                System.out.print(", 站点 URL: " + url);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}