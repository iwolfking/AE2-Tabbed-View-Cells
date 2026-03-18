package xyz.iwolfking.ae2tabs.mixins.client;

import appeng.api.config.IncludeExclude;
import appeng.api.config.TerminalStyle;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.AEKeyFilter;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.TabButton;
import appeng.items.storage.ViewCellItem;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.IConfigManagerListener;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MixinMEStorageScreen<C extends MEStorageMenu> extends AEBaseScreen<C> implements ISortSource, IConfigManagerListener {

    @Unique
    private int ae2tabs$selectedViewCell = -1;

    @Unique
    private final List<TabButton> ae2tabs$tabs = new ArrayList<>();

    @Unique
    private List<ItemStack> ae2tabs$lastViewCells = new ArrayList<>();

    @Shadow
    @Final
    protected Repo repo;

    @Shadow
    @Final
    private List<ItemStack> currentViewCells;

    @Shadow
    @Final
    private boolean supportsViewCells;

    public MixinMEStorageScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "createPartitionList", at = @At("HEAD"), cancellable = true)
    private void ae2tabs$overridePartition(List<ItemStack> viewCells,
                                           CallbackInfoReturnable<IPartitionList> cir) {
        if(!supportsViewCells) {
            return;
        }


        if (!ae2tabs$viewCellsEqual(viewCells, ae2tabs$lastViewCells)) {
            ae2tabs$lastViewCells = ae2tabs$copyStacks(viewCells);
            ae2tabs$rebuildTabs();
        }

        if (ae2tabs$selectedViewCell >= 0 && ae2tabs$selectedViewCell < viewCells.size()) {

            ItemStack stack = viewCells.get(ae2tabs$selectedViewCell);

            cir.setReturnValue(
                    ViewCellItem.createFilter(AEKeyFilter.none(), List.of(stack))
            );
        }
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void ae2tabs$init(CallbackInfo ci) {
        if(!supportsViewCells) {
            return;
        }

        ae2tabs$lastViewCells = ae2tabs$copyStacks(currentViewCells);
        ae2tabs$rebuildTabs();
    }

    @Unique
    private void ae2tabs$rebuildTabs() {
        if(!supportsViewCells) {
            return;
        }

        ae2tabs$tabs.forEach(this::removeWidget);
        ae2tabs$tabs.clear();

        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();

        // All items tab
        TabButton allItemsBtn = new TabButton(
                menu.getHost().getMainMenuIcon(),
                new TextComponent("All Items"),
                ir,
                btn -> ae2tabs$selectTab(-1)
        );

        ae2tabs$addTab(allItemsBtn, 0);

        if(menu.getClientRepo() == null) {
            return;
        }

        Map<AEItemKey, GridInventoryEntry> itemKeyMap = menu.getClientRepo().getAllEntries().stream()
                .filter(e -> e.getWhat() instanceof AEItemKey)
                .collect(Collectors.toMap(e -> (AEItemKey)e.getWhat(), e -> e, (a, b) -> a));


        for (int i = 0; i < currentViewCells.size(); i++) {
            ItemStack stack = currentViewCells.get(i);
            if (stack.isEmpty()) continue;

            IPartitionList filter = ViewCellItem.createFilter(AEKeyFilter.none(), List.of(stack));

            if(filter == null) {
                continue;
            }

            GridInventoryEntry entry = null;
            for (AEItemKey key : itemKeyMap.keySet()) {
                if (filter.matchesFilter(key, IncludeExclude.WHITELIST)) {
                    entry = itemKeyMap.get(key);
                    break;
                }
            }

            ItemStack icon = (entry != null) ? ((AEItemKey)entry.getWhat()).toStack() : stack;

            int index = i;
            TabButton tab = new TabButton(icon, stack.getHoverName(), ir, btn -> ae2tabs$selectTab(index));
            ae2tabs$addTab(tab, i + 1);
        }

    }

    @Redirect(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/me/common/Repo;setPartitionList(Lappeng/util/prioritylist/IPartitionList;)V"
            ),
            remap = true
    )
    private void ae2tabs$redirectPartition(Repo repo, IPartitionList original) {
        if(!supportsViewCells) {
            return;
        }

        if (ae2tabs$selectedViewCell >= 0 && ae2tabs$selectedViewCell < currentViewCells.size()) {

            ItemStack stack = currentViewCells.get(ae2tabs$selectedViewCell);

            IPartitionList filter =
                    ViewCellItem.createFilter(AEKeyFilter.none(), List.of(stack));

            repo.setPartitionList(filter);

        } else {
            ae2tabs$applyCurrentFilter();
        }
    }

    @Unique
    private void ae2tabs$addTab(TabButton tab, int index) {
        int x, y;
        int guiTop = getGuiTop();
        int guiBottom = guiTop + this.imageHeight;
        int screenBottom = this.height;
        boolean nearFullHeight = (guiBottom >= screenBottom - 10);

        if(config.getTerminalStyle().equals(TerminalStyle.TALL) || config.getTerminalStyle().equals(TerminalStyle.FULL) || nearFullHeight) {
            List<Slot> viewCellSlots = menu.getSlots(SlotSemantics.VIEW_CELL);
            Slot last = viewCellSlots.get(viewCellSlots.size() - 1);
            int bottomY = last.y + 32;
            x = getGuiLeft() + this.imageWidth + 8;
            y = bottomY + index * 22;
        } else {
            x = getGuiLeft() + 8 + index * 22;
            y = getGuiTop() - 22;
        }

        tab.x = x;
        tab.y = y;
        tab.setStyle(TabButton.Style.HORIZONTAL);
        tab.setSelected(index - 1 == ae2tabs$selectedViewCell);
        ae2tabs$tabs.add(tab);
        addRenderableWidget(tab);
    }

    @Unique
    private void ae2tabs$selectTab(int index) {
        ae2tabs$selectedViewCell = index;

        for(int i = 0; i < ae2tabs$tabs.size(); i++) {
            ae2tabs$tabs.get(i).setSelected(i - 1 == index);
        }

        ae2tabs$applyCurrentFilter();
    }

    @Unique
    private void ae2tabs$applyCurrentFilter() {

        IPartitionList filter = null;

        if(ae2tabs$selectedViewCell >= 0 && ae2tabs$selectedViewCell < currentViewCells.size()) {
            ItemStack stack = currentViewCells.get(ae2tabs$selectedViewCell);
            filter = ViewCellItem.createFilter(AEKeyFilter.none(), List.of(stack));
        }

        repo.setPartitionList(filter);
        repo.updateView();
    }

    @Unique
    private boolean ae2tabs$viewCellsEqual(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;

        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSameItemSameTags(a.get(i), b.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Unique
    private List<ItemStack> ae2tabs$copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copy = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copy.add(stack.copy());
        }
        return copy;
    }

    @Unique
    private void ae2tabs$refreshViewCellIcons() {
        for (int i = 0; i < currentViewCells.size(); i++) {
            ItemStack viewCell = currentViewCells.get(i);
            if (viewCell.isEmpty()) continue;

            int tabIndex = i + 1; // 0 = All Items tab
            if (tabIndex >= ae2tabs$tabs.size()) continue;

            TabButton tab = ae2tabs$tabs.get(tabIndex);
            ItemStack currentIcon = ((TabButtonAccessor)tab).getItem();

            // Only update if it’s still the raw view cell
            if (ItemStack.isSame(currentIcon, viewCell)) {
                ItemStack resolvedIcon = ae2tabs$getIconForViewCell(viewCell);
                ((TabButtonAccessor)tab).setItem(resolvedIcon);
            }
        }
    }

    @Unique
    private ItemStack ae2tabs$getIconForViewCell(ItemStack stack) {
        Map<AEItemKey, GridInventoryEntry> itemKeyMap = repo.getAllEntries().stream()
                .filter(e -> e.getWhat() instanceof AEItemKey)
                .collect(Collectors.toMap(e -> (AEItemKey)e.getWhat(), e -> e, (a, b) -> a));

        IPartitionList filter = ViewCellItem.createFilter(AEKeyFilter.none(), List.of(stack));
        if (filter == null) return stack;

        for (AEItemKey key : itemKeyMap.keySet()) {
            if (filter.matchesFilter(key, IncludeExclude.WHITELIST)) {
                return ((AEItemKey)itemKeyMap.get(key).getWhat()).toStack();
            }
        }

        return stack;
    }
}
