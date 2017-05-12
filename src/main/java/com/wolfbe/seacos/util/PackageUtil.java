package com.wolfbe.seacos.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * 包的工具类，用于获取包的类文件
 *
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/27
 */
public class PackageUtil {
    private static final Logger logger = LoggerFactory.getLogger(PackageUtil.class);
    private static List<String> EMPTY_LIST = new ArrayList<String>(0);

    /**
     * 获取指定包路径下的所有类对象
     *
     * @param path 包路径
     * @return
     */
    public static List<String> getClassesInPackage(String path) {
        return getClassesInPackage(path, EMPTY_LIST, EMPTY_LIST);
    }

    /**
     * 获取指定包路径下的所有类对象
     *
     * @param path 包路径
     * @return
     */
    public static List<String> getClassesInPackage(String path, List<String> includes, List<String> excludes) {
        String packPre = path;     //包目录
        boolean recursion = false; //递归标识
        if (path.endsWith(".*")) { //递归判断
            packPre = packPre.substring(0, packPre.lastIndexOf(".*"));
            recursion = true;
        }

        List<String> list = new ArrayList<String>();
        try {
            String packDir = packPre.replace(".", "/");
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packDir);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
//                System.out.println("protocol:" + protocol + ", file:" + URLDecoder.decode(url.getFile(), "UTF-8") + ", path:" + url.getPath());
                if ("file".equalsIgnoreCase(protocol)) {      //目录
                    getClassesInDirPackage(packPre, URLDecoder.decode(url.getFile(), "UTF-8"), recursion,
                            includes, excludes, list);
                } else if ("jar".equalsIgnoreCase(protocol)) { //jar
                    getClassesInJarPackage(url, packDir, recursion, includes, excludes, list);
                }
            }
        } catch (IOException e) {
            logger.error("getClassesInPackage error, path is " + path, e);
        }
        return list;
    }

    /**
     * 通过遍历目录的方式查找符合要求的包下的class
     *
     * @param packName 包名（包的层次用 . 分隔）
     * @param packPath 包路径（包的层次用 / 分隔）
     * @param includes 包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param excludes 不包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param classes  类名列表，类名为长类名
     */
    public static void getClassesInDirPackage(String packName, String packPath, final boolean recursion,
                                              List<String> includes, List<String> excludes, List<String> classes) {
        final File file = new File(packPath);
        if (!file.exists()) {
            return;
        }
        //获取目录及带class后缀的文件
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (recursion && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File fl : files) {
            if (fl.isDirectory()) {
                //递归获取目录下的类文件
                getClassesInDirPackage(packName + "." + fl.getName(), fl.getAbsolutePath(), recursion, includes,
                        excludes, classes);
            } else {
                String className = fl.getName().substring(0, fl.getName().length() - 6);
                includeOrExcudeClass(packName, className, includes, excludes, classes);
            }
        }
    }

    /**
     * 通过遍历目录的方式查找符合要求的jar包下的class
     *
     * @param url       jar包URL
     * @param packDir   包前缀
     * @param recursion 递归标识
     */
    public static void getClassesInJarPackage(URL url, String packDir, boolean recursion, List<String> includes,
                                              List<String> excludes, List<String> classes) throws IOException {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (name.startsWith(packDir) && name.endsWith(".class") && !entry.isDirectory()) {
                String clazzName = name.substring(packDir.length() + 1, name.length());
                if (recursion || !clazzName.contains("/")) {
                    includeOrExcudeClass(name.replace("/", "."), includes, excludes, classes);
                }

            }
        }
    }

    /**
     * 判断类文件是否满足条件，如果满足则加入类文件列表
     *
     * @param packName  包名
     * @param className 类名（短类名，不包含package前缀）
     * @param includes  包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param excludes  不包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param classes   类名列表，类名为长类名
     */
    private static void includeOrExcudeClass(String packName, String className, List<String> includes, List<String> excludes,
                                             List<String> classes) {
        String clazz = packName + "." + className;
        includeOrExcudeClass(clazz, includes, excludes, classes);
    }

    /**
     * 判断类文件是否满足条件，如果满足则加入类文件列表
     *
     * @param clazz    类名（完整类名，包含package前缀）
     * @param includes 包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param excludes 不包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param classes  类名列表，类名为长类名
     */
    private static void includeOrExcudeClass(String clazz, List<String> includes, List<String> excludes,
                                             List<String> classes) {
        if (isIncludeOrExcude(clazz, includes, excludes)) {
            classes.add(clazz);
        }
    }

    /**
     * 当clazz在includes列表的范围内且不在excludes的范围内时，返回true
     *
     * @param clazz    类名（长类名，包含package前缀）
     * @param includes 包含的类名(长类名，包含package前缀)，允许正则表达式
     * @param excludes 不包含的类名(长类名，包含package前缀)，允许正则表达式
     * @return
     */
    private static boolean isIncludeOrExcude(String clazz, List<String> includes, List<String> excludes) {
        boolean result = false;
        if (includes.isEmpty() && excludes.isEmpty()) {
            result = true;
        } else {
            boolean isInclude = isFind(clazz, includes);
            boolean isExclued = isFind(clazz, excludes);
            if (isInclude && !isExclued) {
                result = true;
            } else if (isExclued) {
                result = false;
            } else {
                result = includes.isEmpty();
            }
        }
        return result;
    }

    public static boolean isFind(String path, List<String> list) {
        for (String repStr : list) {
            if (Pattern.matches(repStr, path)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        List<String> clazzs = getClassesInPackage("com.yy.ent.cherroot.core");
        String sss = "com.*";
        System.out.println(Pattern.matches(sss, "com.com"));
        System.out.println(Pattern.matches(sss, "com.st.cod"));

        for (String str : clazzs) {
            System.out.println("class : " + str);
        }
    }
}
