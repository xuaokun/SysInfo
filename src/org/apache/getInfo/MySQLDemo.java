//外部传参依次为服务器ip、root用户密码、要获取的磁盘名如nvme0
package org.apache.getInfo;

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MySQLDemo {
    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://10.14.110.21:3306/johnny?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "root";

    static Connection conn = null;
    static Statement stmt = null;
    //调用cmd
    private static void  callCmd(String locationCmd){
        StringBuilder sb = new StringBuilder();
        try {
            Process child = Runtime.getRuntime().exec(locationCmd);
            InputStream in = child.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=bufferedReader.readLine())!=null)
            {
                sb.append(line + "\n");
            }
            in.close();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            System.out.println("sb:" + sb.toString());
            System.out.println("callCmd execute finished");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    //检查是否需要备份数据库
    public static void dump(){
        String batPath = "C:\\Users\\Administrator\\Desktop\\项目文件\\mysql_backup_tool.bat"; // 把你的bat脚本路径写在这里
        File batFile = new File(batPath);
        boolean batFileExist = batFile.exists();
        System.out.println("batFileExist:" + batFileExist);
        if (batFileExist) {
            callCmd(batPath);
        }
    }

    //读取文件内容
    public static void readTxt(File file,String[] data) {
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuffer resposeBuffer = new StringBuffer("");
            String lineTxt = null;
            lineTxt = br.readLine();
            String[] split = lineTxt.split(":");
            data[0] = split[1].split(" ")[0];//deviece
            int cnt = 0;
            while ((lineTxt = br.readLine()) != null) {
                split = lineTxt.split(": ");
                data[++cnt] = split[1];
            }
            //处理字符串为指定格式
            data[2] = data[2].split(" ")[0];
            data[3] = data[3].split("%")[0];
            data[4] = data[4].split("%")[0];
            data[5] = data[5].split("%")[0];
            data[6] = data[6].replaceAll(",","");
            data[7] = data[7].replaceAll(",","");
            data[8] = data[8].replaceAll(",","");
            data[9] = data[9].replaceAll(",","");
//            for(int i=0;i<data.length;i++){
//                System.out.println(data[i]);
//            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        Connection conn = null;
//        Statement stmt = null;
        try{            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            Timer timer = new Timer();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0); //0点
            c.set(Calendar.MINUTE, 0);//0分
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    dump();
                    String sql = "delete from sys_ssd_data";
                    try {
                        stmt.execute(sql);
                        System.out.println("数据已经备份，清空表");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, c.getTime(), 24*60*60*1000); //每天

            //读取linux信息到数据库
            while(true){
                //从linux上获取最新的ssd文件信息
                String strCmd = "pscp.exe -l root -pw \"";
                strCmd += args[1] + "\" root@" + args[0] + ":/root/" + args[2] +".txt \"D:/\"";
                System.out.println(strCmd);

                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec(strCmd);
                } catch (Exception e) {
                    System.out.println("Get txt Error!");
                }
                String filePath = "D:/" + args[2] + ".txt";
                //System.out.println(filePath); //打印文件保存在本地的路径
                File file = new File(filePath);
                String[] data=new String[22];
                readTxt(file,data);
                // 执行插入数据库操作
                String sql;
                sql = "insert into sys_ssd_data(device,critical_warning,temperature,available_spare,available_spare_threshold,percentage_used,data_units_read,data_units_written,host_read_commands,host_write_commands,controller_busy_time,power_cycles,power_on_hours,unsafe_shutdowns,media_errors,num_err_log_entries,Warning_Temperature_Time,Critical_Composite_Temperature_Time,Thermal_Management_T1_Trans_Count,Thermal_Management_T2_Trans_Count,Thermal_Management_T1_Total_Time,Thermal_Management_T2_Total_Time) values(";
                sql += "'" + data[0] + "'," + data[1] + ","+ data[2] + ","+ data[3] + ","+ data[4] + ","+ data[5] + ","+ data[6] + ","+ data[7] + ","
                        + data[8] + ","+ data[9] + ","+ data[10] + ","+ data[11] + ","+ data[12] + ","+ data[13] + ","+ data[14] + ","+ data[15] + ","+ data[16] + ","
                        + data[17] + ","+ data[18] + ","+ data[19] + ","+ data[20] + ","+ data[21] + ")";
                System.out.println(sql);
                int rs = stmt.executeUpdate(sql);
                if(rs == 0){
                    System.out.println("插入数据库失败");
                }
                else{
                    System.out.println("插入数据库成功");
                }
                System.out.println("数据插入时间：" + new Date());
                Thread.sleep(60*1000);
            }
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