package com.lcinshu.clbeanioc.service;

/**
 * @author: licheng
 * @Date: 2020/12/27
 * @Desc:
 */
public interface TransferService {

    void transfer(String fromCardNo,String toCardNo,int money) throws Exception;
}
