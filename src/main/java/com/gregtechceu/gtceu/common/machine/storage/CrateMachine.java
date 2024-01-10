package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author h3tR
 * @date 2023/3/27
 * @implNote CrateMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CrateMachine extends MetaMachine implements IUIMachine, IMachineModifyDrops {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CrateMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private final Material material;
    @Getter
    private final int inventorySize;

    @Persisted
    public final NotifiableItemStackHandler inventory;

    public CrateMachine(IMachineBlockEntity holder, Material material, int inventorySize) {
        super(holder);
        this.material = material;
        this.inventorySize = inventorySize;
        this.inventory = new NotifiableItemStackHandler(this, inventorySize, IO.BOTH);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        int xOffset = inventorySize >= 90 ? 162 : 0;
        int yOverflow = xOffset > 0 ? 18 : 9;
        int yOffset = inventorySize > 3 * yOverflow ? (inventorySize - 3 * yOverflow - (inventorySize - 3 * yOverflow) % yOverflow) / yOverflow * 18 : 0;
        var modularUI = new ModularUI(176 + xOffset, 166 + yOffset, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset / 2, 82 + yOffset, true));
        int x = 0;
        int y = 0;
        for (int slot = 0; slot < inventorySize; slot++) {
            modularUI.widget(new SlotWidget(inventory, slot, x * 18 + 7, y * 18 + 17)
                    .setBackgroundTexture(GuiTextures.SLOT));
            x++;
            if (x == yOverflow) {
                x = 0;
                y++;
            }
        }
        return modularUI;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        MetaMachine.clearInventory(drops, inventory.storage);
    }
}
