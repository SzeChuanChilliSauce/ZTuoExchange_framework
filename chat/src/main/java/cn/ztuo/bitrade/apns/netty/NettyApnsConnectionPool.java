package cn.ztuo.bitrade.apns.netty;

import cn.ztuo.bitrade.apns.model.ApnsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description: NettyApnsConnectionPool
 * @author: MrGao
 * @create: 2019/09/04 11:30
 */
public class NettyApnsConnectionPool {
    private static final Logger log = LoggerFactory.getLogger(NettyApnsConnectionPool.class);
    private static final String HOST_DEVELOPMENT = "api.development.push.apple.com";
    private static final String HOST_PRODUCTION = "api.push.apple.com";
    private static final String ALGORITHM = "sunx509";
    private static final String KEY_STORE_TYPE = "PKCS12";
    private static final int PORT = 2197;
    private volatile boolean isShutdown;
    public BlockingQueue<NettyApnsConnection> connectionQueue;

    public NettyApnsConnectionPool(ApnsConfig config) {
        int poolSize = config.getPoolSize();
        this.connectionQueue = new LinkedBlockingQueue(poolSize);
        String host = config.isDevEnv() ? "api.development.push.apple.com" : "api.push.apple.com";
        KeyManagerFactory keyManagerFactory = this.createKeyManagerFactory(config);

        for(int i = 0; i < poolSize; ++i) {
            NettyApnsConnection connection = new NettyApnsConnection(config.getName(), host, 2197, config.getRetries(), config.getTimeout(), config.getTopic(), keyManagerFactory);
            connection.setConnectionPool(this);
            this.connectionQueue.add(connection);
        }

    }

    private KeyManagerFactory createKeyManagerFactory(ApnsConfig config) {
        try {
            char[] password = config.getPassword().toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(config.getKeyStore(), password);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("sunx509");
            keyManagerFactory.init(keyStore, password);
            return keyManagerFactory;
        } catch (Exception var5) {
            log.error("createKeyManagerFactory", var5);
            throw new IllegalStateException("create key manager factory failed");
        }
    }

    public NettyApnsConnection acquire() {
        try {
            return (NettyApnsConnection)this.connectionQueue.take();
        } catch (InterruptedException var2) {
            log.error("acquire", var2);
            return null;
        }
    }

    public void release(NettyApnsConnection connection) {
        if (connection != null) {
            this.connectionQueue.add(connection);
        }

    }

    public void shutdown() {
        this.isShutdown = true;
    }

    public boolean isShutdown() {
        return this.isShutdown;
    }
}
