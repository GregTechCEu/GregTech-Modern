package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.BatterySlotTrait;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagCompatibilityFixer {

    public static void putTraitTag(MetaMachine machine, CompoundTag machineTag,
                                   MachineTraitType<?> type, String defaultName,
                                   CompoundTag data) {
        var traits = machine.getTraits(type);

        if (traits.size() != 1 || traits.get(0).getTraitName() == null) {
            machineTag.put(defaultName, data);
            return;
        }

        var persistent = traits.get(0).getTraitName();
        machineTag.getCompound("traitHolder").put(Objects.requireNonNull(persistent), data);
    }

    public static void fixTraitTags(MetaMachine machine, CompoundTag tag) {
        if (!tag.contains("traitHolder")) {
            tag.put("traitHolder", new CompoundTag());

            fixBatterySlotTag(machine, tag);
            fixProgrammedCircutTag(machine, tag);
            fixMachineAutoOutputTag(machine, tag);
        }
    }

    public static void fixBatterySlotTag(MetaMachine machine, CompoundTag tag) {
        if (tag.contains("chargerInventory")) {
            var newTag = new CompoundTag();
            newTag.put("storage", Objects.requireNonNull(tag.get("chargerInventory")));

            putTraitTag(machine, tag, BatterySlotTrait.TYPE, "batterySlot", newTag);
        }
    }

    public static void fixProgrammedCircutTag(MetaMachine machine, CompoundTag tag) {
        if (tag.contains("circuitInventory")) {
            var oldData = tag.getCompound("circuitInventory");

            putTraitTag(machine, tag, ProgrammableCircuitSlotTrait.TYPE, "circuitSlot", oldData);
        }
    }

    public static void fixMachineAutoOutputTag(MetaMachine machine, CompoundTag machineTag) {
        var outputTag = new CompoundTag();
        Tag itemOutputDirection = machineTag.get("outputFacingItems");
        Tag fluidOutputDirection = machineTag.get("outputFacingFluids");
        Tag autoOutputItems = machineTag.get("autoOutputItems");
        Tag autoOutputFluids = machineTag.get("autoOutputFluids");
        Tag allowInputItems = machineTag.get("allowInputFromOutputSideItems");
        Tag allowInputFluids = machineTag.get("allowInputFromOutputSideFluids");
        if (itemOutputDirection != null) outputTag.put("itemOutputDirection", itemOutputDirection);
        if (fluidOutputDirection != null) outputTag.put("fluidOutputDirection", fluidOutputDirection);
        if (autoOutputItems != null) outputTag.put("autoOutputItems", autoOutputItems);
        if (autoOutputFluids != null) outputTag.put("autoOutputFluids", autoOutputFluids);
        if (allowInputItems != null) outputTag.put("allowItemInputFromOutputSide", allowInputItems);
        if (allowInputFluids != null) outputTag.put("allowFluidInputFromOutputSide", allowInputFluids);

        putTraitTag(machine, machineTag, AutoOutputTrait.TYPE, "autoOutput", outputTag);
    }

    public static Direction fixUpwardsFacing(Direction frontfacing, Direction upwardsFacing) {
        Direction newUpwards;
        switch (frontfacing) {
            case NORTH: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.WEST;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.EAST;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case EAST: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.NORTH;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.SOUTH;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case SOUTH: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.EAST;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.WEST;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case WEST: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.SOUTH;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.NORTH;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case DOWN:
            case UP:
            default: {
                newUpwards = upwardsFacing;
                // needed if previous machine did not have an upwards facing
                if (upwardsFacing == null) {
                    newUpwards = Direction.NORTH;
                }
            }
        }
        return newUpwards;
    }

    public static Tag stripLDLibPayloadWrapper(Tag t) {
        if (!(t instanceof CompoundTag tag)) return t;
        if (tag.contains("p") && tag.contains("t")) {
            return tag.getCompound("p");
        }
        if (tag.contains("t", Tag.TAG_COMPOUND)) {
            return tag.getCompound("t").getCompound("p");
        }
        return tag;
    }
}
