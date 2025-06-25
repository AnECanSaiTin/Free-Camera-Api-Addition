package cn.anecansaitin.free_camera_api_tripod.attachment;

import cn.anecansaitin.free_camera_api_tripod.FreeCameraApiAddition;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FreeCameraApiAddition.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CameraData>> CAMERA_DATA = ATTACHMENT_TYPES.register("camera_data", () -> AttachmentType.builder(CameraData::new).build());
}
