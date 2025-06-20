package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class CommandLang {

    public static void init(RegistrateLangProvider provider) {
        generateCommandLang(provider);
    }

    private static void generateCommandLang(RegistrateLangProvider provider) {
        provider.add("command.gtceu.dump_data.success", "Dumped %s resources from registry %s to %s");
        provider.add("command.gtceu.place_vein.failure", "Failed to place vein %s at position %s");
        provider.add("command.gtceu.place_vein.success", "Placed vein %s at position %s");
        provider.add("command.gtceu.share_prospection_data.notification", "%s is sharing prospecting data with you!");
        provider.add("command.gtceu.medical_condition.get", "Player %s has these medical conditions:");
        provider.add("command.gtceu.medical_condition.get.empty", "Player %s has no medical conditions.");
        provider.add("command.gtceu.medical_condition.get.element", "Condition %s§r: %s minutes %s seconds");
        provider.add("command.gtceu.medical_condition.get.element.permanent",
                "Condition %s§r: %s minutes %s seconds (permanent)");

        provider.add("command.gtceu.usage", "Usage: /gtceu <worldgen/hand/recipecheck>");
        provider.add("command.gtceu.worldgen.usage", "Usage: /gtceu worldgen <reload>");
        provider.add("command.gtceu.worldgen.reload.usage", "Usage: /gtceu worldgen reload");
        provider.add("command.gtceu.worldgen.reload.success", "Worldgen successfully reloaded from config.");
        provider.add("command.gtceu.worldgen.reload.failed",
                "Worldgen reload failed. Check console for errors.");
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
        provider.add("command.gtceu.recipecheck.usage", "Usage: /gtceu recipecheck");
        provider.add("command.gtceu.recipecheck.begin", "Starting recipe conflict check...");
        provider.add("command.gtceu.recipecheck.end",
                "Recipe conflict check found %d possible conflicts. Check the server log for more info");
        provider.add("command.gtceu.recipecheck.end_no_conflicts", "No recipe conflicts found!");
        provider.add("command.gtceu.copy.copied_and_click", "Copied to clipboard. Click to copy again");
        provider.add("command.gtceu.copy.click_to_copy", "Click to copy");
        provider.add("command.gtceu.copy.copied_start", "Copied [");
        provider.add("command.gtceu.copy.copied_end", "] to the clipboard");
        provider.add("gtceu.chat.cape",
                "§5Congrats: you just unlocked a new cape! See the Cape Selector terminal app to use it.§r");
    }
}
