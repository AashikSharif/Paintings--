package subaraki.paintings.utils;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static subaraki.paintings.Paintings.LOGGER;

public class PaintingPackReader {

    public static final ArrayList<PaintingEntry> addedPaintings = new ArrayList<>();

    /**
     * called once on mod class initialization. the loadFromJson called in here reads
     * json files directly out of a directory.
     */
    public void init() {
        LOGGER.info("loading json file and contents for paintings.");
        loadFromJson();
    }

    private void loadFromJson() {
        // duplicate the base painting's template to our custom folder
        try {
            LOGGER.info("Copying Over Base Template to /paintings");
            Path dir = Paths.get("./paintings");

            if (!Files.exists(dir)) {

                Files.createDirectory(dir);
                Files.copy(getClass().getResourceAsStream("/assets/paintings/paintings.json"), dir.resolve("paintings.json"));
                // copyJsonToFolder
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.warn("************************************");
            LOGGER.warn("!*!*!*!*!");
            LOGGER.error("Copying Base Template Failed");
            LOGGER.error(e.getMessage());
            LOGGER.warn("!*!*!*!*!");
            LOGGER.warn("************************************");
        }

        // read out all resourcepacks, exclusively in zips,
        // to look for any other pack
        // and copy their json file over
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("./resourcepacks"))) {
            LOGGER.info("Reading out ResourcePacks to find painting related json files");

            for (Path resourcePackPath : directoryStream) {
                LOGGER.info("Reading `{}`", resourcePackPath.getFileName().toString());
                try {
                    URI jarUri = new URI("jar:%s".formatted(resourcePackPath.toUri().getScheme()), resourcePackPath.toUri().getPath(), null);

                    try (FileSystem system = initFileSystem(jarUri)) {
                        Iterator<Path> resourcePacks = Files.walk(system.getPath("/")).iterator();
                        while (resourcePacks.hasNext()) {
                            boolean copyOver = false;

                            Path next = resourcePacks.next();
                            if (Files.isRegularFile(next) && next.toString().endsWith("json")) {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(next)))) {

                                    Gson gson = new GsonBuilder().create();
                                    JsonElement je = gson.fromJson(reader, JsonElement.class);
                                    JsonObject json = je.getAsJsonObject();

                                    if (json.has("paintings")) {
                                        copyOver = true;
                                        LOGGER.info("Validated: {}", next.getFileName().toString());
                                    }
                                } catch (Exception e) {
                                    LOGGER.warn("************************************");
                                    LOGGER.error("`{}` Errored. Skipping.", next.getFileName().toString());
                                    LOGGER.error(e.getMessage());
                                    LOGGER.warn("************************************");
                                }
                            }

                            if (copyOver) {
                                Path fileToCopy = Path.of("./paintings").resolve(next.getFileName().toString());
                                if (Files.notExists(fileToCopy))
                                    Files.copy(next, fileToCopy);
                            }

                        }
                    } catch (Exception e) {
                        LOGGER.warn("************************************");
                        LOGGER.error("Invalid ResourcePack  {}", resourcePackPath.getFileName().toString());
                        LOGGER.error(e.getMessage());
                        LOGGER.warn("************************************");

                    }
                } catch (URISyntaxException e) {
                    LOGGER.warn("************************************");
                    LOGGER.error("Error Detected in ResourcePack `{}` ", resourcePackPath.getFileName().toString());
                    LOGGER.warn(e);
                    LOGGER.warn("************************************");
                }
            }
        } catch (IOException e) {

            LOGGER.warn("************************************");
            LOGGER.warn("!*!*!*!*!");
            LOGGER.error("A fatal error occurred reading the resource pack directory");
            LOGGER.error("SKIPPING ENTIRE PROCESS");
            LOGGER.warn("!*!*!*!*!");
            LOGGER.warn("************************************");
            LOGGER.warn(e);
        }

        // read out all json files in the painting directory

        Path dir = Paths.get("./paintings");

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            LOGGER.info("Started Reading all json files in /painting directory");

            for (Path filesInDirPath : ds) {
                LOGGER.info(filesInDirPath);
                Iterator<Path> jsonFiles = Files.walk(filesInDirPath).iterator();

                while (jsonFiles.hasNext()) {
                    Path nextJson = jsonFiles.next();

                    if (Files.isRegularFile(nextJson) && nextJson.toString().endsWith(".json")) {
                        InputStream stream = Files.newInputStream(nextJson);
                        Gson gson = new GsonBuilder().create();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                        JsonElement je = gson.fromJson(reader, JsonElement.class);
                        JsonObject json = je.getAsJsonObject();

                        JsonArray array = json.getAsJsonArray("paintings");

                        for (int index = 0; index < array.size(); index++) {

                            JsonObject jsonObject = array.get(index).getAsJsonObject();

                            String textureName = jsonObject.get("name").getAsString();

                            int sizeX = 0;
                            int sizeY = 0;

                            if (jsonObject.has("x")) {
                                sizeX = jsonObject.get("x").getAsInt();
                            }

                            if (jsonObject.has("y")) {
                                sizeY = jsonObject.get("y").getAsInt();
                            }

                            if (jsonObject.has("square")) {
                                sizeX = sizeY = jsonObject.get("square").getAsInt();
                            }

                            if (sizeX == 0 || sizeY == 0) {
                                LOGGER.error("Tried loading a painting where one of the sides was 0 ! ");
                                LOGGER.error("Painting name is : " + textureName);
                                LOGGER.error("Skipping...");
                                continue;
                            } else if (sizeX % 16 != 0 || sizeY % 16 != 0) {
                                LOGGER.error("Tried loading a painting with a size that is not a multiple of 16 !! ");
                                LOGGER.error("Painting name is : " + textureName);
                                LOGGER.error("Skipping...");
                                continue;
                            }

                            PaintingEntry entry = new PaintingEntry(textureName, sizeX, sizeY);
                            LOGGER.info(String.format("Loaded json painting %s , %d x %d", entry.getRefName(), entry.getSizeX(), entry.getSizeY()));
                            addedPaintings.add(entry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("************************************");
            LOGGER.warn("!*!*!*!*!");
            LOGGER.warn("No Painting Packs Detected. You will not be able to use ");
            LOGGER.warn("the Paintings ++ Mod correctly.");
            LOGGER.warn("Make sure to select or set some in the resourcepack folder and/or ingame gui !");
            LOGGER.warn("!*!*!*!*!");
            LOGGER.warn("************************************");

            e.printStackTrace();
        }
    }

    private FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            return FileSystems.newFileSystem(uri, env);
        }
    }
}
