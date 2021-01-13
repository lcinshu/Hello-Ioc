package com.lcinshu.clbeanioc.service.impl;

import com.lcinshu.clbeanioc.annotation.Autowired;
import com.lcinshu.clbeanioc.annotation.Service;
import com.lcinshu.clbeanioc.annotation.Transactional;
import com.lcinshu.clbeanioc.dao.AccountDao;
import com.lcinshu.clbeanioc.pojo.Account;
import com.lcinshu.clbeanioc.service.TransferService;

/**
 * @author: licheng
 * @Date: 2020/12/27
 * @Desc:
 */
@Service("transferService")
public class TransferServiceImpl implements TransferService {

    // 最佳状态
    @Autowired
    private AccountDao accountDao;

    // 构造函数传值/set方法传值
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    @Transactional()
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
        Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney() - money);
        to.setMoney(to.getMoney() + money);
        accountDao.updateAccountByCardNo(to);
//        int c = 1 / 0;
        accountDao.updateAccountByCardNo(from);
    }
}
