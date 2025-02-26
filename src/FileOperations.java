import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileOperations {
    public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = zipIn.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
        }
    }

    public static void downloadFile(String fileUrl, String localFilePath) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

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
}
