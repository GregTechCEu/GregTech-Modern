package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageCover extends CoverBehavior implements IUICover {

    @SaveField
    @SyncToClient
    public final CustomItemStackHandler inventory;
    private final int SIZE = 18;

    public StorageCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
        inventory = new CustomItemStackHandler(SIZE) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        inventory.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("inventory"));
    }

    @Override
    @NotNull
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (int slot = 0; slot < SIZE; slot++) {
            list.add(inventory.getStackInSlot(slot));
        }
        return list;
    }

    @Override
    public boolean canAttach() {
        if (!(coverHolder instanceof MachineCoverContainer)) return false;
        for (var dir : Direction.values()) {
            if (coverHolder.hasCover(dir) && coverHolder.getCoverAtSide(dir) instanceof StorageCover)
                return false;
        }
        return super.canAttach();
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 126, 87);

        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle())));

        for (int slot = 0; slot < SIZE; slot++) {
            group.addWidget(new SlotWidget(inventory, slot, 7 + (slot % 6) * 18, 21 + (slot / 6) * 18));
        }

        return group;
    }

    private String getUITitle() {
        return "cover.storage.title";
    }

    @Override
    public @Nullable IFancyConfigurator getConfigurator() {
        return new StorageCoverConfigurator();
    }

    private class StorageCoverConfigurator implements IFancyConfigurator {

        @Override
        public Component getTitle() {
            return Component.translatable("cover.storage.title");
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.STORAGE_ICON;
        }

        @Override
        public Widget createConfigurator() {
            final var group = new WidgetGroup(0, 0, 126, 87);

            for (int slot = 0; slot < SIZE; slot++) {
                group.addWidget(new SlotWidget(inventory, slot, 7 + (slot % 6) * 18, 21 + (slot / 6) * 18));
            }

            return group;
        }
    }
}
