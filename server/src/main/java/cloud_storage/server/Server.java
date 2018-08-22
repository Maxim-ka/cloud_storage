package cloud_storage.server;


import cloud_storage.common.FileSendingHandler;
import cloud_storage.common.Rule;
import cloud_storage.server.dataBase.AuthService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.sql.SQLException;
import java.util.Vector;

public class Server {

    private final AuthService authService = new AuthService();
    private final Vector<String> listOfOpenRootFolder;
    private final int port;

    Server(int port) {
        this.port = port;
        listOfOpenRootFolder = new Vector<>();
    }

    public void run() throws InterruptedException, SQLException, ClassNotFoundException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            authService.connect();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder(),
                                    new ObjectDecoder(Rule.MAX_SIZE_OBJECT,ClassResolvers.cacheDisabled(null)),
                                    new AuthorizationHandler(authService, listOfOpenRootFolder),
                                    new ClientHandler(listOfOpenRootFolder, new FileSendingHandler(ch)));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = serverBootstrap.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("закончили работу");
            authService.disconnect();
        }
    }
}
