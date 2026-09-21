package com.gregtechceu.gtceu.api.item.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ItemStackDataPortTest {

    private static RegistryAccess registries;

    @BeforeAll
    static void setup() {
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        // The unit loader does not load server data packs. Bind a minimal real vanilla item fixture.
        if (!Items.PAPER.builtInRegistryHolder().areComponentsBound()) {
            Items.PAPER.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
    }

    private static ItemStack stack() {
        return new ItemStack(Items.PAPER);
    }

    @Test
    void readsPreserveAbsentComponentsAndDefaults() {
        var stack = stack();
        var before = stack.copy();
        assertEquals(1200, ElectricItemData.getMaxCharge(stack, 1200));
        assertEquals(0, ElectricItemData.getCharge(stack, 1200));
        assertEquals(0, ElectricItemData.getCharge(stack, -1));
        assertFalse(ElectricItemData.isActive(stack));
        assertFalse(ElectricItemData.isDischargeMode(stack));
        assertEquals(new NightVisionItemData(false, (byte) 0, 300), NightVisionItemData.read(stack, 300));
        ItemStackData.read(stack).putString("detached", "value");
        assertFalse(stack.has(DataComponents.CUSTOM_DATA));
        assertTrue(ItemStack.matches(before, stack));
    }

    @Test
    void mutationsAreIsolatedFromCopiesAndEscapedTags() {
        var original = stack();
        ItemStackData.updateCompound(original, "GT.PartStats", part -> part.putInt("Damage", 3));
        var copy = original.copy();
        var retained = new AtomicReference<CompoundTag>();
        ItemStackData.updateCompound(copy, "GT.PartStats", part -> {
            part.putInt("Damage", 8);
            retained.set(part);
        });
        retained.get().putInt("Damage", 99);
        assertEquals(3, ItemStackData.read(original).getCompoundOrEmpty("GT.PartStats").getIntOr("Damage", 0));
        assertEquals(8, ItemStackData.read(copy).getCompoundOrEmpty("GT.PartStats").getIntOr("Damage", 0));
        assertFalse(ItemStack.isSameItemSameComponents(original, copy));
    }

    @Test
    void failedMutationDoesNotCommitPartialData() {
        var stack = stack();
        ElectricItemData.setCharge(stack, 23);
        var before = stack.copy();
        assertThrows(IllegalStateException.class, () -> ItemStackData.update(stack, tag -> {
            tag.putLong("Charge", 100);
            throw new IllegalStateException("abort");
        }));
        assertTrue(ItemStack.matches(before, stack));
    }

    @Test
    void electricFieldsPreserveLegacyNumericAndInfiniteSemantics() {
        var stack = stack();
        ItemStackData.update(stack, tag -> tag.putInt("MaxCharge", 99));
        assertEquals(1200, ElectricItemData.getMaxCharge(stack, 1200)); // Override requires a long tag.
        ElectricItemData.setMaxCharge(stack, 100);
        ElectricItemData.setCharge(stack, 140);
        assertEquals(100, ElectricItemData.getCharge(stack, ElectricItemData.getMaxCharge(stack, 1200)));
        ElectricItemData.setCharge(stack, -7);
        assertEquals(-7, ElectricItemData.getCharge(stack, 100)); // Do not silently add a new lower clamp.
        ElectricItemData.setInfinite(stack, true);
        assertEquals(100, ElectricItemData.getCharge(stack, 100));
        ElectricItemData.setInfinite(stack, false);
        assertEquals(-7, ElectricItemData.getCharge(stack, 100));
        ItemStackData.update(stack, tag -> tag.putInt("Charge", 42));
        assertEquals(42, ElectricItemData.getCharge(stack, 100));
    }

    @Test
    void dischargeModeRemovesOnlyItsFieldAndCanonicalizesEmptyData() {
        var stack = stack();
        ElectricItemData.setDischargeMode(stack, true);
        assertTrue(ElectricItemData.isDischargeMode(stack));
        ElectricItemData.setDischargeMode(stack, false);
        assertFalse(stack.has(DataComponents.CUSTOM_DATA));
        assertTrue(ItemStack.isSameItemSameComponents(stack, stack()));
        ElectricItemData.setCharge(stack, 35);
        ElectricItemData.setActive(stack, false);
        ElectricItemData.setDischargeMode(stack, true);
        ElectricItemData.setDischargeMode(stack, false);
        assertEquals(35, ElectricItemData.getCharge(stack, 100));
        assertTrue(ItemStackData.read(stack).contains("Active")); // Explicit false remains stored.
        assertFalse(ItemStackData.read(stack).contains("DischargeMode"));
    }

    @Test
    void armorStateCommitPreservesInterveningDischarge() {
        var stack = stack();
        ElectricItemData.setCharge(stack, 100);
        var armor = NightVisionItemData.read(stack, 300);
        ElectricItemData.setCharge(stack, 90);
        new NightVisionItemData(true, (byte) 5, armor.effectTimer() - 1).save(stack);
        assertEquals(90, ElectricItemData.getCharge(stack, 100));
        assertEquals(new NightVisionItemData(true, (byte) 5, 299), NightVisionItemData.read(stack, 300));
    }

    @Test
    void armorDefaultsAndTimerOnlyUpdatesPreserveLegacyFields() {
        var stack = stack();
        new NightVisionItemData(true, (byte) 2, 42).save(stack, false);
        assertFalse(ItemStackData.read(stack).contains("nightVision"));
        ItemStackData.update(stack, tag -> {
            tag.putString("nightVisionTimer", "invalid");
            tag.putBoolean("nightVision", true);
        });
        assertEquals(0, NightVisionItemData.read(stack, 300).effectTimer());
        new NightVisionItemData(false, (byte) 1, 9).save(stack, false);
        assertTrue(NightVisionItemData.read(stack, 300).enabled());
    }

    @Test
    void nestedUpdatesRetainOtherDataAndReplaceWrongType() {
        var stack = stack();
        ItemStackData.update(stack, tag -> {
            tag.putString("OtherMod", "preserve");
            tag.putString("GT.PartStats", "malformed");
        });
        ItemStackData.updateCompound(stack, "GT.PartStats", part -> part.putString("Material", "gtceu:steel"));
        ItemStackData.updateCompound(stack, "GT.PartStats", part -> part.putInt("Damage", 4));
        assertEquals("preserve", ItemStackData.read(stack).getStringOr("OtherMod", ""));
        var part = ItemStackData.read(stack).getCompoundOrEmpty("GT.PartStats");
        assertEquals("gtceu:steel", part.getStringOr("Material", ""));
        assertEquals(4, part.getIntOr("Damage", 0));
    }

    private static ItemStack populatedStack() {
        var stack = stack();
        stack.setCount(3);
        ElectricItemData.setMaxCharge(stack, 1000);
        ElectricItemData.setCharge(stack, 123);
        ElectricItemData.setInfinite(stack, false);
        ElectricItemData.setDischargeMode(stack, true);
        ElectricItemData.setActive(stack, true);
        new NightVisionItemData(true, (byte) 4, 297).save(stack);
        ArmorMovementItemData.setStepAssist(stack, true);
        ArmorMovementItemData.setStepToggleTimer(stack, (byte) 4);
        ArmorMovementItemData.setRunningTimer(stack, (byte) 7);
        ArmorMovementItemData.setBootsToggleTimer(stack, (byte) 9);
        ArmorMovementItemData.setBoostedJump(stack, true);
        ArmorMovementItemData.setOnGround(stack, false);
        ItemStackData.updateCompound(stack, "GT.PartStats", part -> part.putString("Material", "gtceu:steel"));
        return stack;
    }

    @Test
    void stackPersistencePreservesExactTagTypesAndCount() {
        var original = populatedStack();
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        var encoded = ItemStack.CODEC.encodeStart(ops, original).getOrThrow();
        var decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
        assertTrue(ItemStack.matches(original, decoded));
        assertEquals(1000, ElectricItemData.getMaxCharge(decoded, -1));
        ElectricItemData.setCharge(decoded, 1);
        assertEquals(123, ElectricItemData.getCharge(original, 1000));
    }

    @Test
    void vanillaStackNetworkCodecPreservesComponentsAndCount() {
        var original = populatedStack();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            ItemStack.STREAM_CODEC.encode(buffer, original);
            var decoded = ItemStack.STREAM_CODEC.decode(buffer);
            assertTrue(ItemStack.matches(original, decoded));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1000, ElectricItemData.getMaxCharge(decoded, -1));
        } finally {
            buffer.release();
        }
    }

    @Test
    void splittingRetainsIndependentComponents() {
        var original = populatedStack();
        var split = original.split(1);
        assertEquals(2, original.getCount());
        assertEquals(1, split.getCount());
        assertTrue(ItemStack.isSameItemSameComponents(original, split));
        ElectricItemData.setCharge(split, 77);
        assertEquals(123, ElectricItemData.getCharge(original, 1000));
        assertEquals(77, ElectricItemData.getCharge(split, 1000));
    }

    @Test
    void movementDefaultsPreserveDifferentToggleAndHandlerPolicies() {
        var stack = stack();
        assertFalse(ArmorMovementItemData.isStepAssistEnabled(stack));
        assertTrue(ArmorMovementItemData.shouldApplyStepAssist(stack));
        assertFalse(ArmorMovementItemData.isBoostedJumpEnabled(stack));
        assertTrue(ArmorMovementItemData.wasOnGround(stack));
        assertEquals(0, ArmorMovementItemData.getStepToggleTimer(stack));
        assertEquals(10, ArmorMovementItemData.getRunningTimer(stack, (byte) 10));
        assertEquals(6, ArmorMovementItemData.getBootsToggleTimer(stack, (byte) 6));
        assertFalse(stack.has(DataComponents.CUSTOM_DATA));
        ArmorMovementItemData.setStepAssist(stack, false);
        assertFalse(ArmorMovementItemData.shouldApplyStepAssist(stack));
        ArmorMovementItemData.setStepAssist(stack, true);
        assertTrue(ArmorMovementItemData.isStepAssistEnabled(stack));
        assertTrue(ArmorMovementItemData.shouldApplyStepAssist(stack));
    }

    @Test
    void movementTimersRetainNumericWidthsAndByteReadConversion() {
        var stack = stack();
        ArmorMovementItemData.setStepToggleTimer(stack, (byte) 4);
        ArmorMovementItemData.setBootsToggleTimer(stack, (byte) 9);
        ArmorMovementItemData.setRunningTimer(stack, (byte) 7);
        var tag = ItemStackData.read(stack);
        assertInstanceOf(net.minecraft.nbt.IntTag.class, tag.get("toggleStepTimer"));
        assertInstanceOf(net.minecraft.nbt.IntTag.class, tag.get("toggleBootsTimer"));
        assertInstanceOf(net.minecraft.nbt.ByteTag.class, tag.get("runningTimer"));
        ItemStackData.update(stack, data -> {
            data.putInt("toggleStepTimer", 260);
            data.putInt("toggleBootsTimer", 258);
            data.putInt("runningTimer", 255);
        });
        assertEquals(4, ArmorMovementItemData.getStepToggleTimer(stack));
        assertEquals(2, ArmorMovementItemData.getBootsToggleTimer(stack, (byte) 10));
        assertEquals(-1, ArmorMovementItemData.getRunningTimer(stack, (byte) 10));
    }

    @Test
    void malformedMovementFieldsUseLegacyPresentValueDefaults() {
        var stack = stack();
        ItemStackData.update(stack, tag -> {
            for (var key : java.util.List.of("stepAssist", "onGround", "boostedJump", "runningTimer",
                    "toggleBootsTimer", "toggleStepTimer")) tag.putString(key, "invalid");
        });
        assertFalse(ArmorMovementItemData.shouldApplyStepAssist(stack));
        assertFalse(ArmorMovementItemData.wasOnGround(stack));
        assertFalse(ArmorMovementItemData.isBoostedJumpEnabled(stack));
        assertEquals(0, ArmorMovementItemData.getRunningTimer(stack, (byte) 10));
        assertEquals(0, ArmorMovementItemData.getBootsToggleTimer(stack, (byte) 10));
        assertEquals(0, ArmorMovementItemData.getStepToggleTimer(stack));
    }

    @Test
    void movementWritesPreserveDischargeAndUnrelatedFields() {
        var stack = stack();
        ElectricItemData.setCharge(stack, 100);
        var timer = ArmorMovementItemData.getRunningTimer(stack, (byte) 10);
        var copy = stack.copy();
        ElectricItemData.setCharge(stack, 96);
        new NightVisionItemData(true, (byte) 3, 299).save(stack);
        ArmorMovementItemData.setRunningTimer(stack, (byte) (timer - 1));
        ArmorMovementItemData.setOnGround(stack, false);
        ArmorMovementItemData.setBoostedJump(stack, true);
        assertEquals(96, ElectricItemData.getCharge(stack, 100));
        assertEquals(new NightVisionItemData(true, (byte) 3, 299), NightVisionItemData.read(stack, 300));
        assertEquals(9, ArmorMovementItemData.getRunningTimer(stack, (byte) 10));
        assertFalse(ArmorMovementItemData.wasOnGround(stack));
        assertTrue(ArmorMovementItemData.isBoostedJumpEnabled(stack));
        assertFalse(ItemStackData.read(stack).contains("stepAssist"));
        assertEquals(100, ElectricItemData.getCharge(copy, 100));
        assertTrue(ArmorMovementItemData.wasOnGround(copy));
    }
}
