package com.simbest.boot.templates;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @功能描述 消息模板枚举类。
 * @date 20180108
 * @author LJW
 * 
 */
public enum MessageEnum {

	/**
	 * 消息分类：字母+数字的格式
     *      字母部分含义：
     *          第一个字母
     *              M 代码是消息提示，
     *          第二字母
     *              S:表示系统级别上的提示；
     *              W:表示所属流程操作提示；
     *              B:表示所属业务操作提示；
     *              T:表示是模板类的消息提示
     *      数字部门含义：按照功能顺序排列
	 * 
	 * <pre>
     * 所属模块/流程：
     * MW000001("操作${content}失败!")
	 * </pre>
     *
	 * <pre>
	 * 所属模块/业务：
     * MB000001("操作成功!")
     * </pre>
     *
     * <pre>
     * 所属模块/模板类提示信息：
     * MT000001("协作配合:您收到汪祥向您发送的"关于台前分公司拆除楼顶消防水箱的请示"的协作配合事件，请及时处理。")
     * </pre>
	 */

    /**
     * 正在建设中，敬请期待。
     */
    MS000000("系统正在建设中，敬请期待！" ),

	/**
	 * 无法预览该文件！
	 */
    MB000001("无法预览该文件！" ),

	/**
	 * 正在${operation}，请勿重复操作！
	 */
    MB000002("正在${operation}，请勿重复操作！"),

    /**
     * 上传失败，请重新上传!
     */
    MB000003("${fileName} 上传失败，请确认后重新上传！"),

    /**
     * 文件导入成功！
     */
    MB000004("文件导入成功！"),

    /**
     * 文件导入失败！
     */
    MB000005("文件导入失败！"),

	/**
	 * @param companyName	        : 公司名称。
	 * @param departmentName		: 部门名称。
	 * @param trueName			    : 审批人姓名。
     * @param positionName			: 职务名称。
	 * @消息内容 工单已流转至${companyName}${departmentName}-${trueName}${positionName}进行办理。
	 */
    MW000001("工单已流转至${companyName}${departmentName}-${trueName}${positionName}进行办理"),

	/**
	 * 流程提交失败。
	 */
    MW000002("流程提交失败！"),

    /**
     * @param appName	            : 系统名称。
     * @param fromUser		        : 发送人。
     * @param itemSubject			: 事项主题。
     * @消息内容 协作配合:您收到汪祥向您发送的"关于台前分公司拆除楼顶消防水箱的请示"的事件，请及时处理。
     */
    MT000001("${appName}:您收到${fromUser}向您发送的[${itemSubject}]的${appName}事件，请及时处理。"),

    /**
     * @param appName       系统名称
     * @param managerUser   管理员类型
     * @param documentType  档案类型
     * @消息内容 专利管理提醒：档案室管理员提醒您，您借阅的专利实物档案已经到期，请及时归还
     */
    MT000002("${appName}提醒:${managerUser}提醒您，您借阅的${documentType}已经到期，请及时归还。"),

    /**
     * @param appName       系统名称
     * @param managerUser   管理员类型
     * @param documentType  档案类型
     * @消息内容 专利管理提醒：档案室管理员提醒您，请及时归还您借阅的专利实物档案
     */
    MT000003("${appName}提醒:${managerUser}提醒您，请及时归还您借阅的{documentType}。"),

    /**
     * @param appName           系统名称
     * @param serialNumber      编号
     * @param approveResult     审批结果
     * @消息内容 专利管理提醒：档案室管理员提醒您，请及时归还您借阅的专利实物档案
     */
    MT000004("【${appName}】您提交的“${serialNumber}”${appName}申请，已完成审批。审批结果：${approveResult}。请登录系统查看"),

    /**
     * 工单流转时有代理人的提示信息
     * @param companyName	        : 公司名称。
     * @param departmentName		: 部门名称。
     * @param trueName			    : 审批人姓名。
     * @param positionName			: 职务名称。
     * @消息内容 工单已流转至${companyName}${departmentName}-${trueName}${positionName}进行办理。
     */
    MW000005("工单已流转至${companyName}${departmentName}-${trueName}${positionName}进行办理,被代理人为：${agentedCompanyName}${agentedDepartmentName}-${agentedTrueName}${agentedPositionName}"),

    /*************************************************************流程消息模板***********************************************************************/
    W21000000("流程引擎接口调用提交失败，错误信息为：【21000000】原因可能为提交数据的状态不正确或者提交人没有获取到！");



    /*************************************************************流程消息模板***********************************************************************/

    private String message;

    /**
     * 私有构造方法①，不指定消息显示类型。
     *
     * @param message
     */
    MessageEnum ( String message ) {
        this.message = message;
    }

	/**
	 * 替换参数的正则。<br>
	 */
	public static final Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");

	/**
	 * 解析Enum，生成前台json格式字符串。
	 * @return
	 */
	public static String getObjStrForJS(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{");
		for (MessageEnum enum1 : MessageEnum.values()) {
			/*
			 * 单个消息模板的结构如下：
			"CONFIRM_COMMIT" :{
				"id" : id,
				"message" : message
			};
			 */
			stringBuffer.append(", ");
			stringBuffer.append("\"" + enum1.name() + "\" : ");
			stringBuffer.append("{");
			stringBuffer.append("\"id\" : " + enum1.ordinal());
			stringBuffer.append(",");
			stringBuffer.append("\"message\" : \"" + enum1.message + "\"");
			stringBuffer.append("}");
		}
		// 替换第2个字符即首个逗号","。
		stringBuffer.replace(1, 2, "");
		stringBuffer.append("}");
		return stringBuffer.toString();
	}

	/**
	 * 无参调用。
	 * 
	 * @return
	 */
	public String getMessage() {
		return pattern.matcher(message).replaceAll("");
	}
	
	/**
	 * 有参调用，map内的参数请参考Enum实例的注释。
	 * 
	 * @return
	 */
	public String getMessage( CharSequence onlyParam) {
		if (onlyParam != null) {
			Matcher matcher = pattern.matcher(this.message);
			StringBuffer sbuffer = new StringBuffer();
			while (matcher.find()) {
                //将当前匹配子串替换为指定字符串，并且将替换后的子串以及其之前到上次匹配子串之后的字符串段添加到一个StringBuffer对象里，而appendTail(StringBuffer sb) 方法则将最后一次匹配工作后剩余的字符串添加到一个StringBuffer对象里。
                matcher.appendReplacement(sbuffer, onlyParam.toString());  //使用循环将句子里所有的 onlyParam 找出并替换再将内容加到sb里
			}
			matcher.appendTail(sbuffer);  //最后一次匹配后的剩余字符串加到sbuffer里
			return sbuffer.toString();
		}
		return this.getMessage();
	}

	/**
	 * 有参调用，map内的参数请参考Enum实例的注释。
	 * 
	 * @param paramMap
	 * @return
	 */
	public String getMessage( Map<String, ? extends Object> paramMap) {
		if (paramMap != null && !paramMap.isEmpty()) {
			Matcher matcher = pattern.matcher(this.message);
			StringBuffer sbuffer = new StringBuffer();
			while (matcher.find()) {
				if (matcher.groupCount() > 0) {
					String paramKey = matcher.group(1); // ${xx}  返回第一组匹配到的子字符串
					Object value = paramMap.get(paramKey);
					matcher.appendReplacement(sbuffer, value == null ? "" : value.toString());
				}
			}
			matcher.appendTail(sbuffer);
			return sbuffer.toString();
		}
		return this.getMessage();
	}

    /**
     * 根据name获取消息
     * @param stateNum
     * @return
     */
    public static String getMessageByName(String prefix,String stateNum){
	    String stateNumStr = prefix + stateNum;
        for (MessageEnum messageEnum:MessageEnum.values()) {
            if(messageEnum.name().equals( stateNumStr )){
                return messageEnum.getMessage();
            }
        }
        return null;
    }

	@Override
	public String toString() {
		return this.message;
	}

	public static void main( String... s) {
		System.out.println(MessageEnum.MS000000.getMessage());
		Map<String, String> paramMap = Maps.newHashMap();
		//${companyName}${departmentName}-{trueName}${positionName}
		paramMap.put("companyName", "郑州分公司");
        paramMap.put("departmentName", "人力资源部");
        paramMap.put("trueName", "毛然");
        paramMap.put("positionName", "经理");
		System.out.println(MessageEnum.MW000001.getMessage(paramMap));
		System.out.println(MessageEnum.MB000002.getMessage("文件上传"));
		System.out.println(MessageEnum.values().length + " 个消息模板");
        paramMap = Maps.newHashMap();
        paramMap.put("appName", "协作配合");
        paramMap.put("fromUser", "汪祥");
        paramMap.put("itemSubject", "关于台前分公司拆除楼顶消防水箱的请示");
        System.out.println(MessageEnum.MT000001.getMessage(paramMap));
		System.out.println(getObjStrForJS());
        System.out.println(MessageEnum.MT000001.getMessage("111"));
        System.out.println( "000001>>>" + MessageEnum.getMessageByName( "W","000001" ) );
    }
}
