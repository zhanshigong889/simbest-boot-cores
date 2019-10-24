/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.simbest.boot.sys.service.ISimpleSmsService;
import com.simbest.boot.util.CodeGenerator;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.http.LocalHttpClient;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Map;


/**
 * 用途：基于容联云通讯短信平台的短信实现
 * 作者: lishuyi
 * 时间: 2019/8/9  19:24
 *
 *
 * 容联云通讯短信平台 http://www.yuntongxun.com/member/main
 * yijianshen2015@163.com  zkQNODEQ4poRoagPD2G7ng==
 *
 * @author lishuyi
 *
 */
@Slf4j
public class CloopenSmsService implements ISimpleSmsService {

    private String account;

    private String token;

    private String appId;

    private String templateId;

    public CloopenSmsService(String account, String token, String appId, String templateId) {
        this.account = account;
        this.token = token;
        this.appId = appId;
        this.templateId = templateId;
    }

    @Override
    public String getSmsConfig() {
        StringBuffer sb = new StringBuffer();
        sb.append("账户：" + account + "\n");
        sb.append("令牌：" + token + "\n");
        sb.append("应用ID：" + appId + "\n");
        sb.append("默认模板ID：" + templateId + "\n");
        return sb.toString();
    }

    @Override
    public boolean sendRandomCode(String phone, String randomCode, int minutes){
        Assert.notNull(phone, "手机号码不能为空");
        Assert.notNull(randomCode, "随机码不能为空");
        Assert.notNull(minutes, "有效时间不能为空");
        return sendContents(phone, new String[]{randomCode, String.valueOf(minutes)}, this.account, this.token, this.appId, this.templateId);
    }

    @Override
    public boolean sendRandomCode(String phone, String randomCode, int minutes, Object configs) {
        return sendContents(phone, new String[]{randomCode, String.valueOf(minutes)}, this.account, this.token, this.appId, configs.toString());
    }

    @Override
    public boolean sendContent(String phone, String[] contents, Object configs) {
        return sendContents(phone, contents, this.account, this.token, this.appId, templateId);
    }

    @Override
    public boolean sendAnyPassword(String randomCode) {
        return false;
    }

    private boolean sendContents(String phone, String[] contents, String account, String token, String appId, String templateId) {
        String baseUrl = "https://app.cloopen.com:8883/2013-12-26/Accounts/#accountSid#/SMS/TemplateSMS?sig=#SigParameter#";
        baseUrl = StringUtils.replace(baseUrl, "#accountSid#", account);
        log.debug("baseUrl: "+baseUrl);
        String now = DateUtil.getDateStr("yyyyMMddHHmmss");
        String SigParameter = account+token+now;
        log.debug("SigParameter: "+SigParameter);
        baseUrl = StringUtils.replace(baseUrl, "#SigParameter#", DigestUtils.md5Hex(SigParameter).toUpperCase());
        log.debug("baseUrl: "+baseUrl);
        Map<String, Object> paramters = Maps.newHashMap();
        paramters.put("to", phone);
        paramters.put("appId", appId);
        paramters.put("templateId", templateId);
        String retCode = CodeGenerator.randomInt(4);
        paramters.put("datas", contents);
        String jsonPara = JacksonUtils.obj2json(paramters);
        String Authorization= account+":"+now;
        log.debug("Authorization: "+Authorization);
        Authorization = Base64.encodeBase64String(Authorization.getBytes());
        log.debug("BASE64 Authorization: "+Authorization);
        MediaType contentType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .addHeader("Authorization", Authorization)
                .addHeader("Accept","application/json")
                .addHeader("Content-Type","application/json;charset=utf-8")
                .url(baseUrl) //
                .post(RequestBody.create(contentType,jsonPara)) //
                .build();
        String result = LocalHttpClient.executeStringResult(request);
        log.debug(result);
        if(result==null||result.equals("")){
            return false;
        }
        JsonNode node = JacksonUtils.json2obj(result, JsonNode.class);
        String statusCode = node.findPath("statusCode").textValue();
        if("000000".equals(statusCode)){
            return true;
        }
        else{
            log.warn("短信平台返回代码【{}】", statusCode);
            return false;
        }
    }
}
