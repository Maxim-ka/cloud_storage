package cloud_storage.client;

import cloud_storage.client.GUI.Controller;
import cloud_storage.client.GUI.FilesFolders;
import cloud_storage.common.Message;
import cloud_storage.common.ReceiverCollectorFile;
import cloud_storage.common.SCM;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.io.File;

class ResponseHandler extends ChannelInboundHandlerAdapter{

    private final Controller controller;
    private final ReceiverCollectorFile writer;

    ResponseHandler(Controller controller) {
        this.controller = controller;
        writer = new ReceiverCollectorFile();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message){
            Message message = (Message) msg;
            switch (message.getCommand()){
                case SCM.AUTH_BAD:
                    Client.getInstance().setAuthorized(false);
                    // TODO: 20.07.2018 вывод сообщений о неудачной авторизации
                    return;
                case SCM.AUTH_OK:
                    Client.getInstance().setAuthorized(true);
                    controller.getLogin().clear();
                    controller.getPass().clear();
                    controller.authorization();
                case SCM.BAD:
                    // TODO: 27.07.2018 вывод сообщения
                case SCM.OK:
                    showResponse(controller.getDirectoryOnServer(), controller.getCatalogUser(), message.getNameCatalog(), message.getCatalogFiles());
                    return;
                case SCM.MOVING:
                    writer.writeFile(controller.getCurrentDirectory().getText(), message);
                    // TODO: 20.07.2018 обновление экрана пока последней части файла
                    if(message.getPortion() == message.getTotal()){
                        showResponse(controller.getCurrentDirectory(), controller.getData(), controller.getCurrentDirectory().getText(), new File(controller.getCurrentDirectory().getText()).listFiles());
                    }
            }
        }
    }

    private void showResponse(Label currentDirectory, ObservableList<FilesFolders> list, final String directory, final File[] files){
        Platform.runLater(() ->{
            controller.showChange(currentDirectory, list, directory, files);
        });
    }
}
