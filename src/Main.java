import java.io.IOException;
import java.net.FileNameMap;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length == 0) end("Please specify: neoforge or fabric");

        Map<String, String> info = new HashMap<>();

        out("Mod name: ");
        info.put("mod_name", in.nextLine());

        out("Mod ID: ");
        info.put("mod_id", in.nextLine());
        if (info.get("mod_id").matches("/^[a-z][a-z0-9_]{1,63}$/")) end("Invalid modID");

        if (!args[0].equalsIgnoreCase("fabric")) {
            out("Minecraft version: ");
            String mcVersion =  in.nextLine();
            String versionReg = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$";
            if (!mcVersion.matches(versionReg)) end("Invalid minecraft version.");
            String neoVersion = VersionHandler.getNeoVersion(mcVersion);
            if (neoVersion == null) end("Could not find valid NeoForge version.");
            String parchmentVersion = VersionHandler.getMappingVersion(mcVersion);
            if (parchmentVersion == null) end("Could not find Parchment version");

            info.put("minecraft_version", mcVersion);
            info.put("neo_version", neoVersion);
            info.put("neogradle.subsystems.parchment.mappingsVersion", parchmentVersion);
            info.put("neogradle.subsystems.parchment.minecraftVersion", mcVersion);

            String minor = mcVersion.substring(2,4);
            minor = String.valueOf(Integer.parseInt(minor)+1);
            info.put("minecraft_version_range", "[" +mcVersion+",1."+minor + ")");
            info.put("neo_version_range", "[" + mcVersion.substring(2) + ",)");
        }

        out("Author names (Separate with a comma): ");
        info.put("mod_authors", in.nextLine());

        out("Description (Supports new line chars): ");
        info.put("mod_description", in.nextLine());

        out("Group id: ");
        info.put("mod_group_id", in.nextLine());
        int groupLength = info.get("mod_group_id").split("\\.").length;

        if (!args[0].equalsIgnoreCase("fabric") && groupLength != 3)
            end("Group id must be 3 parts");
        if (args[0].equalsIgnoreCase("fabric") && groupLength != 2)
            end("Group id must be 2 parts");

        out("Main class: ");
        info.put("class_name", in.nextLine());

        String url = "";
        switch (args[0].toLowerCase()) {
            case "neo", "neoforge" -> url = "https://github.com/neoforged/MDK/archive/refs/heads/main.zip";
            case "fabric" -> url = "https://github.com/FabricMC/fabric-example-mod/archive/refs/heads/1.20.zip";
            default -> end("Please specify neoforge or fabric.");
        }

        Path path = Path.of(System.getProperty("user.dir") + "/" + info.get("mod_name"));
        FileHandler.createFileFromMDK(url, Path.of(path + ".zip"));
        FileHandler.unzipFolder(path + ".zip",
                path.toString(), info);

        Path gradleProperties = Path.of(path + "/gradle.properties");
        if (args[0].equalsIgnoreCase("fabric")) {
            Path resources = Path.of(path + "/src/main/resources");
            FileHandler.updateFabricProperties(info, gradleProperties);
            FileHandler.updateFabricJson(info,
                    Path.of(resources+"/fabric.mod.json"),
                    Path.of(resources+"/"+info.get("mod_id")+".mixins.json"));
        }
        else FileHandler.updateNeoProperties(info, gradleProperties);


        String main = path + "/src/main/java/" + info.get("mod_group_id")
                .replace(".", "/") + "/" + info.get("class_name") + ".java";
        FileHandler.updateMain(info.get("mod_id"), Path.of(main));

        if (args.length > 1 && args[1].equals("open")) {
            Runtime.getRuntime().exec(new String[]{
                    "idea64.exe", path.toString()
            });
            out("Opening in Intellij...");
        }
    }

    private static void out(String s) {
        System.out.print(s);
    }

    private static void end(String s) {
        out(s);
        System.exit(1);
    }
}
