package xyz.iwolfking.ae2tabs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import xyz.iwolfking.ae2tabs.config.TabbedViewCellsConfig;

@Mod("ae2tabs")
public class AE2Tabs {

    public static final Logger LOGGER = LogUtils.getLogger();

    public AE2Tabs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TabbedViewCellsConfig.CLIENT_SPEC, "ae2tabs-client.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }
}
