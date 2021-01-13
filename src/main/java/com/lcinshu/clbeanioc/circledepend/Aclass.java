package com.lcinshu.clbeanioc.circledepend;

import com.lcinshu.clbeanioc.annotation.Autowired;
import com.lcinshu.clbeanioc.annotation.Service;

/**
 * @author: licheng
 * @Date: 2021/1/7 22:57
 * @Desc:
 */
@Service
public class Aclass {

    @Autowired
    private Cclass cclass;

    public Cclass getCclass() {
        return cclass;
    }

    public void setCclass(Cclass cclass) {
        this.cclass = cclass;
    }
}
