package xyz.iwolfking.ae2tabs.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TabbedViewCellsConfig {
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> disableTabs;

        public Client(ForgeConfigSpec.Builder builder)
        {
            this.disableTabs = builder.comment("Whether to disable the tabbed View Cells functionality (default: false)").define("disableTabs", false);
        }
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static
    {
        Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }
}
