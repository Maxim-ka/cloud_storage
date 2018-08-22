package cloud_storage.client.GUI;

import cloud_storage.common.Rule;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FilesFolders{

    private String name;
    private String size;
    private String dateTime;
    private File file;

    File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getDateTime() {
        return dateTime;
    }

    FilesFolders(File file){
        this.file = file;
        name = this.file.getName();
        if (!name.equals(Rule.TO_UP_LEVEL)){
            if (this.file.isDirectory()) size = "Folder";
            else {
                size = getSizeFile();
                dateTime = getLastDateTimeModified();
            }
        }
    }

    private String getSizeFile(){
        double sizeFile = file.length();
        int counter = 0;
        while (sizeFile > 1024){
            sizeFile = sizeFile / 1024;
            counter++;
        }
        String dimension = (counter == 0) ? "байт" : (counter == 1) ? "Кб" : (counter == 2) ? "Мб" : "Гб";
        return String.format((counter == 0) ? "%.0f %s" : "%.3f %s", sizeFile, dimension);
    }

    private String getLastDateTimeModified(){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern (Rule.PATTERN_DATE_TIME));
    }
}
