package cn.anecansaitin.freecameraapiaddition.network;

import cn.anecansaitin.freecameraapiaddition.FreeCameraApiAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraState(boolean enable, boolean update) implements CustomPacketPayload {
    public static final Type<CameraState> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FreeCameraApiAddition.MODID, "camera_state"));
    public static final StreamCodec<ByteBuf, CameraState> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (pack) -> pack.enable,
            ByteBufCodecs.BOOL, (pack) -> pack.update,
            CameraState::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CameraState pack, IPayloadContext context) {

    }
}
