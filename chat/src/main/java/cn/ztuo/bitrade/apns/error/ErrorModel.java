package cn.ztuo.bitrade.apns.error;

import cn.ztuo.bitrade.apns.model.PushNotification;
import com.alibaba.fastjson.JSON;

/**
 * @description: ErrorModel
 * @author: MrGao
 * @create: 2019/09/04 11:20
 */
public class ErrorModel {
    private int code;
    private String appName;
    private PushNotification notification;

    public ErrorModel() {
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public PushNotification getNotification() {
        return this.notification;
    }

    public void setNotification(PushNotification notification) {
        this.notification = notification;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
