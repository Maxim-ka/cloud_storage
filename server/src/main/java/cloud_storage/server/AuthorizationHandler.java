package cloud_storage.server;

import cloud_storage.common.Message;
import cloud_storage.common.SCM;
import cloud_storage.server.dataBase.AuthService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.File;
import java.util.Vector;

class AuthorizationHandler extends ChannelInboundHandlerAdapter{

    private final AuthService authService;
    private final Vector<String> listOfOpenChannels;

    AuthorizationHandler(AuthService authService, Vector<String> listOfOpenChannels) {
        this.authService = authService;
        this.listOfOpenChannels = listOfOpenChannels;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        if (msg instanceof  Message){
            Message message = (Message) msg;
            String string = message.getCommand();
            String[] strings = string.split("\\s+");
            if (string.startsWith(SCM.AUTH) && strings.length == 3){
                String catalogUser = authService.getCatalogUser(strings[1], strings[2]);
                if (catalogUser != null){
                    if (!isDuplicateCatalog(catalogUser)){
                        if (new File(catalogUser).exists()){
                            Message.Builder builder = new Message.Builder(SCM.AUTH_OK)
                                .addNameCatalog(catalogUser);
                            ctx.fireChannelRead(builder.build());
                            builder.addCatalogFile(new File(catalogUser).listFiles());
                            ctx.writeAndFlush(builder.build());
                            ctx.pipeline().remove(this);
                            return;
                        }
                    }// TODO: 17.07.2018 написать коды отказов
                }
            }
            ChannelFuture f = ctx.channel().writeAndFlush(new Message.Builder(SCM.AUTH_BAD).build());
            f.channel().disconnect();
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private boolean isDuplicateCatalog(String catalog){
        if (listOfOpenChannels.isEmpty()) return false;
        for (String listOfOpenChannel : listOfOpenChannels) {
            if (listOfOpenChannel.equals(catalog)) return true;
        }
        return false;
    }
}
