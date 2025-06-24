package cn.anecansaitin.freecameraapiaddition.network;

import cn.anecansaitin.freecameraapiaddition.CameraTicketController;
import cn.anecansaitin.freecameraapiaddition.FreeCameraApiAddition;
import cn.anecansaitin.freecameraapiaddition.attachment.CameraData;
import cn.anecansaitin.freecameraapiaddition.attachment.ModAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraView(int x, int z, int radius) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CameraView> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FreeCameraApiAddition.MODID, "camera_radius"));
    public static final StreamCodec<ByteBuf, CameraView> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, pack -> pack.x,
            ByteBufCodecs.VAR_INT, pack -> pack.z,
            ByteBufCodecs.VAR_INT, pack -> pack.radius,
            CameraView::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CameraView pack, IPayloadContext context) {
        Player player = context.player();
        CameraData data = player.getData(ModAttachment.CAMERA_DATA);
        boolean updateView = data.updateView(pack.x, pack.z, pack.radius);

        if (updateView) {
            int currentX = data.currentView.x();
            int currentZ = data.currentView.z();
            int currentRadius = pack.radius;

            int currentMinX = currentX - currentRadius;
            int currentMaxX = currentX + currentRadius;
            int currentMinZ = currentZ - currentRadius;
            int currentMaxZ = currentZ + currentRadius;

            for (int x = currentMinX; x <= currentMaxX; x++) {
                for (int z = currentMinZ; z <= currentMaxZ; z++) {
                    CameraTicketController.addChunk((ServerLevel) player.level(), player, x, z);
                }
            }
        }
    }
}
