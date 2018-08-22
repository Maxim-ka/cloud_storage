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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture future = ctx.channel().closeFuture();
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof  String){
            String[] strings = ((String) msg).split("\\s+");
            if (strings[0].equals(SCM.AUTH)) {
                rootUserDirectory = strings[1];
                listOfOpenChannels.add(strings[1]);
                return;
            }
            if (strings[0].equals(SCM.DISCONNECT)){
                listOfOpenChannels.remove(rootUserDirectory);
                ChannelFuture future = ctx.channel().disconnect();
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }
        if (msg instanceof TransferFile){
            TransferFile file = (TransferFile) msg;
            ++countPart;
            if (file.getPortion() != countPart){
                ctx.writeAndFlush(new RequestCatalog(SCM.BAD, file.getDestinationFolder(), new File(file.getDestinationFolder()).listFiles()));
                // TODO: 22.08.2018 продумать в случае несовпадения частей файла
                return;
            }
            writer.writeFile(file.getDestinationFolder(), file);
            if(countPart == file.getTotal()){
                ctx.writeAndFlush(new RequestCatalog(SCM.OK, file.getDestinationFolder(), new File(file.getDestinationFolder()).listFiles()));
                countPart = 0;
            }
            return;
        }
        if (msg instanceof RequestCatalog){
            RequestCatalog request = (RequestCatalog)msg;
            String command = request.getCommand();
            if (command.equals(SCM.UPDATE) || command.equals(SCM.UP) || command.equals(SCM.DOWN))
                ctx.writeAndFlush(new RequestCatalog(SCM.OK, request.getCurrentCatalog(), new File(request.getCurrentCatalog()).listFiles()));
            else ctx.writeAndFlush(fileSendingHandler.actionWithFile(request.getCommand(), request.getCurrentCatalog(), null, request.getCatalog()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ChannelFuture future = ctx.disconnect();
        future.addListener(ChannelFutureListener.CLOSE);
        System.out.println(cause.toString());
    }
}
