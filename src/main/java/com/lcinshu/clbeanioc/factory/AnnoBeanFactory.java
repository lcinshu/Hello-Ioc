package com.lcinshu.clbeanioc.factory;

import com.lcinshu.clbeanioc.annotation.Autowired;
import com.lcinshu.clbeanioc.annotation.ComponentScan;
import com.lcinshu.clbeanioc.annotation.Service;
import com.lcinshu.clbeanioc.annotation.Transactional;
import com.lcinshu.clbeanioc.pojo.ScanPackagePojo;
import com.mysql.jdbc.StringUtils;
import org.junit.Assert;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: licheng
 * @Date: 2020/12/27 20:29
 * @Desc:
 */
@ComponentScan(scanPackages = "com.lcinshu.clbeanioc")
//@ComponentScan(scanPackages = "com.lcinshu.clbeanioc.circledepend")
public class AnnoBeanFactory {

    private static String basePath = AnnoBeanFactory.class.getClassLoader().getResource(".").getPath();

    // 由于后续使用递归完成依赖填充，需要频繁修改这几个map，使用hashmap会出现并发修改的问题，因此使用ConcurrentHashMap
    /**
     * 缓存尚未实例化完成的对象
     */
    private static Map<String, Object> initialCacheMap = new ConcurrentHashMap<>();
    /**
     * 缓存已经实例化完成的对象
     */
    private static Map<String, Object> finishedCacheMap = new ConcurrentHashMap<>();

    /**
     * 中间缓存map，用于解决循环依赖
     */
    private static Map<String, Object> finishProcessMap = new ConcurrentHashMap<>();

    static {
        System.out.println("初始化注入开始……………………");
        /**
         * 扫描指定目录，并将目录下@Service注解的类放入map中
         */
        scanPackageBeans();
        /**
         * 遍历上面放入其中的实例对象，递归扫描@Autowired注解标注的属性，并将依赖的实例对象通过set方法进行填充
         */
        initialCacheMap.forEach(AnnoBeanFactory::dependOnBeans);
        /**
         * 遍历finishedMap，处理@Transactinal注解标注，判断是否存在事务注解，如果存在，就使用动态代理生成对象并替换finishedmap中对应的bean
         */
        finishedCacheMap.forEach(AnnoBeanFactory::transactionBeans);
    }

    public static Object getFinishedBean(String beanName) {
        return finishedCacheMap.get(beanName);
    }

    public static Object getUnfinishedBean(String beanName) {
        return initialCacheMap.get(beanName);
    }

    /**
     * 扫描ComponentScan注解指定的 bean
     */
    private static void scanPackageBeans() {
        ScanPackagePojo scanPackagePojo = doScanPackage();
        // 获取扫描路径下所有的文件
        try {
            doScanPackageBeans(scanPackagePojo.getScanPath(), scanPackagePojo.getScanPackage());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析扫描文件夹
     * 如果指定了扫描路径就扫描对应路径，否则默认当前扫描类的路径
     * 需要注意：basePath是/分隔
     * path是"."分隔
     *
     * @return
     */
    private static ScanPackagePojo doScanPackage() {
        Class clz = AnnoBeanFactory.class;
        String thisPath = clz.getPackageName().replace(".", "/");
        List<String> scanPath = new ArrayList<>();
        String scanPackage = "";
        Annotation[] annotations = clz.getAnnotations();
        if (annotations.length == 0) {
            scanPath.add(basePath + thisPath);
        } else {
            for (Annotation annotation : annotations) {
                if (annotation instanceof ComponentScan) {
                    String[] strings = ((ComponentScan) annotation).scanPackages();
                    // 如果没有配置，则配置当前类所在目录为扫描目录
                    if (strings.length == 0) {
                        scanPackage = clz.getPackageName();
                        scanPath.add(basePath + scanPackage.replace(".", "/"));
                    } else {
                        for (String path : strings) {
                            scanPackage = path;
                            scanPath.add(basePath + scanPackage.replace(".", "/"));
                        }
                    }
                }
            }
        }
        ScanPackagePojo packagePojo = new ScanPackagePojo();
        packagePojo.setScanPath(scanPath);
        packagePojo.setScanPackage(scanPackage);
        return packagePojo;
    }

    /**
     * 递归扫描指定路径下的@Service注解的类
     * <p>
     * 递归扫描到文件，判断是否@Service注解的类，如果是，则通过反射直接实例化
     * 并将实例放入initialCacheMap中，作为后续进一步处理bean的依据
     *
     * @param paths：需要扫描的根路径列表，方法中循环用
     * @param rootPath：根路径，"."分隔
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static void doScanPackageBeans(List<String> paths, String rootPath) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (String onePath : paths) {
            File[] files = new File(onePath).listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    List<String> path = new ArrayList<>();
                    path.add(file.getPath());
                    doScanPackageBeans(path, rootPath + "." + file.getName());
                } else {
                    String className = rootPath + "." + file.getName().substring(0, file.getName().indexOf("."));
                    if (className.contains("servlet") || className.contains("$")) {
                        continue;
                    }
                    Class clz = Class.forName(className);
                    if (clz.isInterface() || clz.isAnnotation()) {
                        continue;
                    }
                    Annotation[] annotations = clz.getAnnotations();
                    // beanName 驼峰处理
                    String beanName = convertFirst(clz.getSimpleName(), 1);
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Service) {
                            String value = ((Service) annotation).value();
                            if (!StringUtils.isNullOrEmpty(value)) {
                                beanName = value;
                            }
                            // 填充以及缓存
                            initialCacheMap.put(beanName, clz.newInstance());
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理initialCacheMap中对象的依赖问题，壳方法，异常处理
     *
     * @param beanName
     * @param bean
     */
    private static void dependOnBeans(String beanName, Object bean) {
        try {
            doDependOnBeans(beanName, bean);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 依赖处理核心方法
     *
     * @param beanName
     * @param bean
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static Object doDependOnBeans(String beanName, Object bean) throws IllegalAccessException, InstantiationException {
        /**
         * 取出对应的实例
         * 如果已存在finishedCacheMap中，则说明已经实例化完成，无需再进行依赖绑定处理，直接返回
         */
        Object alreadyFinishObject = finishedCacheMap.get(beanName);
        if (alreadyFinishObject != null) {
            return alreadyFinishObject;
        }

        Class clz = bean.getClass();
        /**
         * 对field进行map处理，过滤出Autowired标注的field，方便后续取用field
         */
        Map<String, Field> fieldMap = Arrays.stream(clz.getDeclaredFields())
                .filter(cla -> cla.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toMap(Field::getName, field -> {
                    field.setAccessible(true);
                    return field;
                }));
        /**
         * >> 如果没有类中Autowired注解的属性，则直接返回实例对象，并将当前对象晋升到已完成cache map中，
         *      便于后续对象填充依赖时使用
         *
         * >> 如果类中有Autowired注解的属性，则说明当前类需要依赖注入处理，则将当前对象晋升到finishProcessMap中
         *      一来为后续依赖填充做准备，二来可以解决循环依赖问题
         */
        Object originalObj = initialCacheMap.get(beanName);

        if (fieldMap.size() == 0) {
            finishedCacheMap.put(beanName, bean);
            initialCacheMap.remove(beanName);
            return originalObj;
        } else {
            finishProcessMap.put(beanName, bean);
            initialCacheMap.remove(beanName);
        }

        // 填充依赖
        // （缺陷之一）fieldName: 默认为驼峰形式的依赖类名
        fieldMap.forEach((fieldName, field) -> {
            Autowired annotation = field.getAnnotation(Autowired.class);
            if (annotation != null) {
                Method[] methods = clz.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals("set" + convertFirst(fieldName, 0))) {
                        try {
                            /**
                             * 这里需要注意，多个map之间因为依赖关系和依赖完成度情况需要不断调整map所在位置
                             * 1. 如果initialCacheMap中获取到了对应的bean，需要递归调用当前方法，处理当前field待注入对象的依赖
                             * 2. 如果initialCacheMap未获取到对象，finishedCacheMap或finishProcessMap中获取到对象，
                             *      则表明当前对象已初始化完成/处在初始化中，则直接调用setter方法设置对象
                             *
                             *
                             * 从initialCacheMap中移动到
                             */
                            Object dependOnObj = initialCacheMap.get(fieldName);
                            // 这里先从二级缓存中获取，看下是否已经存在初始化完毕的对象
                            // 如果存在，则直接返回，否则继续递归调用
                            Object finishedOrInProcessObject = finishedCacheMap.get(fieldName);
                            if (finishedOrInProcessObject == null) {
                                finishedOrInProcessObject = finishProcessMap.get(fieldName);
                            }
                            /**
                             * 填充依赖，递归处理
                             * 递归的出口：末级没有autowired标注依赖的类，不会进入当前
                             * 正常情况下
                             */
                            method.invoke(originalObj, finishedOrInProcessObject == null ? doDependOnBeans(fieldName, dependOnObj) : finishedOrInProcessObject);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        // 如果没有被autowired注解的依赖，直接填充到finishedCacheMap中，并移除initialCacheMap中对应的对象，标识该对象已初始化完成，可以直接使用
        // 这个也是递归处理依赖的出口，依赖树上，最底层的那些没有autowired标注的类
        finishedCacheMap.put(beanName, bean);
        finishProcessMap.remove(beanName, bean);
        return originalObj;
    }

    /**
     * 处理@Transactional注解的类，返回代理对象
     * 壳方法，用于返回
     *
     * @param beanName
     * @param bean
     */
    private static void transactionBeans(String beanName, Object bean) {
        Object proxyBean = bean;
        try {
            proxyBean = doTransactionBeans(beanName, bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finishedCacheMap.put(beanName, proxyBean);
    }

    /**
     * 处理@Transactional注解的类，返回代理对象
     *
     * @param beanName
     * @param bean
     * @return
     * @throws Exception
     */
    private static Object doTransactionBeans(String beanName, Object bean) throws Exception {
        Class clz = bean.getClass();
        Method[] methods = clz.getMethods();
        for (Method method : methods) {
            Transactional annotation = method.getAnnotation(Transactional.class);
            if (annotation != null) {
                boolean isInterface = clz.getInterfaces().length > 0;
                Object o = finishedCacheMap.get(beanName);
                Assert.assertNotNull("can't find Transactional Object!", o);
                ProxyFactory proxyFactory = (ProxyFactory) finishedCacheMap.get("proxyFactory");
                return isInterface ? proxyFactory.getJdkProxy(o) : proxyFactory.getCglibProxy(o);
            }
        }
        return bean;
    }


    private static String convertFirst(String name, Integer upperOr) {
        Assert.assertNotNull("string need to be convert upper cannot be null!", name);
        String first;
        if (upperOr == 0) {
            first = (name.charAt(0) + "").toUpperCase();
        } else {
            first = (name.charAt(0) + "").toLowerCase();
        }
        return first + name.substring(1);
    }

    public static void init() {
        System.out.println("初始化注入完成……………………");
    }

    public static void main(String[] args) {

    }
}
