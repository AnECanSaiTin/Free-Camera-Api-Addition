package cn.anecansaitin.free_camera_api_tripod;

import cn.anecansaitin.free_camera_api_tripod.attachment.ModAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(FreeCameraApiAddition.MODID)
public class FreeCameraApiAddition {
    public static final String MODID = "free_camera_api_tripod";

    public FreeCameraApiAddition(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachment.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, CameraAdditionConfig.SPEC);
    }
}
