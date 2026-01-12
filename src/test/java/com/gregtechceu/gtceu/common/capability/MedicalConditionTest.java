package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import lombok.experimental.ExtensionMethod;

@SuppressWarnings("unused")
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
@ExtensionMethod({ TestUtils.class, MedicalConditionTestHelpers.class })
public class MedicalConditionTest {

    @BeforeBatch(batch = "medical_conditions")
    public static void prepare(ServerLevel level) {}

    // spotless:off
    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 2450)
    public static void testMedicalConditionTicking(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // add 'max' count of nausea (600 seconds)
        helper.addMedicalCondition(player, GTMedicalConditions.NAUSEA, 600);

        helper.startSequence()
                // tick the medical condition tracker for 5 seconds
                .thenExecuteFor(5 * 20, player::doTick)
                // check if player has nausea effect
                .thenExecute(() -> helper.assertTrue(player.hasEffect(MobEffects.CONFUSION),
                        "Player " + player + " should have nausea effect"))
                // nausea condition lowers by 5 'counts' per second
                // so the player should have it for another (600 / 5) - 5 = 115 seconds
                .thenExecuteFor(115 * 20, () -> helper.assertHasCondition(player, GTMedicalConditions.NAUSEA))
                // tick the medical condition tracker for 2 ticks, just to be safe
                .thenExecuteFor(2, player::doTick)
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.NAUSEA))
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testItemHazardApplication(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // give player 1x Nt ingot (VERY radioactive, 10 'counts' per second)
        player.addItem(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Neutronium));

        helper.startSequence()
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player has 100 'counts' of cancer
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // remove Nt ingot from player
                .thenExecute(() -> player.getInventory().clearContent())
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // add more cancer to reach max mining fatigue symptom
                .thenExecute(() -> helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 14400))
                // tick the medical condition tracker for 2 ticks, just to be safe
                .thenExecuteFor(2, player::doTick)
                // check that the slowness attribute modifier is properly applied.
                .thenExecute(() -> {
                    AttributeModifier modifier = helper.getAndAssertAttributeModifier(player,
                            Attributes.MOVEMENT_SPEED, Symptom.SYMPTOM_SLOWNESS_UUID);
                    // this value is based on the slowness symptom's default stage count and multiplier (7 and 0.08 respectively)
                    helper.assertTrue(modifier.getAmount() == -7 * 0.08f,
                            "Slowness symprom attribute modifier should have a value of " + (-7 * 0.08f) + " after " + helper.getTick() + " ticks. (is " + modifier.getAmount() + ")");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testHazardProtectionInhalation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // equip face mask
        player.setItemSlot(EquipmentSlot.HEAD, GTItems.FACE_MASK.asStack());
        // give 16x asbestos dust
        player.addItem(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Asbestos, 16));

        helper.startSequence()
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player did NOT get asbestosis
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.ASBESTOSIS))
                // remove face mask
                .thenExecute(() -> player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY))
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player DID get asbestosis this time
                .thenExecute(() -> {
                    if (!player.isAlive()) return; // we don't care if the player died here, that means the asbestos got them
                    helper.assertHasCondition(player, GTMedicalConditions.ASBESTOSIS);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testHazardProtectionSkinContact(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // equip rubber gloves
        player.setItemSlot(EquipmentSlot.CHEST, GTItems.RUBBER_GLOVES.asStack());
        // give a bucket of Fluorine
        player.addItem(new ItemStack(GTMaterials.Fluorine.getBucket()));

        helper.startSequence()
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player did NOT get chemical burns
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.CHEMICAL_BURNS))
                // remove rubber gloves
                .thenExecute(() -> player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY))
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player DID get chemical burns this time
                .thenExecute(() -> {
                    if (!player.isAlive()) return; // we don't care if the player died here, that means the chemical burns got them
                    helper.assertHasCondition(player, GTMedicalConditions.CHEMICAL_BURNS);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testHazardProtectionAnyContact(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // equip hazmat suit
        player.setItemSlot(EquipmentSlot.HEAD, GTItems.HAZMAT_HELMET.asStack());
        player.setItemSlot(EquipmentSlot.CHEST, GTItems.HAZMAT_CHESTPLATE.asStack());
        player.setItemSlot(EquipmentSlot.LEGS, GTItems.HAZMAT_LEGGINGS.asStack());
        player.setItemSlot(EquipmentSlot.FEET, GTItems.HAZMAT_BOOTS.asStack());
        // give 16x Cadmium dust
        player.addItem(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Cadmium, 16));

        helper.startSequence()
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player did NOT get poisoned
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.POISON))
                // remove hazmat suit
                .thenExecute(() -> {
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                })
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::doTick)
                // check if player DID get poisoned this time
                .thenExecute(() -> {
                    if (!player.isAlive()) return; // we don't care if the player died here, that means the poisoning got them
                    helper.assertHasCondition(player, GTMedicalConditions.POISON);
                })
                .thenSucceed();
    }

    // TODO add test for consumption hazard if that ever gets used for anything

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 350)
    public static void testGeneralAntidoteWorksOnWeakPoison(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // give the player Regeneration 7 so they don't die
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.WEAK_POISON, 100);
        // give Player 16x Paracetamol
        ItemStack pillStack = GTItems.PARACETAMOL_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::doTick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // make player eat Paracetamol for 16 * 16 = 256 ticks
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, () -> {
                    player.doTick();
                    // constantly eat another item
                    helper.useItem(player, pillStack);
                })
                .thenExecute(() -> {
                    // check if they were all consumed
                    helper.assertHeldItemCountIs(player, Items.AIR, 0, InteractionHand.MAIN_HAND);
                    // check that the poisoning is gone
                    helper.assertFreeOfCondition(player, GTMedicalConditions.WEAK_POISON);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 350)
    public static void testGeneralAntidoteDoesntWorkOnCancer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // give the player Regeneration 7 so they don't die
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 100);
        // give Player 16x Paracetamol
        ItemStack pillStack = GTItems.PARACETAMOL_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::doTick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // make player eat Paracetamol for 16 * 16 = 256 ticks
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, () -> {
                    player.doTick();
                    // constantly eat another item
                    helper.useItem(player, pillStack);
                })
                .thenExecute(() -> {
                    // check if they were all consumed
                    helper.assertHeldItemCountIs(player, Items.AIR, 0, InteractionHand.MAIN_HAND);
                    // check that count is STILL 100, as Paracetamol shouldn't be able to remove cancer.
                    helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 350)
    public static void testRadAwayWorksOnCancer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // give the player Regeneration 7 so they don't
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low count of cancer
        helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 100);
        // give Player 16x RadAway
        ItemStack pillStack = GTItems.RAD_AWAY_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::doTick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // make player eat RadAway for 16 * 16 = 256 ticks
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, () -> {
                    player.doTick();
                    // constantly eat another item
                    helper.useItem(player, pillStack);
                })
                .thenExecute(() -> {
                    // check if they were all consumed
                    helper.assertHeldItemCountIs(player, Items.AIR, 0, InteractionHand.MAIN_HAND);
                    // check that the cancer is gone
                    helper.assertFreeOfCondition(player, GTMedicalConditions.CARCINOGEN);
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 350)
    public static void testRadAwayDoesntWorkOnWeakPoison(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // give the player Regeneration 7 so they don't
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.WEAK_POISON, 100);
        // give Player 16x RadAway
        ItemStack pillStack = GTItems.RAD_AWAY_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::doTick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // make player eat RadAway for 16 * 16 = 256 ticks
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, () -> {
                    player.doTick();
                    // constantly eat another item
                    helper.useItem(player, pillStack);
                })
                .thenExecute(() -> {
                    // check if they were all consumed
                    helper.assertHeldItemCountIs(player, Items.AIR, 0, InteractionHand.MAIN_HAND);
                    // check that count is STILL 100, as RadAway shouldn't be able to remove weak poisoning.
                    helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100);
                })
                .thenSucceed();
    }

    // spotless:on
}
