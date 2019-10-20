package subaraki.paintings.mod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigData {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static boolean use_vanilla_only = false;
    public static boolean use_selection_gui = true;
    public static boolean cycle_paintings = false;
    public static boolean show_painting_size = true;

    public static class ServerConfig {

        public final ForgeConfigSpec.BooleanValue use_vanilla_only;
        public final ForgeConfigSpec.BooleanValue use_selection_gui;
        public final ForgeConfigSpec.BooleanValue cycle_paintings;

        ServerConfig(ForgeConfigSpec.Builder builder) {

            builder.push("general");
            use_vanilla_only = builder.comment("Pick true to only use the vanilla paintings and skip loading any other paintings")
                    .translation("translate.pick.vanilla").define("use_vanilla_only", false);
            use_selection_gui = builder.comment("Pick true to enable the Painting Selection Gui tot pop up when placing a painting")
                    .translation("translate.pick.psg").define("use_selection_gui", true);
            cycle_paintings = builder.comment("Pick True to enable painting cycling on right click with a painting item")
                    .translation("translate.cycle.painting").define("cycle_paintings", false);
            builder.pop();
        }
    }

    public static class ClientConfig {

        public final ForgeConfigSpec.BooleanValue show_painting_size;

        ClientConfig(ForgeConfigSpec.Builder builder) {

            builder.push("general");
            show_painting_size = builder.comment("Pick True to show the size of paintings in the painting gui").translation("translate.show.size")
                    .define("show_painting_size", true);
            builder.pop();

        }
    }

    public static void refreshClient() {

        show_painting_size = CLIENT.show_painting_size.get();

    }

    public static void refreshServer() {

        use_vanilla_only = SERVER.use_vanilla_only.get();
        use_selection_gui = SERVER.use_selection_gui.get();
        cycle_paintings = SERVER.cycle_paintings.get();

    }
}
