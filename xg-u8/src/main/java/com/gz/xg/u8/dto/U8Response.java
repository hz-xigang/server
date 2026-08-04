package com.gz.xg.u8.dto;

import lombok.Data;

import java.util.List;

/**
 * 用友接口统一响应格式
 */
@Data
public class U8Response<T> {

    /**
     * 返回标识：0-失败，1-成功
     */
    private String code;

    /**
     * 返回信息（执行成功或失败，失败提供报错详情）
     */
    private String returnMessage;

    /**
     * 响应数据
     */
    private List<T> data;

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return "1".equals(code);
    }
}
