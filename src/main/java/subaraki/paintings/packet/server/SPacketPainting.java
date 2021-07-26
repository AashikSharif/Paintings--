package subaraki.paintings.packet.server;

import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import subaraki.paintings.mod.Paintings;
import subaraki.paintings.packet.IPacketBase;
import subaraki.paintings.packet.NetworkHandler;
import subaraki.paintings.packet.client.CPacketPainting;

public class SPacketPainting implements IPacketBase {

    public SPacketPainting() {

    }

    private Motive type;
    private int entityID;

    public SPacketPainting(Motive type, int entityID) {

        this.type = type;
        this.entityID = entityID;
    }

    public SPacketPainting(FriendlyByteBuf buff) {

        decode(buff);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

        buf.writeUtf(this.type.getRegistryName().toString());
        buf.writeInt(entityID);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {

        String name = buf.readUtf(256);
        this.type = ForgeRegistries.PAINTING_TYPES.getValue(new ResourceLocation(name));
        this.entityID = buf.readInt();
    }

    @Override
    public void handle(Supplier<Context> context) {

        context.get().enqueueWork(() -> {
            Level world = context.get().getSender().level;
            Entity entity = world.getEntity(this.entityID);

            if (entity instanceof Painting) {
                Painting painting = (Painting) entity;

                Paintings.utility.setArt(painting, type);
                Paintings.utility.updatePaintingBoundingBox(painting);

                ServerPlayer playerMP = context.get().getSender();
                NetworkHandler.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with((() -> playerMP)),
                        new CPacketPainting(painting, new ResourceLocation[] { type.getRegistryName() }));
            }
        });
        context.get().setPacketHandled(true);
    }

    @Override
    public void encrypt(int id) {

        NetworkHandler.NETWORK.registerMessage(id, SPacketPainting.class, SPacketPainting::encode, SPacketPainting::new, SPacketPainting::handle);
    }
}