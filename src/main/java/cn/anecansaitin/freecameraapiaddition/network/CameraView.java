package cn.anecansaitin.freecameraapiaddition.network;

import cn.anecansaitin.freecameraapiaddition.CameraAdditionConfig;
import cn.anecansaitin.freecameraapiaddition.core.chunk_loader.CameraTicketController;
import cn.anecansaitin.freecameraapiaddition.FreeCameraApiAddition;
import cn.anecansaitin.freecameraapiaddition.attachment.CameraData;
import cn.anecansaitin.freecameraapiaddition.attachment.ModAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraView(int x, int z) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CameraView> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FreeCameraApiAddition.MODID, "camera_radius"));
    public static final StreamCodec<ByteBuf, CameraView> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, pack -> pack.x,
            ByteBufCodecs.VAR_INT, pack -> pack.z,
            CameraView::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CameraView pack, IPayloadContext context) {
        Player player = context.player();
        CameraData data = player.getData(ModAttachment.CAMERA_DATA);
        int radius = CameraAdditionConfig.cameraChunkLoadRadius((ServerPlayer) player);
        boolean updateView = data.updateView(pack.x, pack.z, radius);

        if (updateView) {
            int currentX = data.view.x();
            int currentZ = data.view.z();

            int minX = currentX - radius;
            int maxX = currentX + radius;
            int minZ = currentZ - radius;
            int maxZ = currentZ + radius;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    CameraTicketController.addChunk((ServerLevel) player.level(), player, x, z);
                }
            }
        }
    }
}
