import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;

public class VersionHandler {
    public static String getMappingVersion(String version) throws URISyntaxException, IOException {

        URL url = new URI("https://maven.parchmentmc.org/org/parchmentmc/data/parchment-"
                + version +"/maven-metadata.xml").toURL();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream())
        );

        String line;
        while ((line = in.readLine()) != null) {
            if (!line.contains("<release>")) continue;

            in.close();
            return line.substring(
                    line.indexOf(">")+1, line.indexOf("</"));
        }
        in.close();
        return null;
    }

    public static String getNeoVersion(String mcVersion) throws URISyntaxException, IOException {
        URL url = new URI("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml")
                .toURL();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream())
        );

        mcVersion = mcVersion.substring(2);
        String line;
        String newestVersion = null;
        while ((line = in.readLine()) != null) {
            if (!line.contains("<version>")) continue;

            String version = line.substring(line.indexOf(">")+1, line.indexOf("</"));
            if (version.contains(mcVersion)) newestVersion = version;
            else if (newestVersion != null) {
                in.close();
                return newestVersion;
            }
        }
        in.close();
        return null;
    }
}
