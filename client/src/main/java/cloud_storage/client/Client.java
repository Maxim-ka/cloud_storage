package cloud_storage.client;

import cloud_storage.client.GUI.Controller;
import cloud_storage.common.FileSendingHandler;
import cloud_storage.common.Message;
import cloud_storage.common.Rule;
import cloud_storage.common.SCM;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;

import java.io.File;

public class Client {

    private static volatile Client instance;

    private final String host;
    private final int port;
    private Controller controller;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private ChannelFuture f;
    private FileSendingHandler fileSendingHandler;
    private boolean authorized;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(workerGroup);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    public static synchronized Client getInstance(){
        if (instance == null) {
            synchronized (Client.class){
                if (instance == null){
                    instance = new Client(Rule.IP_ADDRESS, Rule.PORT);
                }
            }
        }
        return instance;
    }

    public void sendRequestGetFromServer(Object message){
        f.channel().writeAndFlush(message);
    }

    public Message sendRequestToServer(String command, String currentFolder, String destinationFolder, File[] fileList){
        return fileSendingHandler.actionWithFile(command, currentFolder, destinationFolder, fileList);
    }

    public boolean connect(){
        try {
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ObjectEncoder(),
                        new ObjectDecoder(Rule.MAX_SIZE_OBJECT, ClassResolvers.cacheDisabled(null)),
                        new ResponseHandler(controller));
                }
            });
            f = bootstrap.connect(host, port).sync();
            if (f.isSuccess()){
                fileSendingHandler = new FileSendingHandler(f.channel());
                return true;
            }
        } catch (Exception e) {
            // TODO: 12.07.2018 сообщение о недоступности сервера
            System.out.println("Не удалось подключиться к серверу");
            return false;
        }
        return  false;
    }

    public void disconnect() {
        try {
            sendRequestGetFromServer(new Message.Builder(SCM.DISCONNECT).build());
            f.channel().disconnect().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            f.addListener(ChannelFutureListener.CLOSE);
            authorized = false;
        }
    }

    public void closeClient(){
        try {
            if (authorized) disconnect();
        }finally {
            Future future = workerGroup.shutdownGracefully();
            try {
                future.await().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
