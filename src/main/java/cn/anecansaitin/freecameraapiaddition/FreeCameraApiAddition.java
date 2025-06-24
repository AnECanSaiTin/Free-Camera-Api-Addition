package cn.anecansaitin.freecameraapiaddition;

import cn.anecansaitin.freecameraapiaddition.attachment.ModAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(FreeCameraApiAddition.MODID)
public class FreeCameraApiAddition {
    public static final String MODID = "free_camera_api_addition";

    public FreeCameraApiAddition(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachment.ATTACHMENT_TYPES.register(modEventBus);
    }
}
