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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
        Player player = helper.makeMockSurvivalPlayer();
        // add 'max' count of nausea (600 seconds)
        helper.addMedicalCondition(player, GTMedicalConditions.NAUSEA, 600);

        helper.startSequence()
                // tick the medical condition tracker for 5 seconds
                .thenExecuteFor(5 * 20, player::tick)
                // check if player has nausea effect
                .thenExecute(() -> helper.assertTrue(player.hasEffect(MobEffects.CONFUSION),
                        "Player " + player + " should have nausea effect"))
                // nausea condition lowers by 5 'counts' per second
                // so the player should have it for another (600 / 5) - 5 = 115 seconds
                .thenExecuteFor(115 * 20, () -> helper.assertHasCondition(player, GTMedicalConditions.NAUSEA))
                // tick the medical condition tracker for 2 ticks, just to be safe
                .thenExecuteFor(2, player::tick)
                .thenExecute(() -> helper.assertFreeOfCondition(player, GTMedicalConditions.NAUSEA))
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 450)
    public static void testItemHazardApplication(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        // give player 1x Nt ingot (VERY radioactive, 10 'counts' per second)
        player.addItem(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Neutronium));

        helper.startSequence()
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::tick)
                // check if player has 100 'counts' of cancer
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // remove Nt ingot from player
                .thenExecute(() -> player.getInventory().clearContent())
                // tick the medical condition tracker for 10 seconds
                .thenExecuteFor(10 * 20, player::tick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // add more cancer to reach max mining fatigue symptom
                .thenExecute(() -> helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 14400))
                // tick the medical condition tracker for 2 ticks, just to be safe
                .thenExecuteFor(2, player::tick)
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

    @GameTest(template = "empty", batch = "medical_conditions", timeoutTicks = 350)
    public static void testGeneralAntidoteWorksOnWeakPoison(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        // give the player Regeneration 7 so they don't die
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.WEAK_POISON, 100);

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::tick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // give Player 16x Paracetamol and make them eat them
                .thenExecute(() -> {
                    ItemStack item = GTItems.PARACETAMOL_PILL.asStack(16);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    var result = helper.useItem(player, item);
                    helper.assertTrue(result.getResult() == InteractionResult.CONSUME,
                            "Using item " + item + " should result in CONSUME result, but got " + result.getResult());
                })
                // tick the player for 16 * 16 = 256 ticks to eat Paracetamol
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, player::tick)
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
        Player player = helper.makeMockSurvivalPlayer();
        // give the player Regeneration 7 so they don't die
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 100);

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::tick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // give Player 16x Paracetamol and make them eat them
                .thenExecute(() -> {
                    ItemStack item = GTItems.PARACETAMOL_PILL.asStack(16);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    var result = helper.useItem(player, item);
                    helper.assertTrue(result.getResult() == InteractionResult.CONSUME,
                            "Using item " + item + " should result in CONSUME result, but got " + result.getResult());
                })
                // tick the player for 16 * 16 = 256 ticks to eat Paracetamol
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, player::tick)
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
        Player player = helper.makeMockSurvivalPlayer();
        // give the player Regeneration 7 so they don't
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low count of cancer
        helper.addMedicalCondition(player, GTMedicalConditions.CARCINOGEN, 100);

        helper.startSequence()
                // tick the medical condition tracker for 2 seconds
                .thenExecuteFor(2 * 20, player::tick)
                // check that count hasn't changed
                .thenExecute(() -> helper.assertConditionCountEquals(player, GTMedicalConditions.CARCINOGEN, 100))
                // give Player 16x RadAway and make them eat them
                .thenExecute(() -> {
                    ItemStack item = GTItems.RAD_AWAY_PILL.asStack(16);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    var result = helper.useItem(player, item);
                    helper.assertTrue(result.getResult() == InteractionResult.CONSUME,
                            "Using item " + item + " should result in CONSUME result, but got " + result.getResult());
                })
                // tick the player for 16 * 16 = 256 ticks to eat RadAway
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, player::tick)
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
        Player player = helper.makeMockSurvivalPlayer();
        // give the player Regeneration 7 so they don't
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 6));
        // add a low-ish count of weak poisoning
        helper.addMedicalCondition(player, GTMedicalConditions.WEAK_POISON, 100);

        helper.startSequence()
                // wait for 2 seconds, check every tick that count hasn't changed
                .thenExecuteFor(2 * 20, () -> helper.assertConditionCountEquals(player, GTMedicalConditions.WEAK_POISON, 100))
                // give Player 16x RadAway and make them eat them
                .thenExecute(() -> {
                    ItemStack item = GTItems.RAD_AWAY_PILL.asStack(16);
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    var result = helper.useItem(player, item);
                    helper.assertTrue(result.getResult() == InteractionResult.CONSUME,
                            "Using item " + item + " should result in CONSUME result, but got " + result.getResult());
                })
                // tick the player for 16 * 16 = 256 ticks to eat RadAway
                // (+2 for safety)
                .thenExecuteFor(16 * 16 + 2, player::tick)
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
