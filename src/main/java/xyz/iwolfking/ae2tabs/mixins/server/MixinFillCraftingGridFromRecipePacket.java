package xyz.iwolfking.ae2tabs.mixins.server;

import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import appeng.helpers.IMenuCraftingPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = FillCraftingGridFromRecipePacket.class, remap = false)
public class MixinFillCraftingGridFromRecipePacket {
    @Redirect(method = "serverPacketData", at = @At(value = "INVOKE", target = "Lappeng/helpers/IMenuCraftingPacket;getViewCells()Ljava/util/List;"))
    private List<ItemStack> dontFilterCraftingByViewCells(IMenuCraftingPacket instance) {
        return List.of();
    }
}
