package cloud_storage.client.GUI;

import cloud_storage.client.Client;
import cloud_storage.common.RequestCatalog;
import cloud_storage.common.Rule;
import cloud_storage.common.SCM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    private MenuBar menu;
    @FXML
    private Label currentDirectory;
    @FXML
    private TableView<FilesFolders> local;
    @FXML
    private TableColumn<FilesFolders, String> localName;
    @FXML
    private TableColumn<FilesFolders, String> localSize;
    @FXML
    private TableColumn<FilesFolders, String> localLastModified;
    @FXML
    private TextField login;
    @FXML
    private PasswordField pass;
    @FXML
    private BorderPane connected;
    @FXML
    private Pane offline;
    @FXML
    private Label directoryOnServer;
    @FXML
    private TableView<FilesFolders> server;
    @FXML
    private TableColumn<FilesFolders, String> serverName;
    @FXML
    private TableColumn<FilesFolders, String> serverSize;
    @FXML
    private TableColumn<FilesFolders, String> serverLastModified;
    @FXML
    private ProgressBar progressBar;

    private ObservableList<FilesFolders> data;
    private ObservableList<FilesFolders> catalogUser;
    private MultipleSelectionModel<FilesFolders> selectedLocal;
    private MultipleSelectionModel<FilesFolders> selectedServer;
    private boolean onServer;
    private String rootServerFolder;

    public ObservableList<FilesFolders> getData() {
        return data;
    }

    public Label getCurrentDirectory() {
        return currentDirectory;
    }

    public TextField getLogin() {
        return login;
    }

    public PasswordField getPass() {
        return pass;
    }

    public Label getDirectoryOnServer() {
        return directoryOnServer;
    }

    public ObservableList<FilesFolders> getCatalogUser() {
        return catalogUser;
    }

    public void showChange(Label currentDirectory, ObservableList<FilesFolders>  list,String currentFolder, File[] files){
        currentDirectory.setText(currentFolder);
        list.setAll(createListFilesFolders(files));
        if (Paths.get(currentDirectory.getText()).getParent() != null) createUpLevel(list);
    }

    public void rename(ActionEvent actionEvent) {

    }

    public void copy(ActionEvent actionEvent) {
        if (!onServer && currentDirectory.getText() == null) return;
        if (onServer && directoryOnServer.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (onServer){
            RequestCatalog requestCatalog = Client.getInstance().sendRequestToServer(SCM.COPY, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, requestCatalog.getCurrentCatalog(), requestCatalog.getCatalog());
        }else {
            Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.COPY, directoryOnServer.getText(), getSelected()));
        }
    }

    public void relocate(ActionEvent actionEvent) {
        if (!onServer && currentDirectory.getText() == null) return;
        if (onServer && directoryOnServer.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (onServer){
            RequestCatalog requestCatalog = Client.getInstance().sendRequestToServer(SCM.RELOCATE, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, requestCatalog.getCurrentCatalog(), requestCatalog.getCatalog());
        }else {
            Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.RELOCATE, directoryOnServer.getText(), getSelected()));
        }
    }

    public void delete(ActionEvent actionEvent) {
        if (!onServer && currentDirectory.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (onServer){
            RequestCatalog requestCatalog = Client.getInstance().sendRequestToServer(SCM.DELETE, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, requestCatalog.getCurrentCatalog(), requestCatalog.getCatalog());
        }else {
            Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.DELETE, directoryOnServer.getText(), getSelected()));
        }
    }

    public void createFolder(ActionEvent actionEvent) {
    }

    public void update(ActionEvent actionEvent) {
        if (Client.getInstance().isAuthorized())
            Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.UPDATE, directoryOnServer.getText(), null));
        if (!data.isEmpty()) showChange(currentDirectory, data, currentDirectory.getText(), new File(currentDirectory.getText()).listFiles());
    }

    private File[] getSelected(){
        if (!selectedLocal.isEmpty()){
            onServer = true;
            return translateListIntoArray(selectedLocal);
        }
        if (!selectedServer.isEmpty()){
            onServer = false;
            return translateListIntoArray(selectedServer);
        }
        // TODO: 16.07.2018 сообщение о отсутвии выбранных файлов
        System.out.println("Нет выбранных файлов");
        return null;
    }

    private File[] translateListIntoArray(MultipleSelectionModel<FilesFolders> selected){
        File[] files = new File[selected.getSelectedItems().size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = selected.getSelectedItems().get(i).getFile();
        }
        return files;
    }

    public void choiceOfSide(MouseEvent mouseEvent) {
        if (local.isFocused()) selectedServer.clearSelection();
        if (server.isFocused())selectedLocal.clearSelection();
    }

    public void openFolder(){
        Stage stage = (Stage) menu.getScene().getWindow();
        DirectoryChooser directory = new DirectoryChooser();
        directory.setInitialDirectory(new File("."));
        File catalog = directory.showDialog(stage);
        if (catalog.isDirectory()){
            showChange(currentDirectory, data, catalog.getAbsolutePath(),catalog.listFiles());
        }
    }

    private void createUpLevel(ObservableList<FilesFolders> list){
        File file = new File(Rule.TO_UP_LEVEL);
        file.deleteOnExit();
        list.add(0, new FilesFolders(file));
    }

    private FilesFolders[] createListFilesFolders(File[] files){
        FilesFolders[] filesFolders = new FilesFolders[files.length];
        for (int i = 0; i < files.length; i++) {
            filesFolders[i] = new FilesFolders(files[i]);
        }
        return filesFolders;
    }

    public void authorize(ActionEvent actionEvent) {
        if (login.getText().isEmpty() || pass.getText().isEmpty()) {
            // TODO: 12.07.2018 написать окно предупреждения
            return;
        }
        if (Client.getInstance().connect())
            Client.getInstance().sendRequestGetFromServer(String.format("%s %s %s", SCM.AUTH, login.getText(), pass.getText()));

    }

    public void authorization(){
        boolean auth = Client.getInstance().isAuthorized();
        offline.setVisible(!auth);
        offline.setManaged(!auth);
        connected.setVisible(auth);
        connected.setManaged(auth);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Client.getInstance().setController(this);
        data = FXCollections.observableArrayList();
        catalogUser = FXCollections.observableArrayList();

        serverName.setCellValueFactory (new PropertyValueFactory<FilesFolders, String>("name"));
        serverSize.setCellValueFactory (new PropertyValueFactory <FilesFolders, String> ("size"));
        serverLastModified.setCellValueFactory (new PropertyValueFactory <FilesFolders, String> ("dateTime"));
        server.setItems(catalogUser.sorted(new Ordering()));
        selectedServer = server.getSelectionModel();
        selectedServer.setSelectionMode(SelectionMode.MULTIPLE);
        server.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                if (isNotGoToLevel(catalogUser, selectedServer, event)) return;
                if (selectedServer.getSelectedItem().getName().equals(Rule.TO_UP_LEVEL)){
                    String folderUp = Paths.get(directoryOnServer.getText()).getParent().toString();
                    Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.UP, folderUp, null));
                    return;
                }
                File folderDown;
                if ((folderDown = selectedServer.getSelectedItem().getFile()).isDirectory()){
                    Client.getInstance().sendRequestGetFromServer(new RequestCatalog(SCM.DOWN, folderDown.getPath(), null));
                }
            }
        });

        localName.setCellValueFactory (new PropertyValueFactory<FilesFolders, String>("name"));
        localSize.setCellValueFactory (new PropertyValueFactory <FilesFolders, String> ("size"));
        localLastModified.setCellValueFactory (new PropertyValueFactory <FilesFolders, String> ("dateTime"));
        local.setItems(data.sorted(new Ordering()));
        selectedLocal = local.getSelectionModel();
        selectedLocal.setSelectionMode(SelectionMode.MULTIPLE);
        local.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isNotGoToLevel(data, selectedLocal, event)) return;
                if (selectedLocal.getSelectedItem().getName().equals(Rule.TO_UP_LEVEL)){
                    File folderUp = Paths.get(currentDirectory.getText()).getParent().toFile();
                    showChange(currentDirectory, data, folderUp.getAbsolutePath(),folderUp.listFiles());
                    return;
                }
                File file;
                if ((file = selectedLocal.getSelectedItem().getFile()).isDirectory()){
                    showChange(currentDirectory, data, file.getAbsolutePath(),file.listFiles());
                }
            }
        });
    }

    private boolean isNotGoToLevel(ObservableList<FilesFolders> list, MultipleSelectionModel<FilesFolders> selected, MouseEvent event){
        return list.isEmpty() || selected.getSelectedItems().size() != 1 || event.getClickCount() != 2;
    }

    public void disconnect(ActionEvent actionEvent){
        if (Client.getInstance().isAuthorized()) {
            Client.getInstance().disconnect();
            authorization();
        }
    }

    private class Ordering implements Comparator<FilesFolders>{

        @Override
        public int compare(FilesFolders o1, FilesFolders o2) {
            if (o1.getName().equals(Rule.TO_UP_LEVEL) || o2.getName().equals(Rule.TO_UP_LEVEL)) return 1;
            if (o1.getFile().isDirectory() && o2.getFile().isFile()) return -1;
            if (o1.getFile().isFile() && o2.getFile().isDirectory()) return 1;
            if (o1.getFile().isFile() && o2.getFile().isFile()){
                if (o1.getFile().lastModified() > o2.getFile().lastModified()) return 1;
                if (o1.getFile().lastModified() < o2.getFile().lastModified()) return -1;
            }
            return 0;
        }
    }
}
