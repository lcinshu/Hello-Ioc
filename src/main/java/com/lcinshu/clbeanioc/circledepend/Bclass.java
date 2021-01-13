package com.lcinshu.clbeanioc.circledepend;

import com.lcinshu.clbeanioc.annotation.Autowired;
import com.lcinshu.clbeanioc.annotation.Service;

/**
 * @author: licheng
 * @Date: 2021/1/7 22:57
 * @Desc:
 */
@Service
public class Bclass {

    @Autowired
    private Aclass aclass;

    public Aclass getAclass() {
        return aclass;
    }

    public void setAclass(Aclass aclass) {
        this.aclass = aclass;
    }
}
