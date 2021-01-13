package com.lcinshu.clbeanioc.dao;

import com.lcinshu.clbeanioc.pojo.Account;

/**
 * @author: licheng
 * @Date: 2020/12/27
 * @Desc:
 */
public interface AccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;
}
