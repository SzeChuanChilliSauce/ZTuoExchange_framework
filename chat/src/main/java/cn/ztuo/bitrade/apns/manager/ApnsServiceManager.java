package cn.ztuo.bitrade.apns.manager;

import cn.ztuo.bitrade.apns.model.ApnsConfig;
import cn.ztuo.bitrade.apns.service.ApnsService;
import cn.ztuo.bitrade.apns.service.NettyApnsService;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: ApnsServiceManager
 * @author: MrGao
 * @create: 2019/09/04 11:21
 */
public class ApnsServiceManager {
    private static Map<String, ApnsService> serviceMap = new HashMap();

    public ApnsServiceManager() {
    }

    public static ApnsService createService(ApnsConfig config) {
        checkConfig(config);
        String name = config.getName();
        ApnsService apnsService = (ApnsService)serviceMap.get(name);
        if (apnsService == null) {
            synchronized(name.intern()) {
                if (apnsService == null) {
                    apnsService = NettyApnsService.create(config);
                    serviceMap.put(name, apnsService);
                }
            }
        }

        return (ApnsService)apnsService;
    }

    public static ApnsService getService(String name) {
        return (ApnsService)serviceMap.get(name);
    }

    private static void checkConfig(ApnsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置为空,请检查");
        } else if (config.getKeyStore() == null) {
            throw new IllegalArgumentException("证书不正确,请检查");
        } else if (config.getPassword() != null && !"".equals(config.getPassword().trim())) {
            if (config.getPoolSize() <= 0) {
                throw new IllegalArgumentException("池大小必须为正数,请检查");
            } else if (config.getRetries() <= 0) {
                throw new IllegalArgumentException("重试次数必须为正数,请检查");
            } else if (config.getCacheLength() <= 0) {
                throw new IllegalArgumentException("缓存长度必须为正数,请检查");
            } else if (config.getName() != null && !"".equals(config.getName().trim())) {
                if (config.getTopic() == null || "".equals(config.getTopic().trim())) {
                    throw new IllegalArgumentException("标题,即证书的bundleID不能为空,请检查");
                }
            } else {
                throw new IllegalArgumentException("服务名为必填项,请检查");
            }
        } else {
            throw new IllegalArgumentException("密码为空,请检查");
        }
    }
}
