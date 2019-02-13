package cloud_storage.server;

import cloud_storage.common.*;
import io.netty.channel.*;
import java.io.*;
import java.util.Vector;

class ClientHandler extends ChannelInboundHandlerAdapter{

    private final ReceiverCollectorFile writer = new ReceiverCollectorFile();
    private final FileSendingHandler fileSendingHandler;
    private final Vector<String> listOfOpenChannels;
    private String rootUserDirectory;
    private int countPart;

    ClientHandler(Vector<String> listOfOpenChannels, FileSendingHandler fileSendingHandler) {
        this.listOfOpenChannels = listOfOpenChannels;
        this.fileSendingHandler = fileSendingHandler;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        ChannelFuture future = ctx.channel().closeFuture();
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message){
            Message message = (Message) msg;
            switch (message.getCommand()){
                case SCM.AUTH_OK:
                    rootUserDirectory = message.getNameCatalog();
                    listOfOpenChannels.add(message.getNameCatalog());
                    return;
                case SCM.COPY:
                case SCM.RELOCATE:
                case SCM.DELETE:
                    ctx.writeAndFlush(fileSendingHandler.actionWithFile(message.getCommand(), message.getNameCatalog(), null, message.getCatalogFiles()));
                    return;
                case SCM.UPDATE:
                case SCM.UP:
                case SCM.DOWN:
                    ctx.writeAndFlush(createMessageToClient(SCM.OK, message));
                    return;
                case SCM.MOVING:
                    ++countPart;
                    if (message.getPortion() != countPart){
                        ctx.writeAndFlush(createMessageToClient(SCM.BAD, message));
                        // TODO: 22.08.2018 продумать в случае несовпадения частей файла
                        return;
                    }
                    writer.writeFile(message.getNameCatalog(), message);
                    if(countPart == message.getTotal()){
                        ctx.writeAndFlush(createMessageToClient(SCM.OK, message));
                        countPart = 0;
                    }
                    return;
                case SCM.DISCONNECT:
                    listOfOpenChannels.remove(rootUserDirectory);
                    ChannelFuture future = ctx.channel().disconnect();
                    future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private Message createMessageToClient(String command, Message request){
        Message.Builder builder = new Message.Builder(command)
            .addNameCatalog(request.getNameCatalog())
            .addCatalogFile(new File(request.getNameCatalog()).listFiles());
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        ChannelFuture future = ctx.disconnect();
        future.addListener(ChannelFutureListener.CLOSE);
        System.out.println(cause.toString());
    }
}
