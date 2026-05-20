package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MufflerPartMachine extends TieredPartMachine implements IUIMachine {

    @Getter
    private final int recoveryChance;
    @Getter
    @SaveField
    private final CustomItemStackHandler inventory;

    private TickableSubscription snowSubscription;
    @Getter
    private final EnvironmentalHazardEmitterTrait hazardEmitter;

    public MufflerPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.recoveryChance = Math.max(1, tier * 10);
        this.inventory = new CustomItemStackHandler((int) Math.pow(tier + 1, 2));
        this.hazardEmitter = attachTrait(
                new EnvironmentalHazardEmitterTrait(GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                        2.5f / Math.max(tier, 1)));
    }

    //////////////////////////////////////
    // ******** Muffler *********//
    //////////////////////////////////////

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
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeLogicMachine &&
                    recipeLogicMachine.getRecipeLogic().isWorking()) {
                emitPollutionParticles();
                break;
            }
        }
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        return isFrontFaceFree() ? recipe : super.modifyRecipe(recipe);
    }

    @Override
    public boolean afterWorking(IWorkableMultiController controller) {
        hazardEmitter.emitHazard();
        var supplier = controller.self().getDefinition().getRecoveryItems();
        if (supplier != null) {
            recoverItemsTable(supplier.get());
        }
        return super.afterWorking(controller);
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        super.addedToController(controller);
        if (snowSubscription == null) {
            this.snowSubscription = subscribeServerTick(null, this::tryBreakSnow);
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        if (controllers.isEmpty()) {
            unsubscribe(snowSubscription);
            snowSubscription = null;
        }
    }

    private void tryBreakSnow() {
        if (getOffsetTimer() % 10 == 0) {
            for (MultiblockControllerMachine controller : getControllers()) {
                if (controller instanceof IRecipeLogicMachine recipeLogicMachine &&
                        recipeLogicMachine.getRecipeLogic().isWorking()) {
                    BlockPos mufflerPos = getBlockPos().relative(getFrontFacing());
                    GTUtil.tryBreakSnow(getLevel(), mufflerPos, getLevel().getBlockState(mufflerPos), true);
                }
            }
        }
    }

    public boolean isFrontFaceFree() {
        var frontPos = self().getBlockPos().relative(self().getFrontFacing());
        return self().getLevel().getBlockState(frontPos).isAir() ||
                GTCapabilityHelper.getHazardContainer(self().getLevel(),
                        frontPos, self().getFrontFacing().getOpposite()) != null;
    }

    public void emitPollutionParticles() {
        var pos = self().getBlockPos();
        var facing = self().getFrontFacing();

        IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(self().getLevel(),
                pos.relative(facing), facing.getOpposite());
        if (container != null) {
            // do not emit particles if front face has a duct on it.
            return;
        }

        var center = pos.getCenter();
        var offset = .75f;
        var xPos = (float) (center.x + facing.getStepX() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var yPos = (float) (center.y + facing.getStepY() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var zPos = (float) (center.z + facing.getStepZ() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);

        var ySpd = facing.getStepY() + (GTValues.RNG.nextFloat() - .15f) * .5f;
        var xSpd = facing.getStepX() + (GTValues.RNG.nextFloat() - .5f) * .5f;
        var zSpd = facing.getStepZ() + (GTValues.RNG.nextFloat() - .5f) * .5f;

        self().getLevel().addParticle(GTParticleTypes.MUFFLER_PARTICLE.get(),
                xPos, yPos, zPos, xSpd, ySpd, zSpd);
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
