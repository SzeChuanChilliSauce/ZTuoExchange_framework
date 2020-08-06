package cn.ztuo.bitrade.apns.service;

import cn.ztuo.bitrade.apns.model.PushNotification;

/**
 * @description: ApnsService
 * @author: MrGao
 * @create: 2019/09/04 11:37
 */
public interface ApnsService {
    void sendNotification(PushNotification var1);

    boolean sendNotificationSynch(PushNotification var1);

    void shutdown();
}
