package xyz.iwolfking.ae2tabs.mixins;

import appeng.client.gui.widgets.TabButton;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TabButton.class, remap = false)
public interface TabButtonAccessor {
    @Accessor("item")
    ItemStack getItem();

    @Accessor("item")
    void setItem(ItemStack item);
}
