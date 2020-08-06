package cn.ztuo.bitrade.apns.netty.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.concurrent.TimeUnit;

/**
 * @description: Http2SettingsHandler
 * @author: MrGao
 * @create: 2019/09/04 11:33
 */
public class Http2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
    private ChannelPromise promise;

    Http2SettingsHandler(ChannelPromise promise) {
        this.promise = promise;
    }

    public void awaitSettings(long timeout, TimeUnit unit) throws Exception {
        if (!this.promise.awaitUninterruptibly(timeout, unit)) {
            throw new IllegalStateException("Timed out waiting for settings");
        } else if (!this.promise.isSuccess()) {
            throw new RuntimeException(this.promise.cause());
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
        this.promise.setSuccess();
        ctx.pipeline().remove(this);
    }
}
