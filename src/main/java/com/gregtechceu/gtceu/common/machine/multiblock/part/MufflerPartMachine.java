package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.mui.GTMuiMachineUtil.createSquareSlotGroupFromInventory;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MufflerPartMachine extends TieredPartMachine implements IMufflerMachine, IMuiMachine {

    @Getter
    private final int recoveryChance;
    @Getter
    @SaveField
    private final CustomItemStackHandler inventory;

    private TickableSubscription snowSubscription;

    public MufflerPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.recoveryChance = Math.max(1, tier * 10);
        this.inventory = new CustomItemStackHandler((int) Math.pow(tier + 1, 2));
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
                    BlockPos mufflerPos = getBlockPos().relative(getFrontFacing());
                    GTUtil.tryBreakSnow(getLevel(), mufflerPos, getLevel().getBlockState(mufflerPos), true);
                }
            }
        }
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public @NotNull ModularPanel buildUI(@NotNull PosGuiData data, @NotNull PanelSyncManager syncManager,
                                         @NotNull UISettings settings) {
        int size = (int) Math.sqrt(inventory.getSlots());

        return new ModularPanel(this.getDefinition().getName())
                .size(Math.max(176, 20 + (18 * size)), 100 + (18 * size))
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(1)
                        .height(20 + 18 * size)
                        .child(Flow.row()
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .align(Alignment.CENTER)
                                .coverChildren()
                                .child(createSquareSlotGroupFromInventory(inventory, "muffler_inventory", syncManager)
                                        .marginLeft(30)
                                        .marginRight(30)
                                        .verticalCenter())))
                .child(new ParentWidget<>()
                        .bottom(7)
                        .widthRel(1)
                        .height(76)
                        .child(Flow.row()
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .align(Alignment.CENTER).coverChildren().child(
                                        SlotGroupWidget.playerInventory(false))));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * int rowSize = (int) Math.sqrt(inventory.getSlots());
     * int xOffset = rowSize == 10 ? 9 : 0;
     * var modular = new ModularUI(176 + xOffset * 2,
     * 18 + 18 * rowSize + 94, this, entityPlayer)
     * .background(GuiTextures.BACKGROUND)
     * .widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId()))
     * .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset,
     * 18 + 18 * rowSize + 12, true));
     *
     * for (int y = 0; y < rowSize; y++) {
     * for (int x = 0; x < rowSize; x++) {
     * int index = y * rowSize + x;
     * modular.widget(new SlotWidget(inventory, index,
     * (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, true, false)
     * .setBackgroundTexture(GuiTextures.SLOT));
     * }
     * }
     * return modular;
     * }
     */
}
