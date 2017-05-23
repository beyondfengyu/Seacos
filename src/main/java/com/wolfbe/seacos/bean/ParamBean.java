package com.wolfbe.seacos.bean;

/**
 *
 * @author Andy
 * @version 1.0
 * @date 2016/6/28
 */
public class ParamBean {
    // 引用，对应method元素中ref属性，可以是PathBean值，或者ClassBean中的ID
    private String ref;
    // 类型，对应method元素中type属性
    private String type;
    // 值，对应method元素中value属性
    private String value;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
