package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Set;

public class CleaningMaintenanceHatchPartMachine extends AutoMaintenanceHatchPartMachine {
    protected static final Set<CleanroomType> CLEANED_TYPES = new ObjectOpenHashSet<>();

    static {
        CLEANED_TYPES.add(CleanroomType.CLEANROOM);
    }

    // must come after the static block
    private static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.createForTypes(CLEANED_TYPES);

    public CleaningMaintenanceHatchPartMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver && receiver.getCleanroom() == null) {
            receiver.setCleanroom(DUMMY_CLEANROOM);
        }
    }

    @Override
    public int getTier() {
        return GTValues.UV;
    }

    /**
     * Add an {@link CleanroomType} that is provided to multiblocks with this hatch
     *
     * @param type the type to add
     */
    @SuppressWarnings("unused")
    public static void addCleanroomType(@Nonnull CleanroomType type) {
        CLEANED_TYPES.add(type);
    }

    /**
     * @return the {@link CleanroomType}s this hatch provides to multiblocks
     */
    @SuppressWarnings("unused")
    public static ImmutableSet<CleanroomType> getCleanroomTypes() {
        return ImmutableSet.copyOf(CLEANED_TYPES);
    }

    @Nullable
    public IMultiController getController() {
        return getControllers().size() > 0 ? getControllers().get(0) : null;
    }
}
