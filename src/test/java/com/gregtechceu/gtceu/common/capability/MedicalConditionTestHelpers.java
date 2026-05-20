package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(TestUtils.class)
public class MedicalConditionTestHelpers {

    public static void addMedicalConditionCounts(GameTestHelper helper, Player player,
                                                 MedicalCondition condition, float counts) {
        getMedicalConditionTracker(helper, player).progressCondition(condition, counts);
    }

    public static void setMedicalConditionCounts(GameTestHelper helper, Player player,
                                                 MedicalCondition condition, float counts) {
        MedicalConditionTracker tracker = getMedicalConditionTracker(helper, player);
        tracker.medicalConditions.put(condition, counts);
        tracker.updateActiveSymptoms();
    }

    public static void clearMedicalCondition(GameTestHelper helper, Player player, MedicalCondition condition) {
        getMedicalConditionTracker(helper, player).removeMedicalCondition(condition);
    }

    public static MedicalConditionTracker getMedicalConditionTracker(GameTestHelper helper, Player player) {
        helper.assertEntityAlive(player);
        MedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        helper.assertTrue(tracker != null,
                "Player " + player + " doesn't have a medical condition tracker capability");
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
                                                  MedicalCondition condition, float expectedCounts) {
        float counts = getMedicalConditionTracker(helper, player).medicalConditions.getFloat(condition);
        helper.assertTrue(Mth.equal(counts, expectedCounts),
                "Player " + player + " should have " + expectedCounts + " 'counts' of medical condition " +
                        condition.id + ", has " + counts);
    }
}
