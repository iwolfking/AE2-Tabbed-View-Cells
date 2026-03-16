package xyz.iwolfking.ae2tabs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("ae2tabs")
public class AE2Tabs {
    public AE2Tabs() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
