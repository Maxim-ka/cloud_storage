package cloud_storage.server;

import cloud_storage.common.RequestCatalog;
import cloud_storage.common.SCM;
import cloud_storage.server.dataBase.AuthService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.File;
import java.util.Vector;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter{

    private final AuthService authService;
    private final Vector<String> listOfOpenChannels;

    AuthorizationHandler(AuthService authService, Vector<String> listOfOpenChannels) {
        this.authService = authService;
        this.listOfOpenChannels = listOfOpenChannels;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String string = (String)msg;
        String[] strings = string.split("\\s+");
        if (string.startsWith(SCM.AUTH) && strings.length == 3){
            String catalogUser = authService.getCatalogUser(strings[1], strings[2]);
            if (catalogUser != null){
                if (!isDuplicateCatalog(catalogUser)){
                    if (new File(catalogUser).exists()){
                        String message = String.format("%s %s", strings[0], SCM.OK);
                        ctx.fireChannelRead(String.format("%s %s", strings[0], catalogUser));
                        File[] catalogFiles = new File(catalogUser).listFiles();
                        ctx.writeAndFlush(new RequestCatalog(message, catalogUser, catalogFiles));
                        ctx.pipeline().remove(this);
                        return;
                    }
                }// TODO: 17.07.2018 написать коды отказов
            }
        }
        String message = String.format("%s %s", strings[0], SCM.BAD);
        ChannelFuture f = ctx.channel().writeAndFlush(new RequestCatalog(message,null, null));
        f.channel().disconnect();
        f.addListener(ChannelFutureListener.CLOSE);
    }

    private boolean isDuplicateCatalog(String catalog){
        if (listOfOpenChannels.isEmpty()) return false;
        for (String listOfOpenChannel : listOfOpenChannels) {
            if (listOfOpenChannel.equals(catalog)) return true;
        }
        return false;
    }
}
