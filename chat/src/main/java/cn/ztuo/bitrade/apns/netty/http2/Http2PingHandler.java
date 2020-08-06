package cn.ztuo.bitrade.apns.netty.http2;

import cn.ztuo.bitrade.apns.model.PingMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;

/**
 * @description: Http2PingHandler
 * @author: MrGao
 * @create: 2019/09/04 11:33
 */
public class Http2PingHandler extends ChannelOutboundHandlerAdapter {
    private Http2ConnectionEncoder encoder;

    Http2PingHandler(Http2ConnectionEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof PingMessage)) {
            ctx.write(msg, promise);
        } else {
            this.encoder.writePing(ctx, false, Http2CodecUtil.emptyPingBuf().readLong(), promise);
            ctx.channel().flush();
        }
    }
}