/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

/**
 * 用途：封装一个简单短信接口
 * 作者: lishuyi
 * 时间: 2019/8/9  20:14
 */
public interface ISimpleSmsService {

    /**
     * 获取实现短信接口配置
     * @return
     */
    String getSmsConfig();

    /**
     * 使用默认配置（模板），发送验证码
     *
     * @param phone 接收手机号
     * @param randomCode  随机数据码
     * @param minutes 有效分钟数
     * @return
     */
    boolean sendRandomCode(String phone, String randomCode, int minutes);

    /**
     * 使用特定配置（模板），发送验证码
     *
     * @param phone 接收手机号
     * @param randomCode  随机数据码
     * @param minutes 有效分钟数
     * @return
     */
    boolean sendRandomCode(String phone, String randomCode, int minutes, Object configs);

    /**
     * 使用特定模板发送短信内容
     *
     * @param phone 接收手机号
     * @param contents 短信内容
     * @param configs 短信配置参数
     * @return
     */
    boolean sendContent(String phone, String[] contents, Object configs);


    /**
     * 向特定账号发送通用密码
     * @param randomCode
     * @return
     */
    boolean sendAnyPassword(String randomCode);

    /**
     * 发送系统健康检查告警信息
     * @param message
     */
    void sendHealthCheckMessage(String message);

}
