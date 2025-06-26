package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CommandLang {

    public static void init(GTLangProvider provider) {
        generateCommandLang(provider);
    }

    private static void generateCommandLang(GTLangProvider provider) {
        provider.add("command.gtceu.dump_data.success", "Dumped %s resources from registry %s to %s");

        // Prospection Commands
        provider.add("command.gtceu.place_vein.failure", "Failed to place vein %s at position %s");
        provider.add("command.gtceu.place_vein.success", "Placed vein %s at position %s");
        provider.add("command.gtceu.share_prospection_data.notification", "%s is sharing prospecting data with you!");

        // Medical Conditions
        provider.add("command.gtceu.medical_condition.get", "Player %s has these medical conditions:");
        provider.add("command.gtceu.medical_condition.get.empty", "Player %s has no medical conditions.");
        provider.add("command.gtceu.medical_condition.get.element", "Condition %s§r: %s minutes %s seconds");
        provider.add("command.gtceu.medical_condition.get.element.permanent",
                "Condition %s§r: %s minutes %s seconds (permanent)");

        // GT Worldgen
        provider.add("command.gtceu.usage", "Usage: /gtceu <worldgen/hand/recipecheck>");
        provider.add("command.gtceu.worldgen.usage", "Usage: /gtceu worldgen <reload>");
        provider.add("command.gtceu.worldgen.reload.usage", "Usage: /gtceu worldgen reload");
        provider.add("command.gtceu.worldgen.reload.success", "Worldgen successfully reloaded from config.");
        provider.add("command.gtceu.worldgen.reload.failed",
                "Worldgen reload failed. Check console for errors.");

        // GT Hand (useless)
        provider.add("command.gtceu.hand.groovy", "Consider using §6/gs hand");
        provider.add("command.gtceu.hand.usage", "Usage: /gtceu hand");
        provider.add("command.gtceu.hand.item_id", "Item: %s (Metadata: %d)");
        provider.add("command.gtceu.hand.electric", "Electric Info: %d / %d EU - Tier: %d; Is Battery: %s");
        provider.add("command.gtceu.hand.fluid", "Fluid Info: %d / %d mB; Can Fill: %s; Can Drain: %s");
        provider.add("command.gtceu.hand.fluid2", "Fluid Id:");
        provider.add("command.gtceu.hand.material", "Material Id:");
        provider.add("command.gtceu.hand.ore_prefix", "Ore prefix:");
        provider.add("command.gtceu.hand.meta_item", "MetaItem Id:");
        provider.add("command.gtceu.hand.tag_entries", "§3Tag entries:");
        provider.add("command.gtceu.hand.tool_stats", "Tool Stats Class: %s");
        provider.add("command.gtceu.hand.not_a_player", "This command is only usable by a player.");
        provider.add("command.gtceu.hand.no_item",
                "You must hold something in main hand or off hand before executing this command.");

        // Recipe Check
        provider.add("command.gtceu.recipecheck.usage", "Usage: /gtceu recipecheck");
        provider.add("command.gtceu.recipecheck.begin", "Starting recipe conflict check...");
        provider.add("command.gtceu.recipecheck.end",
                "Recipe conflict check found %d possible conflicts. Check the server log for more info");
        provider.add("command.gtceu.recipecheck.end_no_conflicts", "No recipe conflicts found!");

        // Copy command
        provider.add("command.gtceu.copy.copied_and_click", "Copied to clipboard. Click to copy again");
        provider.add("command.gtceu.copy.click_to_copy", "Click to copy");
        provider.add("command.gtceu.copy.copied_start", "Copied [");
        provider.add("command.gtceu.copy.copied_end", "] to the clipboard");

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
