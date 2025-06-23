package cn.anecansaitin.freecameraapiaddition;

import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapiaddition.network.CameraPoseUpdate;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import static cn.anecansaitin.freecameraapi.core.ModifierStates.*;

@EventBusSubscriber(modid = FreeCameraApiAddition.MODID)
public class CameraChunkLoader {
    public static final CameraChunkLoader INSTANCE = new CameraChunkLoader();
    private ClientChunkCache.Storage cameraStorage;
    // todo 临时半径
    private int radius = 2;
    private boolean chunkLoaderPrepared;

    public ClientChunkCache.Storage cameraStorage() {
        return cameraStorage;
    }

    public void cameraStorage(ClientChunkCache.Storage storage) {
        cameraStorage = storage;
    }

    public boolean loadingChunk() {
        return chunkLoaderPrepared;
    }

    public void updateChunkLoader() {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledAnd(CHUNK_LOADER | ENABLE)) {
            if (chunkLoaderPrepared) {
                chunkLoaderPrepared = false;
                PacketDistributor.sendToServer(new CameraPoseUpdate(false, true, 0, 0, 0, 0));
                cameraStorage.viewCenterX = Integer.MAX_VALUE;
                cameraStorage.viewCenterZ = Integer.MAX_VALUE;
            }

            return;
        }

        Vector3f pos = manager.pos();

        if (!chunkLoaderPrepared) {
            PacketDistributor.sendToServer(new CameraPoseUpdate(true, true, pos.x, pos.y, pos.z, radius));
            chunkLoaderPrepared = true;
            return;
        }

        int vx = cameraStorage.viewCenterX;
        int vz = cameraStorage.viewCenterZ;
        int nvx = SectionPos.blockToSectionCoord(pos.x);
        int nvz = SectionPos.blockToSectionCoord(pos.z);

        if (vx == nvx && vz == nvz) {
            PacketDistributor.sendToServer(new CameraPoseUpdate(true, false, pos.x, pos.y, pos.z, radius));
            return;
        }

        cameraStorage.viewCenterX = nvx;
        cameraStorage.viewCenterZ = nvz;
        PacketDistributor.sendToServer(new CameraPoseUpdate(true, true, pos.x, pos.y, pos.z, radius));
    }

    @SubscribeEvent
    public static void levelTick(ClientTickEvent.Post event) {
        CameraChunkLoader.INSTANCE.updateChunkLoader();
    }
}
