package com.hero.zcat;

import com.hero.servlet.HeroResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import java.nio.charset.StandardCharsets;

public class HeroResponseImpl implements HeroResponse {
    private final HttpRequest request;
    private final ChannelHandlerContext context;

    public HeroResponseImpl(HttpRequest request, ChannelHandlerContext context) {
        this.request = request;
        this.context = context;
    }

    @Override
    public void write(String content) throws Exception {
        if (StringUtil.isNullOrEmpty(content)) {
            return; // Fast-return if content is empty
        }
        // Simply create response, HTTP/1.1 200 OK
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content.getBytes(StandardCharsets.UTF_8)));
        HttpHeaders headers = response.headers();
        // We don't know what mime type it is, so just plain
        headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.set(HttpHeaderNames.EXPIRES, 0); // Don't cache
        // HTTP/1.1 keepalive
        if (HttpUtil.isKeepAlive(request)) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        context.writeAndFlush(response); // write the response
    }
}
