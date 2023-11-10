package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.IntStream;

/**
 * @author KilaBash
 * @date 2023/3/8
 * @implNote MufflerPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MufflerPartMachine extends TieredPartMachine implements IMufflerMachine, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MufflerPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    @Getter
    private final int recoveryChance;
    @Getter @Persisted
    private final ItemStackTransfer inventory;

    public MufflerPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.recoveryChance = Math.max(1, tier * 10);
        this.inventory = new ItemStackTransfer((int) Math.pow(tier + 1, 2));
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    //********     Muffler     *********//
    //////////////////////////////////////

    @Override
    public void recoverItemsTable(ItemStack... recoveryItems) {
        int numRolls = Math.min(recoveryItems.length, inventory.getSlots());
        IntStream.range(0, numRolls).forEach(slot -> {
            if (calculateChance()) {
                ItemTransferHelper.insertItemStacked(inventory, recoveryItems[slot].copy(), false);
            }
        });
    }

    private boolean calculateChance() {
        return recoveryChance >= 100 || recoveryChance >= GTValues.RNG.nextInt(100);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientTick() {
        super.clientTick();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeLogicMachine && recipeLogicMachine.getRecipeLogic().isWorking()) {
                emitPollutionParticles();
                break;
            }
        }
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventory.getSlots());
        int xOffset = rowSize == 10 ? 9 : 0;
        var modular = new ModularUI(176 + xOffset * 2,
                18 + 18 * rowSize + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset, 18 + 18 * rowSize + 12, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                modular.widget(new SlotWidget(inventory, index,
                        (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return modular;
    }

}
