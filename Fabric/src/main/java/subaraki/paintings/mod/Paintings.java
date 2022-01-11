package subaraki.paintings.mod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Motive;
import subaraki.paintings.events.Events;
import subaraki.paintings.network.ServerNetwork;
import subaraki.paintings.util.ModConfig;
import subaraki.paintings.utils.PaintingEntry;
import subaraki.paintings.utils.PaintingPackReader;

import static subaraki.paintings.Paintings.LOGGER;
import static subaraki.paintings.Paintings.MODID;

public class Paintings implements ModInitializer {

    public static ModConfig config;

    /* call init here, to read json files before any event is launched. */
    static {
        new PaintingPackReader().init();
    }

    @Override
    public void onInitialize() {
        try {
            for (PaintingEntry entry : PaintingPackReader.addedPaintings) {
                ResourceLocation name = new ResourceLocation(MODID, entry.getRefName());
                Registry.register(Registry.MOTIVE, name, new Motive(entry.getSizeX(), entry.getSizeY()));
                LOGGER.info("Registered painting " + name);
            }
        } catch (ResourceLocationException e) {
            LOGGER.error("Skipping. Found Error: {}", e.getMessage());
        }
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ServerNetwork.registerServerPackets();
        Events.events();
    }
}
