package net.petrabarus.java.record_dir_and_upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

public class App {

    private static final int DELAY = 10_000;

    private final Path dirPath;

    private final Path outputPath;

    public App(String dirPath, String outputPath) {
        this.dirPath = Paths.get(dirPath);
        this.outputPath = Paths.get(outputPath);
    }

    public static void main(final String[] argv) throws InterruptedException, IOException {
        String dirPath = argv[0];
        String outputPath = argv[1];
        App app = new App(dirPath, outputPath);
        app.run();
    }

    public void run() throws InterruptedException, IOException {
        registerSigtermHandler();
        initOutputFile();
        while (true) {
            zipDirectory();
            Thread.sleep(DELAY);
        }
    }

    private void initOutputFile() throws IOException {
        FileUtils.writeStringToFile(outputPath.toFile(), "", StandardCharsets.US_ASCII);
    }

    public void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Finishing!");
                try {
                    compressDirectory(dirPath, outputPath);
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void zipDirectory() throws IOException {
        System.out.println("Zipping...");
        compressDirectory(dirPath, outputPath);
    }

    public static void compressDirectory(Path dirPath, Path outputPath) throws IOException, FileNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zipFile = new ZipOutputStream(out);
        compressDirectoryToZipFile(dirPath, dirPath, zipFile);
        IOUtils.closeQuietly(zipFile);

        long unixTime = System.currentTimeMillis() / 1000L;
        File outputFile = outputPath.toFile();
        FileUtils.writeStringToFile(outputFile, "\n>>>>>>>>>>> " + unixTime + "\n", StandardCharsets.US_ASCII, true);
        FileUtils.writeByteArrayToFile(outputFile, out.toByteArray(), true);
        //System.out.println(out.toString());
    }

    private static void compressDirectoryToZipFile(Path rootDir, Path sourceDir, ZipOutputStream out) throws IOException, FileNotFoundException {
        for (File file : rootDir.toFile().listFiles()) {
            if (file.isDirectory()) {
                compressDirectoryToZipFile(rootDir, sourceDir.resolve(file.getName()), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.toString().replace(rootDir.toString(), "") + file.getName());
                out.putNextEntry(entry);
                FileInputStream in = new FileInputStream(sourceDir.resolve(file.getName()).toString());
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
            }
        }
    }
}
