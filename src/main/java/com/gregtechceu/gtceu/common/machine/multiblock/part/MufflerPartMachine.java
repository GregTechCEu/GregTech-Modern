package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.stream.IntStream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MufflerPartMachine extends TieredPartMachine implements IMufflerMachine, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MufflerPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    @Getter
    private final int recoveryChance;
    @Getter
    @Persisted
    private final CustomItemStackHandler inventory;

    private TickableSubscription snowSubscription;

    public MufflerPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.recoveryChance = Math.max(1, tier * 10);
        this.inventory = new CustomItemStackHandler((int) Math.pow(tier + 1, 2));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******** Muffler *********//
    //////////////////////////////////////

    @Override
    public void recoverItemsTable(ItemStack... recoveryItems) {
        int numRolls = Math.min(recoveryItems.length, inventory.getSlots());
        IntStream.range(0, numRolls).forEach(slot -> {
            if (calculateChance()) {
                ItemHandlerHelper.insertItemStacked(inventory, recoveryItems[slot].copy(), false);
            }
        });
    }

    private boolean calculateChance() {
        return recoveryChance >= 100 || recoveryChance >= GTValues.RNG.nextInt(100);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeLogicMachine &&
                    recipeLogicMachine.getRecipeLogic().isWorking()) {
                emitPollutionParticles();
                break;
            }
        }
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (snowSubscription == null) {
            this.snowSubscription = subscribeServerTick(null, this::tryBreakSnow);
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controllers.isEmpty()) {
            unsubscribe(snowSubscription);
            snowSubscription = null;
        }
    }

    private void tryBreakSnow() {
        if (getOffsetTimer() % 10 == 0) {
            for (IMultiController controller : getControllers()) {
                if (controller instanceof IRecipeLogicMachine recipeLogicMachine &&
                        recipeLogicMachine.getRecipeLogic().isWorking()) {
                    BlockPos mufflerPos = getPos().relative(getFrontFacing());
                    GTUtil.tryBreakSnow(getLevel(), mufflerPos, getLevel().getBlockState(mufflerPos), true);
                }
            }
        }
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventory.getSlots());
        int xOffset = rowSize == 10 ? 9 : 0;
        var modular = new ModularUI(176 + xOffset * 2,
                18 + 18 * rowSize + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset,
                        18 + 18 * rowSize + 12, true));

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
