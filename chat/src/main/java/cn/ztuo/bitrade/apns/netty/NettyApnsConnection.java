package cn.ztuo.bitrade.apns.netty;

import cn.ztuo.bitrade.apns.model.PushNotification;
import cn.ztuo.bitrade.apns.netty.http2.Http2ClientInitializer;
import cn.ztuo.bitrade.apns.netty.http2.Http2SettingsHandler;
import cn.ztuo.bitrade.apns.netty.http2.HttpResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: NettyApnsConnection
 * @author: MrGao
 * @create: 2019/09/04 11:29
 */
public class NettyApnsConnection {
    private static final Logger log = LoggerFactory.getLogger(NettyApnsConnection.class);
    private static final int INITIAL_STREAM_ID = 3;
    private int retryTimes;
    private KeyManagerFactory keyManagerFactory;
    private Channel channel;
    private AtomicInteger streamId = new AtomicInteger(3);
    private Http2ClientInitializer http2ClientInitializer;
    public NettyApnsConnectionPool connectionPool;
    private String host;
    private String name;
    private int port;
    private int timeout;
    private String topic;

    public NettyApnsConnection(String name, String host, int port, int retryTimes, int timeout, String topic, KeyManagerFactory keyManagerFactory) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.retryTimes = retryTimes;
        this.timeout = timeout;
        this.topic = topic;
        this.keyManagerFactory = keyManagerFactory;
    }

    public void setConnectionPool(NettyApnsConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public boolean sendNotification(PushNotification notification) {
        if (this.connectionPool.isShutdown()) {
            log.error("线程池已经死掉");
            return false;
        } else {
            if (this.channel == null || !this.channel.isActive()) {
                try {
                    this.initializeNettyClient();
                } catch (Exception var7) {
                    log.error("initializeNettyClient", var7);
                    this.http2ClientInitializer = null;
                    return false;
                }
            }

            FullHttpRequest request = null;
            HttpResponseHandler responseHandler = this.http2ClientInitializer.responseHandler();
            if (request == null) {
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "https://" + this.host + "/3/device/" + notification.getToken(), Unpooled.copiedBuffer(notification.getPayload().toString().getBytes()));
                request.headers().add("apns-topic", this.topic);
                if (this.streamId.get() > 200000000) {
                    this.streamId.set(3);
                }

                int streamId = this.streamId.getAndAdd(2);
                ChannelFuture channelFuture = this.channel.writeAndFlush(request);
                responseHandler.put(streamId, notification, channelFuture, this.channel.newPromise());
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    public void operationComplete(Future<? super Void> future) throws Exception {
                    }
                });
                channelFuture.awaitUninterruptibly();
                boolean success = channelFuture.isSuccess();
                return success;
            } else {
                return false;
            }
        }
    }

    private void initializeNettyClient() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Http2ClientInitializer http2ClientInitializer = new Http2ClientInitializer(this.name, this.createSslContext(), 2147483647);
        Bootstrap bootstrap = new Bootstrap();
        ((Bootstrap)((Bootstrap)((Bootstrap)bootstrap.group(workerGroup)).channel(NioSocketChannel.class)).option(ChannelOption.SO_KEEPALIVE, true)).remoteAddress(this.host, this.port).handler(http2ClientInitializer);
        log.info("connecting to " + this.host);
        this.channel = bootstrap.connect().syncUninterruptibly().channel();
        log.info("connected");
        Http2SettingsHandler http2SettingsHandler = http2ClientInitializer.settingsHandler();
        log.info("await setting");
        http2SettingsHandler.awaitSettings(30L, TimeUnit.SECONDS);
        log.info("setting success");
        this.streamId.set(3);
        this.http2ClientInitializer = http2ClientInitializer;
    }

    private SslContext createSslContext() throws SSLException {
        SslProvider provider = SslProvider.JDK;
        return SslContextBuilder.forClient().sslProvider(provider).ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE).trustManager(InsecureTrustManagerFactory.INSTANCE).keyManager(this.keyManagerFactory).applicationProtocolConfig(ApplicationProtocolConfig.DISABLED).build();
    }
}
