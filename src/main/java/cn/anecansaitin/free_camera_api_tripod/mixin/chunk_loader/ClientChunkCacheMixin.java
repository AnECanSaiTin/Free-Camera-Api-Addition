package cn.anecansaitin.free_camera_api_tripod.mixin.chunk_loader;

import cn.anecansaitin.free_camera_api_tripod.CameraAdditionConfig;
import cn.anecansaitin.free_camera_api_tripod.core.chunk_loader.CameraChunkLoader;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
    @Unique
    private ClientChunkCache self() {
        return (ClientChunkCache) (Object) this;
    }

    @Final
    @Shadow
    ClientLevel level;

    @Shadow
    private static boolean isValidChunk(LevelChunk chunk, int x, int z) {
        return false;
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void freeCameraAPI$onInit(ClientLevel level, int viewDistance, CallbackInfo ci) {
        CameraChunkLoader.INSTANCE.cameraStorage(self().new Storage(Math.max(2, viewDistance) + 3));
        CameraChunkLoader.INSTANCE.cameraStorage().viewCenterX = Integer.MAX_VALUE;
        CameraChunkLoader.INSTANCE.cameraStorage().viewCenterZ = Integer.MAX_VALUE;
    }

    @Inject(method = "updateViewRadius", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;<init>(Lnet/minecraft/client/multiplayer/ClientChunkCache;I)V"))
    public void freeCameraAPI$updateViewRadius(int viewDistance, CallbackInfo ci) {
        // 可视范围更新时，同步更新相机的storage
        ClientChunkCache.Storage storage = self().new Storage(CameraAdditionConfig.cameraChunkLoadRadius(viewDistance) + 3);
        ClientChunkCache.Storage cameraStorage = CameraChunkLoader.INSTANCE.cameraStorage();
        storage.viewCenterX = cameraStorage.viewCenterX;
        storage.viewCenterZ = cameraStorage.viewCenterZ;

        for (int i = 0; i < cameraStorage.chunks.length(); ++i) {
            LevelChunk chunk = cameraStorage.chunks.get(i);

            if (chunk == null) {
                continue;
            }

            ChunkPos pos = chunk.getPos();

            if (!storage.inRange(pos.x, pos.z)) {
                continue;
            }

            storage.replace(storage.getIndex(pos.x, pos.z), chunk);
        }

        CameraChunkLoader.INSTANCE.cameraStorage(storage);
    }

    @Inject(method = "drop", at = @At(value = "HEAD"))
    public void freeCameraAPI$onDrop(ChunkPos pos, CallbackInfo ci) {
        // 丢弃相机范围内的区块
        ClientChunkCache.Storage cameraStorage = CameraChunkLoader.INSTANCE.cameraStorage();

        if (!cameraStorage.inRange(pos.x, pos.z)) {
            return;
        }

        int i = cameraStorage.getIndex(pos.x, pos.z);
        LevelChunk chunk = cameraStorage.getChunk(i);

        if (!isValidChunk( chunk, pos.x, pos.z)) {
            return;
        }

        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
        cameraStorage.replace(i, null);
    }

    @Inject(method = "replaceWithPacketData", at = @At(value = "HEAD"), cancellable = true)
    private void freeCameraAPI$onReplace(int x, int z, FriendlyByteBuf buffer, Map<Heightmap.Types, long[]> heightmaps, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        // 更新区块数据
        ClientChunkCache.Storage cameraStorage = CameraChunkLoader.INSTANCE.cameraStorage();

        if (!CameraChunkLoader.INSTANCE.loadingChunk() || !cameraStorage.inRange(x, z)) {
            return;
        }

        int index = cameraStorage.getIndex(x, z);
        LevelChunk chunk = cameraStorage.getChunk(index);
        ChunkPos chunkPos = new ChunkPos(x, z);

        if (!isValidChunk(chunk, x, z)) {
            chunk = new LevelChunk(level, chunkPos);
            chunk.replaceWithPacketData(buffer, heightmaps, consumer);
            cameraStorage.replace(index, chunk);
        } else {
            chunk.replaceWithPacketData(buffer, heightmaps, consumer);
            cameraStorage.refreshEmptySections(chunk);
        }

        level.onChunkLoaded(chunkPos);
        NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
        cir.setReturnValue(chunk);
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", at = @At("TAIL"), cancellable = true)
    private void freeCameraAPI$onGetChunk(int x, int z, ChunkStatus requiredStatus, boolean load, CallbackInfoReturnable<LevelChunk> callback) {
        // 添加对相机缓存区块的获取
        ClientChunkCache.Storage cameraStorage = CameraChunkLoader.INSTANCE.cameraStorage();

        if (!CameraChunkLoader.INSTANCE.loadingChunk() || !cameraStorage.inRange(x, z)) {
            return;
        }

        LevelChunk chunk = cameraStorage.getChunk(cameraStorage.getIndex(x, z));

        if (!isValidChunk(chunk, x, z)) {
            return;
        }

        callback.setReturnValue(chunk);
    }
}