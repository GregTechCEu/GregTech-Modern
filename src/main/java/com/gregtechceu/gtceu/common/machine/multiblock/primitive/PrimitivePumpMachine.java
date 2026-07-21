package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitivePumpMachine extends MultiblockControllerMachine implements IMuiMachine {

    private int biomeModifier = 0;
    private int hatchModifier = 0;
    private @Nullable NotifiableFluidTank fluidTank;
    private @Nullable TickableSubscription produceWaterSubscription;

    public PrimitivePumpMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(String name) {
        super.formStructure(name);
        initializeTank();
        produceWaterSubscription = subscribeServerTick(this::produceWater);
    }

    private void initializeTank() {
        for (MultiblockPartMachine part : getParts()) {
            var handlerLists = part.getRecipeHandlers();

            for (var handlerList : handlerLists) {
                var recipeCap = handlerList.getCapability(FluidRecipeCapability.CAP);
                if (handlerList.getHandlerIO().support(IO.OUT) && !recipeCap.isEmpty()) {
                    fluidTank = (NotifiableFluidTank) recipeCap.get(0);
                    long tankCapacity = fluidTank.getTankCapacity(0);
                    if (tankCapacity == FluidType.BUCKET_VOLUME) {
                        hatchModifier = 1;
                    } else if (tankCapacity == FluidType.BUCKET_VOLUME * 8) {
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
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
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
        if (getOffsetTimer() % 20 == 0 && isFormed()) {
            if (biomeModifier == 0) {
                biomeModifier = GTUtil.getPumpBiomeModifier(getLevel().getBiome(getBlockPos()));
            } else if (biomeModifier > 0) {
                if (fluidTank == null) initializeTank();
                if (fluidTank != null) {
                    fluidTank.handleRecipe(IO.OUT, null,
                            List.of(SizedFluidIngredient.of(GTMaterials.Water.getFluid(getFluidProduction()))), false);
                }
            }
        }
    }

    private boolean isRainingInBiome() {
        if (!getLevel().isRaining()) return false;
        return getBiomePrecipitation() != Precipitation.NONE;
    }

    private Precipitation getBiomePrecipitation() {
        return getLevel().getBiome(getBlockPos()).value().getPrecipitationAt(getBlockPos());
    }

    public int getFluidProduction() {
        int value = biomeModifier * hatchModifier;
        if (isRainingInBiome()) {
            value = value * 3 / 2;
        }
        return value;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>();
        listWidget
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft)
                .left(3)
                .top(3);
        parentWidget.size(width, height)
                .background(GuiTextures.DISPLAY);

        listWidget.children(getWidgetsForDisplay(syncManager));
        parentWidget.child(listWidget.left(3).top(3));
        return parentWidget;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager, 177, 77).margin(4, 2));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        // Sync these values for use in getFluidProduction()
        syncManager.getOrCreateSyncHandler("biomeModifier", IntSyncValue.class,
                () -> new IntSyncValue(() -> this.biomeModifier, value -> this.biomeModifier = value));
        syncManager.getOrCreateSyncHandler("hatchModifier", IntSyncValue.class,
                () -> new IntSyncValue(() -> this.hatchModifier, value -> this.hatchModifier = value));

        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(Text
                .dynamic(() -> Text
                        .lang("gtceu.top.primitive_pump_production",
                                FormattingUtil.formatNumbers(this.getFluidProduction()))
                        .withStyle(ChatFormatting.WHITE))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue()));

        return widgets;
    }
}
