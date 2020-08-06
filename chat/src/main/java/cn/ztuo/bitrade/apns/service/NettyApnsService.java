package cn.ztuo.bitrade.apns.service;

import cn.ztuo.bitrade.apns.model.ApnsConfig;
import cn.ztuo.bitrade.apns.model.PushNotification;
import cn.ztuo.bitrade.apns.netty.NettyApnsConnection;
import cn.ztuo.bitrade.apns.netty.NettyApnsConnectionPool;

/**
 * @description: NettyApnsService
 * @author: MrGao
 * @create: 2019/09/04 11:37
 */
public class NettyApnsService extends AbstractApnsService {
    private NettyApnsConnectionPool connectionPool;

    private NettyApnsService(ApnsConfig config) {
        super(config);
        this.connectionPool = new NettyApnsConnectionPool(config);
    }

    public static NettyApnsService create(ApnsConfig apnsConfig) {
        return new NettyApnsService(apnsConfig);
    }

    @Override
    public void sendNotification(PushNotification notification) {
        this.executorService.execute(() -> {
            NettyApnsConnection connection = null;

            try {
                connection = this.connectionPool.acquire();
                if (connection != null) {
                    boolean result = connection.sendNotification(notification);
                    if (!result) {
                        log.debug("发送通知失败");
                    }
                }
            } catch (Exception var7) {
                log.error("sendNotification", var7);
            } finally {
                this.connectionPool.release(connection);
            }

        });
    }
    @Override
    public boolean sendNotificationSynch(PushNotification notification) {
        NettyApnsConnection connection = null;

        boolean var4;
        try {
            connection = this.connectionPool.acquire();
            if (connection == null) {
                return false;
            }

            boolean result = connection.sendNotification(notification);
            var4 = result;
        } catch (Exception var8) {
            log.error("sendNotification", var8);
            return false;
        } finally {
            this.connectionPool.release(connection);
        }

        return var4;
    }
    @Override
    public void shutdown() {
        this.connectionPool.shutdown();
        super.shutdown();
    }
}

