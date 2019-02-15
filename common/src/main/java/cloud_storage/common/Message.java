package cloud_storage.common;

import java.io.File;
import java.io.Serializable;

public class Message implements Serializable {

    private String command;
    private String nameCatalog;
    private File[] catalogFiles;
    private File file;
    private int portion;
    private int total;
    private byte[] bytes;

    public String getCommand() {
        return command;
    }

    public String getNameCatalog() {
        return nameCatalog;
    }

    public File[] getCatalogFiles() {
        return catalogFiles;
    }

    public File getFile() {
        return file;
    }

    public int getPortion() {
        return portion;
    }

    public int getTotal() {
        return total;
    }

    public byte[] getBytes() {
        return bytes;
    }

    private void setNameCatalog(String nameCatalog) {
        this.nameCatalog = nameCatalog;
    }

    private void setCatalogFiles(File[] catalogFiles) {
        this.catalogFiles = catalogFiles;
    }

    private void setFile(File file) {
        this.file = file;
    }

    private void setPortion(int portion) {
        this.portion = portion;
    }

    private void setTotal(int total) {
        this.total = total;
    }

    private void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    private Message(String command) {
        this.command = command;
    }

    public static class Builder{

        private Message message;

        public Builder(String command) {
            message = new Message(command);
        }

        public Builder addNameCatalog(String nameCatalog){
            message.setNameCatalog(nameCatalog);
            return this;
        }

        public Builder addCatalogFile(File[] catalog){
            message.setCatalogFiles(catalog);
            return this;
        }

        public Builder addFile(File file){
            message.setFile(file);
            return this;
        }

        public Builder addPortion(int portion){
            message.setPortion(portion);
            return this;
        }

        public Builder addTotal(int total){
            message.setTotal(total);
            return this;
        }

        public Builder addArrayBytes(byte[] bytes){
            message.setBytes(bytes);
            return this;
        }

        public Message build(){
            return message;
        }
    }
}
