package com.wolfbe.seacos.util;

import com.wolfbe.seacos.anno.Exclude;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/28
 */
@Exclude
public class Dom4jXmlParse {
    private static final Logger logger = LoggerFactory.getLogger(Dom4jXmlParse.class);
    private Document document;
    private Element root;

    /**
     * 构造方法，指定XML文件，不校验文件的正确性.
     * @param xmlFile  xml文件的路径
     */
    public Dom4jXmlParse(String xmlFile) {
        this(xmlFile,false);
    }

    /**
     * 构造方法，指定XML文件名，同时可以指定是否做校验.
     * @param xmlFile    xml文件的路径
     * @param validate   是否校验XML文件的正确性
     */
    public Dom4jXmlParse(String xmlFile,boolean validate) {
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(new File(xmlFile));
            root = document.getRootElement();
        } catch (DocumentException ex) {
            logger.error("Dom4jXmlParse constructor init error",ex);
            throw new RuntimeException("Dom4jXmlParse parse xml file error, " + ex.getMessage());
        }
    }

    public Element getRoot(){
        return root;
    }
}
