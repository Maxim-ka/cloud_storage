package cloud_storage.client.GUI;

import cloud_storage.client.Client;
import cloud_storage.common.Message;
import cloud_storage.common.Rule;
import cloud_storage.common.SCM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
    private boolean toServer;

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
        if (!toServer && currentDirectory.getText() == null) return;
        if (toServer && directoryOnServer.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (toServer){
            Message catalog = Client.getInstance().sendRequestToServer(SCM.COPY, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, catalog.getNameCatalog(), catalog.getCatalogFiles());
        }else {
            Client.getInstance().sendRequestGetFromServer(createMessageToServer(SCM.COPY, getSelected()));
        }
    }

    public void relocate(ActionEvent actionEvent) {
        if (!toServer && currentDirectory.getText() == null) return;
        if (toServer && directoryOnServer.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (toServer){
            Message catalog = Client.getInstance().sendRequestToServer(SCM.RELOCATE, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, catalog.getNameCatalog(), catalog.getCatalogFiles());
        }else {
            Client.getInstance().sendRequestGetFromServer(createMessageToServer(SCM.RELOCATE, getSelected()));
        }
    }

    public void delete(ActionEvent actionEvent) {
        if (!toServer && currentDirectory.getText() == null) return;
        if (getSelected() == null) return;   // TODO: 24.07.2018 предупреждение о неправильных действиях
        if (toServer){
            Message catalog = Client.getInstance().sendRequestToServer(SCM.DELETE, currentDirectory.getText(), directoryOnServer.getText(),getSelected());
            showChange(currentDirectory, data, catalog.getNameCatalog(), catalog.getCatalogFiles());
        }else {
            Client.getInstance().sendRequestGetFromServer(createMessageToServer(SCM.DELETE, getSelected()));
        }
    }

    public void createFolder(ActionEvent actionEvent) {
    }

    public void update(ActionEvent actionEvent) {
        if (Client.getInstance().isAuthorized())
            Client.getInstance().sendRequestGetFromServer(createMessageToServer(SCM.UPDATE, null));
        if (!data.isEmpty()) showChange(currentDirectory, data, currentDirectory.getText(), new File(currentDirectory.getText()).listFiles());
    }

    private Message createMessageToServer(String command, File[] catalog){
        Message.Builder builder = new Message.Builder(command)
            .addNameCatalog(directoryOnServer.getText())
            .addCatalogFile(catalog);
        return builder.build();
    }

    private File[] getSelected(){
        if (!selectedLocal.isEmpty()){
            toServer = true;
            return translateListIntoArray(selectedLocal);
        }
        if (!selectedServer.isEmpty()){
            toServer = false;
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
        if (Client.getInstance().connect()){
            Message.Builder builder = new Message.Builder(String.format("%s %s %s", SCM.AUTH, login.getText(), pass.getText()));
            Client.getInstance().sendRequestGetFromServer(builder.build());
        }
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

        serverName.setCellValueFactory (new PropertyValueFactory<>("name"));
        serverSize.setCellValueFactory (new PropertyValueFactory <> ("size"));
        serverLastModified.setCellValueFactory (new PropertyValueFactory <> ("dateTime"));
        server.setItems(catalogUser.sorted(new Ordering()));
        selectedServer = server.getSelectionModel();
        selectedServer.setSelectionMode(SelectionMode.MULTIPLE);
        server.setOnMouseClicked(event -> {
            if (isNotGoToLevel(catalogUser, selectedServer, event)) return;
            if (selectedServer.getSelectedItem().getName().equals(Rule.TO_UP_LEVEL)){
                Message.Builder builder = new Message.Builder(SCM.UP)
                    .addNameCatalog(Paths.get(directoryOnServer.getText()).getParent().toString());
                Client.getInstance().sendRequestGetFromServer(builder.build());
                return;
            }
            File folderDown;
            if ((folderDown = selectedServer.getSelectedItem().getFile()).isDirectory()){
                Message.Builder builder = new Message.Builder(SCM.DOWN)
                    .addNameCatalog(folderDown.getPath());
                Client.getInstance().sendRequestGetFromServer(builder.build());
            }
        });

        localName.setCellValueFactory (new PropertyValueFactory<>("name"));
        localSize.setCellValueFactory (new PropertyValueFactory <> ("size"));
        localLastModified.setCellValueFactory (new PropertyValueFactory <> ("dateTime"));
        local.setItems(data.sorted(new Ordering()));
        selectedLocal = local.getSelectionModel();
        selectedLocal.setSelectionMode(SelectionMode.MULTIPLE);
        local.setOnMouseClicked(event -> {
            if (isNotGoToLevel(data, selectedLocal, event)) return;
            if (selectedLocal.getSelectedItem().getName().equals(Rule.TO_UP_LEVEL)){
                File folderUp = Paths.get(currentDirectory.getText()).getParent().toFile();
                showChange(currentDirectory, data, folderUp.getAbsolutePath(),folderUp.listFiles());
                return;
            }
            File file;
            if ((file = selectedLocal.getSelectedItem().getFile()).isDirectory()){
                showChange(currentDirectory, data, file.getAbsolutePath(), file.listFiles());
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
