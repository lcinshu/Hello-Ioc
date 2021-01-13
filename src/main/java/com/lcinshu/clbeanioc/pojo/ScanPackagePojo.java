package com.lcinshu.clbeanioc.pojo;

import java.util.List;

/**
 * @author: licheng
 * @Date: 2021/1/7 20:46
 * @Desc:
 */
public class ScanPackagePojo {
    List<String> scanPath;
    String scanPackage;

    public List<String> getScanPath() {
        return scanPath;
    }

    public void setScanPath(List<String> scanPath) {
        this.scanPath = scanPath;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }
}
