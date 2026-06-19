package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ItemVoidingCover extends ConveyorCover implements IUICover, IControllable {

    public ItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 0);
        setWorkingEnabled(false);
    }

    @Override
    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 5 != 0)
            return;

        doVoidItems();
        subscriptionHandler.updateSubscription();
    }

    protected void doVoidItems() {
        IItemHandler handler = getOwnItemHandler();
        if (handler == null) {
            return;
        }
        voidAny(handler);
    }

    void voidAny(IItemHandler handler) {
        ItemFilter filter = filterHandler.getFilter();

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack sourceStack = handler.extractItem(slot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !filter.test(sourceStack)) {
                continue;
            }
            handler.extractItem(slot, Integer.MAX_VALUE, false);
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 120);
        group.addWidget(new LabelWidget(10, 5, getUITitle()));

        group.addWidget(new ToggleButtonWidget(10, 20, 20, 20,
                GuiTextures.BUTTON_POWER, this::isWorkingEnabled, this::setWorkingEnabled));

        // group.addWidget(filterHandler.createFilterSlotUI(36, 21));
        group.addWidget(filterHandler.createFilterSlotUI(148, 91));
        group.addWidget(filterHandler.createFilterConfigUI(10, 50, 126, 60));

        buildAdditionalUI(group);

        return group;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.item.voiding.title";
    }

    @Override
    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return InteractionResult.PASS;
        }
        if (!isRemote()) {
            setWorkingEnabled(!isWorkingEnabled);
            context.getPlayer().sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.voiding.message.enabled" : "cover.voiding.message.disabled"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    // TODO: Decide grid behavior
    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return super.shouldRenderGrid(player, pos, state, held, toolTypes);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              ItemStack held, Direction side) {
        var superTips = super.sideTips(player, pos, state, toolTypes, held, side);
        if (superTips != null) return superTips;
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled() ? GuiTextures.TOOL_START : GuiTextures.TOOL_PAUSE;
        }
        return null;
    }
}
