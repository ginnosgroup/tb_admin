package org.zhinanzhen.b.service;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/12/22 上午 11:29
 * Description:
 * Version: V1.0
 */
public enum ServicePackageTypeEnum {

    CA("职业评估"), EOI("EOI"), SA("学校申请"), VA("签证申请"), ZD("州担"),
    TM("提名"), DB("担保");

    private String comment;

    ServicePackageTypeEnum(String comment){
        this.comment = comment;
    }

    public static String getServicePackageTypeComment(String name){
        for (ServicePackageTypeEnum e : ServicePackageTypeEnum.values()){
            if (e.toString().equals(name))
                return e.comment;
        }
        return "";
    }

    public static ServicePackageTypeEnum getServicePackageTypeEnum(String name){
        for (ServicePackageTypeEnum e : ServicePackageTypeEnum.values()){
            if (e.toString().equals(name))
                return e;
        }
        return null;
    }

    public String getComment(){
        return comment;
    }
}
