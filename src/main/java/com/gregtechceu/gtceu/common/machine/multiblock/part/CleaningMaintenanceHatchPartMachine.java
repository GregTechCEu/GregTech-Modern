package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.UV;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleaningMaintenanceHatchPartMachine extends AutoMaintenanceHatchPartMachine {

    // must come after the static block
    private final ICleanroomProvider DUMMY_CLEANROOM;

    @Getter
    private final CleanroomType cleanroomType;

    public CleaningMaintenanceHatchPartMachine(IMachineBlockEntity holder, CleanroomType cleanroomType) {
        super(holder);
        this.cleanroomType = cleanroomType;
        DUMMY_CLEANROOM = DummyCleanroom.createForTypes(Collections.singletonList(cleanroomType));
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(DUMMY_CLEANROOM);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver && receiver.getCleanroom() == DUMMY_CLEANROOM) {
            receiver.setCleanroom(null);
        }
    }

    @Override
    public int getTier() {
        return cleanroomType == CleanroomType.CLEANROOM ? UV : UHV;
    }
}
