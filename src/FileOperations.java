import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileOperations {
    // Extracts a zip file to a specified directory
    public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = zipIn.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
        }
    }

    // Downloads a file from a URL to a local path
    public static void downloadFile(String fileUrl, String localFilePath) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Unzips a zip file to a specified directory
    public static void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileInputStream fis = new FileInputStream(zipFilePath);
             ZipInputStream zipIn = new ZipInputStream(fis)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    FileOperations.extractFile(zipIn, filePath);
                } else {
                    File subDir = new File(filePath);
                    subDir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    // Pack a directory into a zip fi;e
    public static void pack(File source, File destination) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destination);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    packFile(file, zipOut, source.getAbsolutePath().length() + 1);
                }
            }
        }
    }

    // Pack a file into a zip file
private static void packFile(File file, ZipOutputStream zipOut, int basePathLength) throws IOException {
        String filePath = file.getAbsolutePath().substring(basePathLength);
        if (file.isHidden()) {
            return;
        }
        if (file.isDirectory()) {
            if (file.listFiles().length == 0) {
                zipOut.putNextEntry(new ZipEntry(filePath + "/"));
                zipOut.closeEntry();
            } else {
                for (File subFile : file.listFiles()) {
                    packFile(subFile, zipOut, basePathLength);
                }
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(filePath);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

}
