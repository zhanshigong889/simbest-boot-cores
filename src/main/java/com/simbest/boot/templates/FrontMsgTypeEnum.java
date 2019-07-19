package com.simbest.boot.templates;

/**
 * <strong>Title : FrontMsgTypeEnum</strong><br>
 * <strong>Description : 前端消息提示框枚举</strong><br>
 * <strong>Create on : 2019-7-19</strong><br>
 * <strong>Modify on : 2019-7-19</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
public enum FrontMsgTypeEnum {

    SUCESS("成功提示",0),ERROR("错误提示",-1),WARN("警告信息",1),INFO("正常",2);

    private String name;
    private int stateNum;

    FrontMsgTypeEnum ( String name, int stateNum ) {
        this.name = name;
        this.stateNum = stateNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStateNum() {
        return stateNum;
    }

    public void setStateNum(int stateNum) {
        this.stateNum = stateNum;
    }

    public static String getTypeName(int stateNum){
        for (FrontMsgTypeEnum cTypeEnum:FrontMsgTypeEnum.values()) {
            if(cTypeEnum.getStateNum() == stateNum){
                return cTypeEnum.getName();
            }
        }
        return null;
    }
}
