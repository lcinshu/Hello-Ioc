package com.lcinshu.clbeanioc.circledepend;

import com.lcinshu.clbeanioc.annotation.Autowired;
import com.lcinshu.clbeanioc.annotation.Service;

/**
 * @author: licheng
 * @Date: 2021/1/7 22:58
 * @Desc:
 */
@Service
public class Cclass {

    @Autowired
    private Bclass bclass;

    public Bclass getBclass() {
        return bclass;
    }

    public void setBclass(Bclass bclass) {
        this.bclass = bclass;
    }
}
