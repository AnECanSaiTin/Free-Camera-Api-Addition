package cn.anecansaitin.freecameraapiaddition;

import cn.anecansaitin.freecameraapi.core.ModifierManager;
import cn.anecansaitin.freecameraapiaddition.network.CameraPos;
import cn.anecansaitin.freecameraapiaddition.network.CameraState;
import cn.anecansaitin.freecameraapiaddition.network.CameraView;
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

    private void updateChunkLoader() {
        ModifierManager manager = ModifierManager.INSTANCE;

        if (!manager.isStateEnabledAnd(CHUNK_LOADER | ENABLE)) {
            if (chunkLoaderPrepared) {
                chunkLoaderPrepared = false;
                PacketDistributor.sendToServer(new CameraState(false, true));
                cameraStorage.viewCenterX = Integer.MAX_VALUE;
                cameraStorage.viewCenterZ = Integer.MAX_VALUE;
            }

            return;
        }

        Vector3f pos = manager.pos();

        if (!chunkLoaderPrepared) {
            PacketDistributor.sendToServer(
                    new CameraState(true, true),
                    new CameraPos(pos.x, pos.y, pos.z),
                    new CameraView(SectionPos.blockToSectionCoord(pos.x), SectionPos.blockToSectionCoord(pos.z), radius)
            );
            chunkLoaderPrepared = true;
            return;
        }

        int vx = cameraStorage.viewCenterX;
        int vz = cameraStorage.viewCenterZ;
        int nvx = SectionPos.blockToSectionCoord(pos.x);
        int nvz = SectionPos.blockToSectionCoord(pos.z);

        if (vx == nvx && vz == nvz) {
            PacketDistributor.sendToServer(new CameraPos(pos.x, pos.y, pos.z));
            return;
        }

        cameraStorage.viewCenterX = nvx;
        cameraStorage.viewCenterZ = nvz;
        PacketDistributor.sendToServer(new CameraView(nvx, nvz, radius));
    }

    @SubscribeEvent
    public static void levelTick(ClientTickEvent.Post event) {
        CameraChunkLoader.INSTANCE.updateChunkLoader();
    }
}
