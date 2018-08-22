package cloud_storage.common;

import java.io.File;
import java.io.Serializable;

public class RequestCatalog implements Serializable {

    private String command;
    private String currentCatalog;
    private File[] catalog;

    public String getCommand() {
        return command;
    }

    public String getCurrentCatalog() {
        return currentCatalog;
    }

    public File[] getCatalog() {
        return catalog;
    }

    public RequestCatalog(String command, String currentCatalog, File[] catalog) {
        this.command = command;
        this.currentCatalog = currentCatalog;
        this.catalog = catalog;
    }
}
