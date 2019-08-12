/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 用途：系统实体
 * 作者: lishuyi
 * 时间: 2018/1/30  21:49
 *
 * System.out.println(LocalDateTime.now());//获取当前时间
 * System.out.println(LocalDateTime.now().toLocalTime());//获取当前的 时分秒毫秒
 * System.out.println(LocalDateTime.now().toLocalDate());//获取当前的日期
 * System.out.println(LocalTime.now().withNano(0));//去除毫秒
 * Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());//LocationDateTime 转时间戳
 * LocalDateTime.now().plusHours(1L);//当前时间加一个小时
 * LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() = System.currentTimeMillis()
 *  LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
 * System.out.println(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); ==> 2018-06-21T21:27:33.236

 * DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
 * LocalDateTime time = LocalDateTime.now();
 * String localTime = df.format(time);
 * LocalDateTime ldt = LocalDateTime.parse("2017-09-28 17:07:05",df);
 * System.out.println("LocalDateTime转成String类型的时间："+localTime);
 * System.out.println("String类型的时间转成LocalDateTime："+ldt);
 */
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class SystemModel extends GenericModel {

    //创建时间
    @Setter
    @Getter
//    @Column(nullable = false, updatable = false)
    @Column(nullable = false)//确保可以更新，在逻辑删除数据后，再次恢复数据有可能创建时间不一样
    @CreationTimestamp// 创建时自动更新时间
    @JsonFormat(pattern = ApplicationConstants.FORMAT_DATE_TIME, timezone = ApplicationConstants.FORMAT_TIME_ZONE)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdTime;

    //最后更新时间
    @Setter
    @Getter
    @Column(nullable = false)
    @UpdateTimestamp// 更新时自动更新时间
    @JsonFormat(pattern = ApplicationConstants.FORMAT_DATE_TIME, timezone = ApplicationConstants.FORMAT_TIME_ZONE)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime modifiedTime;

}
