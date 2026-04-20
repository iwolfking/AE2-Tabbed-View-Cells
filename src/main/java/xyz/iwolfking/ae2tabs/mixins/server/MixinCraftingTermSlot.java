package xyz.iwolfking.ae2tabs.mixins.server;

import appeng.menu.slot.CraftingTermSlot;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(value = CraftingTermSlot.class, remap = false)
public class MixinCraftingTermSlot {
    @Redirect(method = "craftItem", at = @At(value = "INVOKE", target = "Lappeng/items/storage/ViewCellItem;createItemFilter(Ljava/util/Collection;)Lappeng/util/prioritylist/IPartitionList;"))
    private IPartitionList dontFilterCraftingByViewCells(Collection<ItemStack> list) {
        return null;
    }
}
