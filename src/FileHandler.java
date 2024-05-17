import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    public static void createFileFromMDK(String url, Path path) {
        try (InputStream in = new URI(url).toURL().openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to copy mdk to file", exception);
        }
    }

    public static void updateNeoProperties(Map<String, String> info, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String line : Files.readAllLines(path)) {
            int index = line.indexOf("=");
            if (index < 0) {
                builder.append(line);
                continue;
            }

            String key = line.substring(0, index);
            if (info.get(key) != null) {
                line = key + "=" + info.get(key);
            }

            builder.append("\n").append(line).append("\n");
        }

        Files.writeString(path, builder.toString());
    }

    public static void updateFabricProperties(Map<String, String> info, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String line : Files.readAllLines(path)) {
            int index = line.indexOf("=");
            if (index < 0) {
                builder.append(line);
                continue;
            }

            String key = line.substring(0, index);
            if (key.equals("maven_group")) {
                line = key + "=" + info.get("mod_group_id");
            } else if (key.equals("archives_base_name")) {
                line = key + "=" + info.get("mod_id");
            }

            builder.append("\n").append(line).append("\n");
        }

        Files.writeString(path, builder.toString());
    }

    public static void updateFabricJson(Map<String, String> info, Path modPath, Path mixinPath) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String line : Files.readAllLines(modPath)) {
            if (line.contains("Example mod"))
                line = line.replace("Example mod", info.get("mod_name"));
            else if (line.contains("description"))
                line = line.substring(0,line.indexOf(":"))
                        + ": \"" + info.get("mod_description") + "\"";
            else if (line.contains("Me!")) {
                String indent = line.substring(0, line.indexOf("\""));
                line = indent + "\"" + info.get("mod_authors")
                        .replace(", ", "\"\n" + indent + "\"") + "\"";
            } else if (line.contains("modid"))
                line = line.replace("modid", info.get("mod_id"));
            else if (line.contains("com.example.ExampleMod"))
                line = line.replace("com.example.ExampleMod",
                        info.get("mod_group_id") + "." + info.get("class_name"));

            sb.append(line).append("\n");
        }

        Files.writeString(modPath, sb.toString());

        sb = new StringBuilder();
        for (String line : Files.readAllLines(mixinPath)) {
            if (line.contains("com.example.mixin"))
                line = line.replace("com.example", info.get("mod_group_id"));
            sb.append(line).append("\n");
        }

        Files.writeString(mixinPath, sb.toString());
    }

    public static void updateMain(Map<String, String> info, Path path) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String line : Files.readAllLines(path)) {
            if (line.contains("package "))
                line = "package " + info.get("mod_group_id") + ";";
            else if (line.contains("examplemod"))
                line = line.replace("examplemod", info.get("mod_id"));
            else if (line.contains("ExampleMod"))
                line = line.replace("ExampleMod", info.get("class_name"));
            else if (line.contains("\"modid\""))
                line = line.replace("modid", "mod_id");

            sb.append("\n").append(line);
        }

        Files.writeString(path, sb.toString());
    }

    public static void unzipFolder(String zip, String dest, Map<String, String> info) {
        System.out.println(dest);
        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry = zipInput.getNextEntry();
            byte[] buffer = new byte[1024];
            while (entry != null) {

                File newFile = new File(dest, changeEntry(entry, info));
                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new Exception("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipInput.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                entry = zipInput.getNextEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to unzip", e);
        }

        new File(zip).delete();
    }

    private static String changeEntry(ZipEntry entry, Map<String, String> info) {
        String[] groupId = info.get("mod_group_id").split("\\.");
        String fixedName = entry.toString().replace("MDK-main/", "");
        fixedName = fixedName.replace("fabric-example-mod-1.20/", "");

        fixedName = fixedName.replace("/com/",
                "/" + groupId[0] + "/");
        fixedName = fixedName.replace("/example/",
                "/" + groupId[1] + "/");

        if (groupId.length == 3)
            fixedName = fixedName.replace("/examplemod/",
                "/" + groupId[2] + "/");

        fixedName = fixedName.replace("ExampleMod.java",
                info.get("class_name") + ".java");
        fixedName = fixedName.replace("ExampleModClient.java",
                info.get("class_name") + "Client.java");

        fixedName = fixedName.replace("modid", info.get("mod_id"));

        return fixedName;
    }
}
