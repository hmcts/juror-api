package uk.gov.hmcts.juror.api.moj.utils;

import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;


public final class FileUtils {

    private FileUtils() {

    }

    public static boolean doesFileExist(File checksFile) {
        return checksFile != null && checksFile.exists();
    }

    public static File createFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (Exception exception) {
            throw new MojException.InternalServerError("Failed to create new file on path: " + path, exception);
        }
    }

    public static void writeToFile(File file, String data) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
            final byte[] contentInBytes = data.getBytes();
            outputStream.write(contentInBytes);
            outputStream.flush();
        }
    }


}
