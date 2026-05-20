package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomProviderTrait;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.UV;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleaningMaintenanceHatchPartMachine extends AutoMaintenanceHatchPartMachine {

    private final CleanroomProviderTrait cleanroomProvider;

    @Getter
    private final CleanroomType cleanroomType;

    public CleaningMaintenanceHatchPartMachine(BlockEntityCreationInfo info, CleanroomType cleanroomType) {
        super(info);
        this.cleanroomType = cleanroomType;
        this.cleanroomProvider = attachTrait(new CleanroomProviderTrait(Set.of(cleanroomType)));
        cleanroomProvider.setActive(true);
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        super.addedToController(controller);
        controller.self().getTraitOptional(CleanroomReceiverTrait.TYPE)
                .ifPresent(t -> t.setCleanroomProvider(cleanroomProvider));
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        controller.self().getTraitOptional(CleanroomReceiverTrait.TYPE)
                .ifPresent(CleanroomReceiverTrait::removeCleanroom);
    }

    @Override
    public int getTier() {
        return cleanroomType == CleanroomType.CLEANROOM ? UV : UHV;
    }
}
