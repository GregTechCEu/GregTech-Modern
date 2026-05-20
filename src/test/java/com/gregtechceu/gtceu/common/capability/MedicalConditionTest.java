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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
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
    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testMedicalConditionTicking(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockSurvivalServerPlayer();
        // add a 'reasonable' count of nausea (450 counts)
        helper.addMedicalConditionCounts(player, GTMedicalConditions.NAUSEA, 640);

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                // this clears 10 counts of nausea
                .thenExecuteFor(2 * 20, () -> helper.tickEntity(player))
                // check if player has nausea effect
                // they should, as the nausea condition hasn't gone below 600 'counts' yet
                .thenExecute(() -> helper.assertTrue(player.hasEffect(MobEffects.CONFUSION),
                        "Player " + player + " should have nausea effect"))
                // remove extra nausea
                .thenExecute(() -> helper.addMedicalConditionCounts(player, GTMedicalConditions.NAUSEA, -550))
                // nausea condition lowers by 5 'counts' per second
                // so the player should have it for another (80 / 5) = 16 seconds
                // -1 because the player is ticked once during init
                .thenExecuteFor(16 * 20, () -> {
                    helper.assertHasCondition(player, GTMedicalConditions.NAUSEA);
                    helper.tickEntity(player);
                })
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
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
                // check if player has 100 'counts' of cancer
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // remove Nt ingot from player
                .thenExecute(() -> player.getInventory().clearContent())
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // add more cancer to reach max slowness symptom
                .thenExecute(() -> helper.setMedicalConditionCounts(player, GTMedicalConditions.CARCINOGEN, 18000))
                // // tick the medical condition tracker for 2 ticks, just to be safe
                // .thenExecuteFor(2, () -> helper.tickEntity(player))
                // check that the slowness attribute modifier is properly applied.
                .thenExecute(() -> {
                    double modifier = player.getAttributes().getModifierValue(Attributes.MOVEMENT_SPEED, Symptom.SYMPTOM_SLOWNESS_UUID);
                    // this value is based on the slowness symptom's default stage count and multiplier (7 and 0.08 respectively)
                    helper.assertTrue(Mth.equal(modifier, -7 * 0.05f),
                            "Slowness symprom attribute modifier should have a value of " + (-7 * 0.05f) + " at 18000 counts. (is " + modifier + ")");
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
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
                // check if player did NOT get asbestosis
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.ASBESTOSIS))
                // remove face mask
                .thenExecute(() -> player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY))
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
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
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
                // check if player did NOT get chemical burns
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.CHEMICAL_BURNS))
                // remove rubber gloves
                .thenExecute(() -> player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY))
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
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
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
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
                .thenExecuteFor(10 * 20, () -> helper.tickEntity(player))
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
        // add a low-ish count of weak poisoning
        helper.addMedicalConditionCounts(player, GTMedicalConditions.WEAK_POISON, 100);
        // give Player 16x Paracetamol
        ItemStack pillStack = GTItems.PARACETAMOL_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, () -> helper.tickEntity(player))
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // make player eat Paracetamol for 16 * 16 = 256 ticks
                .thenExecuteFor(16 * 16 + 1, () -> {
                    helper.tickEntity(player);
                    // constantly eat another item
                    var result = helper.useItem(player,
                            player.getItemInHand(InteractionHand.MAIN_HAND));
                    if (result.getResult().consumesAction()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
                    }
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
        // add a low-ish count of weak poisoning
        helper.addMedicalConditionCounts(player, GTMedicalConditions.CARCINOGEN, 100);
        // give Player 16x Paracetamol
        ItemStack pillStack = GTItems.PARACETAMOL_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, () -> helper.tickEntity(player))
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // make player eat Paracetamol for 16 * 16 = 256 ticks
                .thenExecuteFor(16 * 16 + 1, () -> {
                    helper.tickEntity(player);
                    // constantly eat another item
                    var result = helper.useItem(player,
                            player.getItemInHand(InteractionHand.MAIN_HAND));
                    if (result.getResult().consumesAction()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
                    }
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
        // add a low count of cancer
        helper.addMedicalConditionCounts(player, GTMedicalConditions.CARCINOGEN, 100);
        // give Player 16x RadAway
        ItemStack pillStack = GTItems.RAD_AWAY_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, () -> helper.tickEntity(player))
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // make player eat RadAway for 16 * 16 = 256 ticks
                .thenExecuteFor(16 * 16 + 1, () -> {
                    helper.tickEntity(player);
                    // constantly eat another item
                    var result = helper.useItem(player,
                            player.getItemInHand(InteractionHand.MAIN_HAND));
                    if (result.getResult().consumesAction()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
                    }
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
        // add a low-ish count of weak poisoning
        helper.addMedicalConditionCounts(player, GTMedicalConditions.WEAK_POISON, 100);
        // give Player 16x RadAway
        ItemStack pillStack = GTItems.RAD_AWAY_PILL.asStack(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pillStack);

        final long startTick = helper.getTick();

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, () -> helper.tickEntity(player))
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // make player eat RadAway for 16 * 16 = 256 ticks
                .thenExecuteFor(16 * 16 + 1, () -> {
                    helper.tickEntity(player);
                    // constantly eat another item
                    var result = helper.useItem(player,
                            player.getItemInHand(InteractionHand.MAIN_HAND));
                    if (result.getResult().consumesAction()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
                    }
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
