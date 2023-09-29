package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.biome.Biome.Precipitation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitivePumpMachine extends MultiblockControllerMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PrimitivePumpMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    private long biomeModifier = 0;
    private int hatchModifier = 0;
    private NotifiableFluidTank fluidTank;
    private TickableSubscription produceWaterSubscription;

    public PrimitivePumpMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        initializeTank();
        produceWaterSubscription = subscribeServerTick(this::produceWater);
        produceWater();
    }

    private void initializeTank() {
        for (IMultiPart part : getParts()) {
            for (var handler : part.getRecipeHandlers()) {
                if (handler.getHandlerIO() == IO.OUT && handler.getCapability() == FluidRecipeCapability.CAP) {
                    fluidTank = (NotifiableFluidTank) handler;
                    long tankCapacity = fluidTank.getTankCapacity(0);
                    if (tankCapacity == FluidHelper.getBucket()) {
                        hatchModifier = 1;
                    } else if (tankCapacity == FluidHelper.getBucket() * 8) {
                        hatchModifier = 2;
                    } else {
                        hatchModifier = 4;
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        resetState();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        resetState();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        resetState();
    }

    private void resetState() {
        unsubscribe(produceWaterSubscription);
        hatchModifier = 0;
        fluidTank = null;
    }

    private void produceWater() {
        if (getOffsetTimer() % 20 == 0 && isFormed() && !getMultiblockState().hasError()) {
            if (biomeModifier == 0) {
                biomeModifier = GTUtil.getPumpBiomeModifier(getLevel().getBiome(getPos()));
            } else if (biomeModifier > 0) {
                if (fluidTank == null) initializeTank();
                if (fluidTank != null) {
                    fluidTank.handleRecipe(IO.OUT, null, List.of(FluidIngredient.of(GTMaterials.Water.getFluid(getFluidProduction()))), null, false);
                }
            }
        }
    }

    private boolean isRainingInBiome() {
        if (!getLevel().isRaining()) return false;
        return getBiomePrecipitation() != Precipitation.NONE;
    }

    private Precipitation getBiomePrecipitation() {
        return getLevel().getBiome(getPos()).value().getPrecipitationAt(getPos());
    }

    public long getFluidProduction() {
        long value = biomeModifier * hatchModifier;
        if (isRainingInBiome()) {
            value = value * 3 / 2;
        }
        return value;
    }
}
