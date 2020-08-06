package cn.ztuo.bitrade.apns.netty.http2;

import cn.ztuo.bitrade.apns.model.PingMessage;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @description: Http2ClientInitializer
 * @author: MrGao
 * @create: 2019/09/04 11:32
 */
public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final int IDLE_TIME_SECONDS = 30;
    private final Http2FrameLogger logger;
    private final SslContext sslCtx;
    private final int maxContentLength;
    private HttpToHttp2ConnectionHandler connectionHandler;
    private HttpResponseHandler responseHandler;
    private Http2SettingsHandler settingsHandler;
    private Http2PingHandler pingHandler;
    private String name;

    public Http2ClientInitializer(String name, SslContext sslCtx, int maxContentLength) {
        this.sslCtx = sslCtx;
        this.maxContentLength = maxContentLength;
        this.name = name;
        this.logger = new SimpleHttp2FrameLogger(name);
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        Http2Connection connection = new DefaultHttp2Connection(false);
        this.connectionHandler = (new HttpToHttp2ConnectionHandlerBuilder()).frameListener(new DelegatingDecompressorFrameListener(connection, (new InboundHttp2ToHttpAdapterBuilder(connection)).maxContentLength(this.maxContentLength).propagateSettings(true).build())).frameLogger(this.logger).connection(connection).build();
        this.pingHandler = new Http2PingHandler(this.connectionHandler.encoder());
        this.responseHandler = new HttpResponseHandler(this.name);
        this.settingsHandler = new Http2SettingsHandler(ch.newPromise());
        if (this.sslCtx != null) {
            this.configureSsl(ch);
        } else {
            this.configureClearText(ch);
        }

    }

    public HttpResponseHandler responseHandler() {
        return this.responseHandler;
    }

    public Http2SettingsHandler settingsHandler() {
        return this.settingsHandler;
    }

    private void configureEndOfPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new ChannelHandler[]{this.settingsHandler, this.responseHandler, this.pingHandler});
    }

    private void configureSsl(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ChannelHandler[]{this.sslCtx.newHandler(ch.alloc())});
        pipeline.addLast(new ChannelHandler[]{new ApplicationProtocolNegotiationHandler("") {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                ChannelPipeline p = ctx.pipeline();
                p.addLast(new ChannelHandler[]{Http2ClientInitializer.this.connectionHandler});
                Http2ClientInitializer.this.configureEndOfPipeline(p);
            }
        }});
        pipeline.addLast(new ChannelHandler[]{new IdleStateHandler(2147483647, 30, 30)}).addLast(new ChannelHandler[]{new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                super.userEventTriggered(ctx, evt);
                if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
                    IdleStateEvent idleStateEvent = (IdleStateEvent)evt;
                    if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                        ctx.channel().write(PingMessage.INSTANCE);
                    }
                }

            }
        }});
    }

    private void configureClearText(SocketChannel ch) {
        HttpClientCodec sourceCodec = new HttpClientCodec();
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(this.connectionHandler);
        HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);
        ch.pipeline().addLast(new ChannelHandler[]{sourceCodec, upgradeHandler, new Http2ClientInitializer.UpgradeRequestHandler(), new Http2ClientInitializer.UserEventLogger()});
    }

    private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
        private UpgradeRequestHandler() {
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            DefaultFullHttpRequest upgradeRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            ctx.writeAndFlush(upgradeRequest);
            ctx.fireChannelActive();
            ctx.pipeline().remove(this);
            Http2ClientInitializer.this.configureEndOfPipeline(ctx.pipeline());
        }
    }

    private static class UserEventLogger extends ChannelInboundHandlerAdapter {
        private UserEventLogger() {
        }
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
