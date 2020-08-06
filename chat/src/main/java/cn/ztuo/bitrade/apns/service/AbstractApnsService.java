package cn.ztuo.bitrade.apns.service;

import cn.ztuo.bitrade.apns.model.ApnsConfig;
import cn.ztuo.bitrade.apns.model.NamedThreadFactory;
import cn.ztuo.bitrade.apns.model.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: AbstractApnsService
 * @author: MrGao
 * @create: 2019/09/04 11:37
 */
public class AbstractApnsService implements ApnsService {
    protected static final Logger log = LoggerFactory.getLogger(AbstractApnsService.class);
    private static final int EXPIRE = 900000;
    private static final AtomicInteger IDS = new AtomicInteger(0);
    protected ExecutorService executorService;

    public AbstractApnsService(ApnsConfig config) {
        this.executorService = Executors.newFixedThreadPool(config.getPoolSize(), new NamedThreadFactory(config.getName()));
    }

    @Override
    public void sendNotification(PushNotification var1) {

    }

    @Override
    public boolean sendNotificationSynch(PushNotification var1) {
        return false;
    }

    @Override
    public void shutdown() {
        this.executorService.shutdownNow();

        try {
            this.executorService.awaitTermination(6L, TimeUnit.SECONDS);
        } catch (InterruptedException var2) {
            log.error("shutdown", var2);
        }

    }
}