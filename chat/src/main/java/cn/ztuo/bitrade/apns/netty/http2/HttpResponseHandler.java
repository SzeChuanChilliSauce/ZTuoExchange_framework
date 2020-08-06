package cn.ztuo.bitrade.apns.netty.http2;

import cn.ztuo.bitrade.apns.error.ErrorDispatcher;
import cn.ztuo.bitrade.apns.error.ErrorModel;
import cn.ztuo.bitrade.apns.model.PushNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description: HttpResponseHandler
 * @author: MrGao
 * @create: 2019/09/04 11:34
 */
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_WRITE_TIMEOUT = 1;
    public static final int CODE_WRITE_FAILED = 2;
    public static final int CODE_READ_TIMEOUT = 3;
    public static final int CODE_READ_FAILED = 4;
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);
    private String name;
    private ConcurrentHashMap<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamIdPromiseMap;
    public ConcurrentHashMap<Integer, PushNotification> notificationMap;

    HttpResponseHandler(String name) {
        this.name = name;
        this.streamIdPromiseMap = new ConcurrentHashMap();
        this.notificationMap = new ConcurrentHashMap();
    }

    public PushNotification removeNotification(int streamId) {
        return (PushNotification)this.notificationMap.remove(streamId);
    }

    public Map.Entry<ChannelFuture, ChannelPromise> put(int streamId, PushNotification notification, ChannelFuture writeFuture, ChannelPromise promise) {
        this.notificationMap.put(streamId, notification);
        Map.Entry<ChannelFuture, ChannelPromise> mapFuture = (Map.Entry)this.streamIdPromiseMap.put(streamId, new SimpleEntry(writeFuture, promise));
        this.dumpStreamIdPromiseMap("put");
        return mapFuture;
    }

    private void dumpStreamIdPromiseMap(String tag) {
    }

    public Map<Integer, Integer> awaitResponses(long timeout, TimeUnit unit) {
        this.dumpStreamIdPromiseMap("awaitResponses");
        HashMap<Integer, Integer> responses = new HashMap();
        Iterator itr = this.streamIdPromiseMap.entrySet().iterator();

        while(itr.hasNext()) {
            Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>> entry = (Map.Entry)itr.next();
            ChannelFuture writeFuture = (ChannelFuture)((Map.Entry)entry.getValue()).getKey();
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
                responses.put(entry.getKey(), 1);
                log.info("write id " + entry.getKey() + " timeout");
            } else if (!writeFuture.isSuccess()) {
                responses.put(entry.getKey(), 2);
                itr.remove();
                log.info("write id " + entry.getKey() + " failed");
            } else {
                ChannelPromise promise = (ChannelPromise)((Map.Entry)entry.getValue()).getValue();
                if (!promise.awaitUninterruptibly(timeout, unit)) {
                    log.info("read id " + entry.getKey() + " timeout");
                    responses.put(entry.getKey(), 3);
                } else if (!promise.isSuccess()) {
                    responses.put(entry.getKey(), 4);
                    itr.remove();
                    log.info("read id " + entry.getKey() + " failed");
                } else {
                    responses.put(entry.getKey(), 0);
                    itr.remove();
                }
            }
        }

        return responses;
    }

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        this.dumpStreamIdPromiseMap("channelRead0");
        Integer streamId = msg.headers().getInt(ExtensionHeaderNames.STREAM_ID.text());
        int code = msg.status().code();
        PushNotification notification = (PushNotification)this.notificationMap.remove(streamId);
        if (code != 200) {
            log.debug("response[" + code + "],[streamId:" + streamId + "][token:" + notification + "]");
            ErrorModel errorModel = new ErrorModel();
            errorModel.setAppName(this.name);
            errorModel.setCode(code);
            errorModel.setNotification(notification);
            ErrorDispatcher.getInstance().dispatch(errorModel);
        }

        if (streamId == null) {
            log.error("HttpResponseHandler unexpected message received: " + msg);
        } else {
            Map.Entry<ChannelFuture, ChannelPromise> entry = (Map.Entry)this.streamIdPromiseMap.get(streamId);
            if (entry == null) {
                log.error("Message received for unknown stream id " + streamId);
            } else {
                ByteBuf content = msg.content();
                if (content.isReadable()) {
                    int contentLength = content.readableBytes();
                    byte[] arr = new byte[contentLength];
                    content.readBytes(arr);
                    new String(arr, 0, contentLength, CharsetUtil.UTF_8);
                }

                ((ChannelPromise)entry.getValue()).setSuccess();
            }

        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("TCP连接断开" + ctx.channel());
        super.channelInactive(ctx);
    }
}
