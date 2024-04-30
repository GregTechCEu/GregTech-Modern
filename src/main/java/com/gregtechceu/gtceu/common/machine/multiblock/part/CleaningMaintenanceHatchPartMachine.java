package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleaningMaintenanceHatchPartMachine extends AutoMaintenanceHatchPartMachine {
    protected static final Set<CleanroomType> CLEANROOM = new ObjectOpenHashSet<>();
    protected static final Set<CleanroomType> STERILE_CLEANROOM = new ObjectOpenHashSet<>();

    static {
        CLEANROOM.add(CleanroomType.CLEANROOM);
        STERILE_CLEANROOM.add(CleanroomType.CLEANROOM);
        STERILE_CLEANROOM.add(CleanroomType.STERILE_CLEANROOM);
    }

    // must come after the static block
    private static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.createForTypes(CLEANROOM);
    private static final ICleanroomProvider STERILE_DUMMY_CLEANROOM = DummyCleanroom.createForTypes(STERILE_CLEANROOM);

    ICleanroomProvider cleanroomTypes;
    public CleaningMaintenanceHatchPartMachine(IMachineBlockEntity metaTileEntityId,ICleanroomProvider cleanroomTypes) {
        super(metaTileEntityId);
        this.cleanroomTypes=cleanroomTypes;
    }
    public static CleaningMaintenanceHatchPartMachine Cleaning(IMachineBlockEntity metaTileEntityId){
        return new CleaningMaintenanceHatchPartMachine(metaTileEntityId,DUMMY_CLEANROOM);
    }
    public static CleaningMaintenanceHatchPartMachine SterileCleaning(IMachineBlockEntity metaTileEntityId){
        return new CleaningMaintenanceHatchPartMachine(metaTileEntityId,STERILE_DUMMY_CLEANROOM);
    }


    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(cleanroomTypes);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver && receiver.getCleanroom() == cleanroomTypes) {
            receiver.setCleanroom(null);
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
    public static void addCleanroomType(@NotNull CleanroomType type) {
        CLEANROOM.add(type);
    }

    /**
     * @return the {@link CleanroomType}s this hatch provides to multiblocks
     */
    @SuppressWarnings("unused")
    public static ImmutableSet<CleanroomType> getCleanroomTypes() {
        return ImmutableSet.copyOf(CLEANROOM);
    }
    public static ImmutableSet<CleanroomType> getCleanroomTypesSTERILE() {
        return ImmutableSet.copyOf(STERILE_CLEANROOM);
    }

}
