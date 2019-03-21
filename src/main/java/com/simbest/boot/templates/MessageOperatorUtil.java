package com.simbest.boot.templates;

import org.springframework.stereotype.Component;

/**
 * <strong>Title : MessageOperatorUtil</strong><br>
 * <strong>Description : 消息操作工具类</strong><br>
 * <strong>Create on : 2019/2/21</strong><br>
 * <strong>Modify on : 2019/2/21</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Component
public class MessageOperatorUtil {

    /**
     *  查询流程类的消息
     * @param messageCode   消息模板编码
     * @return
     */
    public String queryWorkFlowMessage(Long messageCode){
        String messageCodeStr = String.valueOf(Math.abs( messageCode ));
        String wfMessage = MessageEnum.getMessageByName( "W",messageCodeStr );
        return wfMessage;
    }
}
