package cn.anecansaitin.free_camera_api_tripod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestScreen extends Screen {
    protected TestScreen() {
        super(Component.empty());
        Minecraft mc = Minecraft.getInstance();
        init(mc, mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }
}
