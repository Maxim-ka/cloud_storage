package cloud_storage.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ReceiverCollectorFile {

    private int countPart;

    public void writeFile(String destinationFolder, TransferFile transferFile){
        if (transferFile.getBytes() == null){
            new File(String.format("%s/%s", destinationFolder, transferFile.getFile())).mkdir();
            return;
        }
        String nameFile = String.format("%s/%s" ,destinationFolder, transferFile.getFile());
        File tempFile;
        try(FileOutputStream outputStream = new FileOutputStream(tempFile = new File(nameFile.concat(".temp")), true)) {
            countPart++;
            outputStream.write(transferFile.getBytes());
            outputStream.close();
            if (countPart == transferFile.getTotal()){
                checkNameFile(nameFile, tempFile);
                countPart = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkNameFile(String nameFile, File tempName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(nameFile);
        int insertIndex = stringBuilder.lastIndexOf(".");
        int numCopy = 0;
        while (new File(nameFile).exists()){
            if (insertIndex == stringBuilder.lastIndexOf(".")) stringBuilder.insert(insertIndex, String.format("(%d)", ++numCopy));
            else stringBuilder.replace(insertIndex, String.format("(%d)", numCopy).length(), String.format("(%d)", ++numCopy));
            nameFile = stringBuilder.toString();
        }
        Files.move(tempName.toPath(), new File(nameFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
