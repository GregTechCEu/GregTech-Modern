package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class SteamWorkableMachine extends SteamMachine
                                           implements IRecipeLogicMachine, IMufflableMachine {

    @Getter
    protected final CleanroomReceiverTrait cleanroomReceiver;
    @Getter
    @SaveField
    @SyncToClient
    public final RecipeLogic recipeLogic;
    @Getter
    public final GTRecipeType[] recipeTypes;
    @Getter
    @Setter
    public int activeRecipeType;
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected Direction outputFacing;
    @SaveField
    @SyncToClient
    @Getter
    @Setter
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;
    @Getter
    protected final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
    @Getter
    protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;
    protected final List<ISubscription> traitSubscriptions;

    public SteamWorkableMachine(BlockEntityCreationInfo info, boolean isHighPressure,
                                RecipeLogic recipeLogic,
                                NotifiableFluidTank steamTank) {
        super(info, isHighPressure, steamTank);
        this.recipeTypes = getDefinition().getRecipeTypes();
        this.activeRecipeType = 0;
        this.cleanroomReceiver = attachTrait(new CleanroomReceiverTrait());
        this.recipeLogic = attachTrait(recipeLogic);
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.traitSubscriptions = new ArrayList<>();
        this.outputFacing = hasFrontFacing() ? getFrontFacing().getOpposite() : Direction.UP;
    }

    public SteamWorkableMachine(BlockEntityCreationInfo info, boolean isHighPressure,
                                RecipeLogic recipeLogic) {
        this(info, isHighPressure, recipeLogic,
                new NotifiableFluidTank(1, 16 * FluidType.BUCKET_VOLUME, IO.IN));
    }

    public SteamWorkableMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        this(info, isHighPressure, new RecipeLogic());
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        // attach self traits
        Map<IO, List<IRecipeHandler<?>>> ioTraits = new Object2ObjectOpenHashMap<>();

        for (MachineTrait trait : getAllTraits()) {
            if (trait instanceof IRecipeHandlerTrait<?> handlerTrait) {
                ioTraits.computeIfAbsent(handlerTrait.getHandlerIO(), i -> new ArrayList<>()).add(handlerTrait);
            }
        }

        for (var entry : ioTraits.entrySet()) {
            var handlerList = RecipeHandlerList.of(entry.getKey(), entry.getValue());
            this.addHandlerList(handlerList);
            traitSubscriptions.add(handlerList.subscribe(recipeLogic::updateTickSubscription));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        capabilitiesProxy.clear();
        capabilitiesFlat.clear();
    }

    public boolean hasOutputFacing() {
        return true;
    }

    /**
     * @param outputFacing the facing to set
     */
    public void setOutputFacing(Direction outputFacing) {
        if (hasOutputFacing() && (!hasFrontFacing() || this.outputFacing != getFrontFacing())) {
            this.outputFacing = outputFacing;
        }
    }

    public @Nullable Direction getOutputFacing() {
        if (hasOutputFacing()) {
            return outputFacing;
        }
        return null;
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == getOutputFacing()) {
            return false;
        }
        return super.isFacingValid(facing);
    }

    @Override
    protected InteractionResult onWrenchClick(ExtendedUseOnContext context) {
        var gridSide = context.getGridSide();
        var player = context.getPlayer();
        if (!player.isShiftKeyDown()) {
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;
            setOutputFacing(gridSide);
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.onWrenchClick(context);
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

    @Override
    public GTRecipeType getRecipeType() {
        return recipeTypes[activeRecipeType];
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (previouslyMuffled != isMuffled) {
            previouslyMuffled = isMuffled;

            recipeLogic.updateSound();
        }
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }
}
