package cloud_storage.client;

import cloud_storage.client.GUI.Controller;
import cloud_storage.client.GUI.FilesFolders;
import cloud_storage.common.ReceiverCollectorFile;
import cloud_storage.common.RequestCatalog;
import cloud_storage.common.SCM;
import cloud_storage.common.TransferFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.io.File;

class ResponseHandler extends ChannelInboundHandlerAdapter{

    private Controller controller;
    private ReceiverCollectorFile writer;

    ResponseHandler(Controller controller) {
        this.controller = controller;
        writer = new ReceiverCollectorFile();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof  RequestCatalog){
            RequestCatalog request = (RequestCatalog) msg;
            String string = request.getCommand();
            String[] strings = string.split("\\s+");
            if (strings[0].equals(SCM.AUTH)) {
                if (strings[1].equals(SCM.OK)){
                    Client.getInstance().setAuthorized(true);
                    controller.getLogin().clear();
                    controller.getPass().clear();
                    controller.authorization();
                    showResponse(controller.getDirectoryOnServer(), controller.getCatalogUser(), request.getCurrentCatalog(), request.getCatalog());
                    return;
                }
                Client.getInstance().setAuthorized(false);
                // TODO: 20.07.2018 вывод сообщений о неудачной авторизации
                return;
            }
            if (strings[0].equals(SCM.OK)){
                showResponse(controller.getDirectoryOnServer(), controller.getCatalogUser(), request.getCurrentCatalog(), request.getCatalog());
                return;
            }
            if (strings[0].equals(SCM.BAD)){
                showResponse(controller.getDirectoryOnServer(), controller.getCatalogUser(), request.getCurrentCatalog(), request.getCatalog());
                System.out.println("проблема с получением файла");
                // TODO: 27.07.2018 вывод сообщения
            }
        }
        if (msg instanceof TransferFile){
            TransferFile file = (TransferFile) msg;
            writer.writeFile(controller.getCurrentDirectory().getText(), file);
            // TODO: 20.07.2018 обновление экрана пока последней части файла
            if(file.getPortion() == file.getTotal()){
                showResponse(controller.getCurrentDirectory(), controller.getData(), controller.getCurrentDirectory().getText(), new File(controller.getCurrentDirectory().getText()).listFiles());
            }
        }
    }

    private void showResponse(Label currentDirectory, ObservableList<FilesFolders> list, final String directory, final File[] files){
        Platform.runLater(() ->{
            controller.showChange(currentDirectory, list, directory, files);
        });
    }
}
