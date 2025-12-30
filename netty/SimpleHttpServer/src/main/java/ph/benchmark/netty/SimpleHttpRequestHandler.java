package ph.benchmark.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class SimpleHttpRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {
    // private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

    private HttpVersion http_version = HttpVersion.HTTP_1_1;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        http_version = req.protocolVersion();

        boolean keepAlive = HttpUtil.isKeepAlive(req);

        URI uri = new URI(req.uri());

        String path = uri.getPath();

        // parse query
        String query = uri.getRawQuery();
        Map<String, String> params = new HashMap<>();
        for (String p : query.split("&")) {
            String[] pair = p.split("=");

            params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }

        // calc float
        double result = Double.parseDouble(params.get("init"));
        int loop = Integer.parseInt(params.get("loop_count"));
        double add = Double.parseDouble(params.get("add"));
        double mul = Double.parseDouble(params.get("mul"));
        double sub = Double.parseDouble(params.get("sub"));
        double div = Double.parseDouble(params.get("div"));

        int line = Integer.parseInt(params.get("line"));

        for (int i = 0; i < loop; i++) {
            result += add;
            result *= mul;
            result -= sub;
            result /= div;
        }

        String result_str = Double.toString(result).substring(0, 10);

        // build resp content
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line; i++) {
            sb.append("result=").append(result_str).append('\n');
        }

        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(http_version,
                OK,
                Unpooled.wrappedBuffer(content));

        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (keepAlive) {
            if (!req.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }

        ChannelFuture f = ctx.write(response);

        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // cause.printStackTrace();

		/*
		FullHttpResponse response = new DefaultFullHttpResponse(http_version,
				OK,
				Unpooled.EMPTY_BUFFER);

		response.headers()
				.set(CONTENT_TYPE, TEXT_PLAIN)
				.setInt(CONTENT_LENGTH, response.content().readableBytes());

		response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

		ChannelFuture f = ctx.write(response);

		f.addListener(ChannelFutureListener.CLOSE);
		*/
    }
}
