package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CommandLang {

    public static void init(GTLangProvider provider) {
        generateCommandLang(provider);
    }

    private static void generateCommandLang(GTLangProvider provider) {
        provider.add("command.gtceu.dump_data.success", "Dumped %s resources from registry %s to %s");
        provider.add("gtceu.debug.resource_rebuild.done", "Gradle resource rebuild done in %s");
        provider.add("gtceu.debug.resource_rebuild.start",
                "Invoking gradle resource rebuild (./gradlew :processResources)");

        // Prospection Commands
        provider.add("command.gtceu.place_vein.failure", "Failed to place vein %s at position %s");
        provider.add("command.gtceu.place_vein.success", "Placed vein %s at position %s");
        provider.add("command.gtceu.share_prospection_data.notification", "%s is sharing prospecting data with you!");

        // Medical Conditions
        provider.add("command.gtceu.medical_condition.clear.everything.failed", "Target has no conditions to remove");
        provider.add("command.gtceu.medical_condition.clear.everything.success.multiple",
                "Removed all conditions from %s targets");
        provider.add("command.gtceu.medical_condition.clear.everything.success.single",
                "Removed all conditions from %s");
        provider.add("command.gtceu.medical_condition.clear.specific.failed",
                "Target doesn't have the requested condition");
        provider.add("command.gtceu.medical_condition.clear.specific.success.multiple", "Removed %s from %s targets");
        provider.add("command.gtceu.medical_condition.clear.specific.success.single", "Removed %s from %s");
        provider.add("command.gtceu.medical_condition.give.failed", "Unable to apply this condition (invalid target)");
        provider.add("command.gtceu.medical_condition.give.success.multiple", "Applied %s to %s targets");
        provider.add("command.gtceu.medical_condition.give.success.single", "Applied %s to %s");
        provider.add("command.gtceu.medical_condition.get", "%s has");
        provider.add("command.gtceu.medical_condition.get.empty", "%s is perfectly healthy.");
        provider.add("command.gtceu.medical_condition.get.element", "- %s for %s minutes %s seconds");
        provider.add("command.gtceu.medical_condition.get.element.permanent",
                "- %s for %s minutes %s seconds (permanent)");
        provider.add("command.gtceu.medical_condition.get.symptoms.empty", "%s has no symptoms.");
        provider.add("command.gtceu.medical_condition.get.symptoms", "Currently %s has these symptoms:");
        provider.add("command.gtceu.medical_condition.get.symptoms.element", "- %s");

        // GT Worldgen
        provider.add("command.gtceu.usage", "Usage: /gtceu <worldgen/hand/recipecheck>");
        provider.add("command.gtceu.worldgen.usage", "Usage: /gtceu worldgen <reload>");
        provider.add("command.gtceu.worldgen.reload.usage", "Usage: /gtceu worldgen reload");
        provider.add("command.gtceu.worldgen.reload.success", "Worldgen successfully reloaded from config.");
        provider.add("command.gtceu.worldgen.reload.failed",
                "Worldgen reload failed. Check console for errors.");

        // Cape Message
        provider.add("gtceu.chat.cape",
                "§5Congrats: you just unlocked a new cape! See the Cape Selector terminal app to use it.§r");

        // New Cape Commands
        provider.add("command.gtceu.cape.give.failed", "No new capes were unlocked");
        provider.add("command.gtceu.cape.give.success.multiple", "Unlocked %s capes for %s players");
        provider.add("command.gtceu.cape.give.success.single", "Unlocked %s capes for %s");
        provider.add("command.gtceu.cape.take.failed", "No capes could be removed");
        provider.add("command.gtceu.cape.take.success.multiple", "Took %s capes from %s players");
        provider.add("command.gtceu.cape.take.success.single", "Took %s capes from %s");
        provider.add("command.gtceu.cape.use.failed",
                "%s can't use cape %s because they don't have it (or it doesn't exist)!");
        provider.add("command.gtceu.cape.use.success", "%s is now using cape %s");
        provider.add("command.gtceu.cape.use.success.none", "%s is no longer using a cape");
    }
}
