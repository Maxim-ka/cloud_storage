package cloud_storage.common;

import io.netty.channel.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileSendingHandler{

    private Path currentDirectory;
    private Channel channel;

    public FileSendingHandler(Channel channel) {
        this.channel = channel;
    }

    public RequestCatalog actionWithFile(String command, String currentFolder, String destinationFolder, File[] files){
        currentDirectory = Paths.get(currentFolder);
        for (File file : files) {
            if (file.isFile()) {
                try {
                    switch (command) {
                        case SCM.COPY:
                            readFile(destinationFolder, file);
                            break;
                        case SCM.DELETE:
                            Files.delete(file.toPath());
                            break;
                        case SCM.RELOCATE:
                            readFile(destinationFolder, file);
                            Files.delete(file.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (file.isDirectory()) {
                try {
                    Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (command.startsWith(SCM.COPY) || command.startsWith(SCM.RELOCATE)) {
                                Path relativeDir = currentDirectory.relativize(dir);
                                readFolder(destinationFolder, relativeDir.toFile());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            switch (command) {
                                case SCM.COPY:
                                    readFile(destinationFolder, file.toFile());
                                    break;
                                case SCM.DELETE:
                                    Files.delete(file);
                                    break;
                                case SCM.RELOCATE:
                                    readFile(destinationFolder, file.toFile());
                                    Files.delete(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            System.out.println(exc.getCause().toString()); // TODO: 24.07.2018 логирование и сообщения
                            System.out.println(exc.getMessage());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            if (command.startsWith(SCM.RELOCATE) || command.startsWith(SCM.DELETE)) {
                                Files.delete(dir);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new RequestCatalog(SCM.OK, currentFolder, new File(currentFolder).listFiles());
    }

    private File getRelativePathFile(File file){
        return currentDirectory.relativize(file.toPath()).toFile();
    }

    private void readFolder(String destinationFolder, File file){
        channel.writeAndFlush(new TransferFile(destinationFolder, file, 1, 1, null));
    }

    private void readFile(String destinationFolder, File file){
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            File relFile = getRelativePathFile(file);
            int quotient = (int) (randomAccessFile.length() /  Rule.MAX_NUMBER_TRANSFER_BYTES);
            int totalPart = (randomAccessFile.length() %  Rule.MAX_NUMBER_TRANSFER_BYTES == 0) ? quotient : quotient + 1;
            int size = (randomAccessFile.length() >= Rule.MAX_NUMBER_TRANSFER_BYTES) ? Rule.MAX_NUMBER_TRANSFER_BYTES : (int) randomAccessFile.length();
            int portion = 0;
            while (portion < totalPart){
                byte[] bytes = new byte[size];
                randomAccessFile.read(bytes);
                int residue = (int)(randomAccessFile.length() - randomAccessFile.getFilePointer());
                if (residue < Rule.MAX_NUMBER_TRANSFER_BYTES) size = residue;
                channel.writeAndFlush(new TransferFile(destinationFolder, relFile, ++portion, totalPart, bytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
