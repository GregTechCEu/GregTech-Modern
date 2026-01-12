package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class MedicalConditionTestHelpers {

    public static void addMedicalCondition(GameTestHelper helper, Player player,
                                           MedicalCondition condition, float counts) {
        getMedicalConditionTracker(helper, player).progressCondition(condition, counts);
    }

    public static void removeMedicalCondition(GameTestHelper helper, Player player, MedicalCondition condition) {
        getMedicalConditionTracker(helper, player).removeMedicalCondition(condition);
    }

    public static MedicalConditionTracker getMedicalConditionTracker(GameTestHelper helper, Player player) {
        MedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        helper.assertTrue(tracker != null,
                "Player " + player + " doesn't have a medical condition tracker capability.");
        return tracker;
    }

    public static void assertHasCondition(GameTestHelper helper, Player player, MedicalCondition condition) {
        helper.assertTrue(getMedicalConditionTracker(helper, player).medicalConditions.containsKey(condition),
                "Player " + player + " should have medical condition " + condition.id);
    }

    public static void assertFreeOfCondition(GameTestHelper helper, Player player, MedicalCondition condition) {
        helper.assertFalse(getMedicalConditionTracker(helper, player).medicalConditions.containsKey(condition),
                "Player " + player + " should not have medical condition " + condition.id);
    }

    public static void assertConditionCountEquals(GameTestHelper helper, Player player,
                                                  MedicalCondition condition, float count) {
        helper.assertTrue(
                Mth.equal(getMedicalConditionTracker(helper, player).medicalConditions.getFloat(condition), count),
                "Player " + player + " should have " + count + " 'counts' of medical condition " + condition.id);
    }

    public static AttributeModifier getAndAssertAttributeModifier(GameTestHelper helper, Player player,
                                                                  Attribute attribute, UUID modifierId) {
        AttributeInstance instance = player.getAttribute(attribute);
        helper.assertTrue(instance != null,
                "Player " + player + " should have attribute " + attribute.getDescriptionId());
        assert instance != null;
        AttributeModifier modifier = instance.getModifier(modifierId);
        helper.assertTrue(modifier != null,
                "Player " + player + " should have a modifier with UUID " + modifierId + " for attribute " +
                        attribute.getDescriptionId());
        return modifier;
    }
}
