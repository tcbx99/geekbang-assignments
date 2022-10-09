package com.hero.zcat.server;

import com.hero.servlet.HeroRequest;
import com.hero.servlet.HeroResponse;
import com.hero.servlet.HeroServlet;
import com.hero.zcat.DefaultServlet;
import com.hero.zcat.HeroRequestImpl;
import com.hero.zcat.HeroResponseImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.Map;

public class ZCatServer {
    private final Map<String, HeroServlet> servlets;

    private final HeroServlet defaultServlet = new DefaultServlet();

    ZCatServer(Map<String, HeroServlet> servlets) {
        this.servlets = servlets;
    }

    public void run() throws Exception {
        runServer();
    }

    private void runServer() throws Exception {

        EventLoopGroup parent = new NioEventLoopGroup();
        EventLoopGroup child = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parent, child)
                    .option(ChannelOption.SO_BACKLOG, 16384)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // We need Keepalive
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new Handler());
                        }
                    });
            int port = initPort();
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("HeroCat启动成功：监听端口号为:" + port);
            future.channel().closeFuture().sync();
        } finally {
            parent.shutdownGracefully();
            child.shutdownGracefully();
        }
    }

    //初始化端口
    private int initPort() throws DocumentException {
        //初始化端口
        //读取配置文件Server.xml中的端口号
        // FIXME: We use server.xml to find the port, but this file could be got by browsers.
        InputStream in = getClass().getClassLoader().getResourceAsStream("server.xml");
        //获取配置文件输入流
        SAXReader saxReader = new SAXReader(); // FIXME: lots of sonarlint warnings
        Document doc = saxReader.read(in);
        //使用SAXReader + XPath读取端口配置
        Element portEle = (Element) doc.selectSingleNode("//port");
        return Integer.valueOf(portEle.getText());
    }


    private class Handler extends ChannelInboundHandlerAdapter {
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                String uri = request.uri();
                String path = getPath(uri);
                // Find real servlet
                HeroServlet servlet = servlets.getOrDefault(path, defaultServlet);
                // Do the real job
                HeroRequest req = new HeroRequestImpl(request);
                HeroResponse res = new HeroResponseImpl(request, ctx);
                if (request.method().name().equalsIgnoreCase("GET")) {
                    servlet.doGet(req, res);
                } else if (request.method().name().equalsIgnoreCase("POST")) {
                    servlet.doPost(req, res);
                }
                ctx.close();
            }
        }

        private String getPath(String uri) {
            String path = uri;
            if (path.contains("?")) {
                path = uri.substring(0, uri.indexOf("?"));
            }
            while (path.endsWith("/")) {
                // remove trailing slash
                path = path.substring(0, path.length() - 1);
            }
            return path;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }
}
