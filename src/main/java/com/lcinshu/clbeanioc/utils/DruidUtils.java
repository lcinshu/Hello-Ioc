package com.lcinshu.clbeanioc.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author 林肯
 */
public class DruidUtils {

    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();


    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/clbatis");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("12qazxsw@");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}
