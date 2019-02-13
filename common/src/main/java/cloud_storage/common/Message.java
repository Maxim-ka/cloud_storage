package cloud_storage.common;

import java.io.File;
import java.io.Serializable;

public class Message implements Serializable {

    private final String command;
    private final String nameCatalog;
    private final File[] catalogFiles;
    private final File file;
    private final int portion;
    private final int total;
    private final byte[] bytes;

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

    public static class Builder{

        private final String command;
        private String nameCatalog;
        private File[] catalogFile;
        private File file;
        private int portion;
        private int total;
        private byte[] bytes;

        public Builder(String command) {
            this.command = command;
        }

        public Builder addNameCatalog(String nameCatalog){
            this.nameCatalog = nameCatalog;
            return this;
        }

        public Builder addCatalogFile(File[] catalog){
            this.catalogFile = catalog;
            return this;
        }

        public Builder addFile(File file){
            this.file = file;
            return this;
        }

        public Builder addPortion(int portion){
            this.portion = portion;
            return this;
        }

        public Builder addTotal(int total){
            this.total = total;
            return this;
        }

        public Builder addArrayBytes(byte[] bytes){
            this.bytes = bytes;
            return this;
        }

        public Message build(){
            return new Message(this);
        }
    }

    private Message(Builder builder) {
        command = builder.command;
        nameCatalog = builder.nameCatalog;
        catalogFiles = builder.catalogFile;
        file = builder.file;
        portion = builder.portion;
        total = builder.total;
        bytes = builder.bytes;
    }
}
