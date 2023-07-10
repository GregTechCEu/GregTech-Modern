package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.lowdragmc.lowdraglib.gui.factory.UIEditorFactory;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/9
 * @implNote ServerCommands
 */
public class ServerCommands {
    public static List<LiteralArgumentBuilder<CommandSourceStack>> createServerCommands() {
        return List.of(
                Commands.literal("gtceu")
                        .then(Commands.literal("ui_editor")
                                .executes(context -> {
                                    GTUIEditorFactory.INSTANCE.openUI(GTUIEditorFactory.INSTANCE, context.getSource().getPlayerOrException());
                                    return 1;
                                })
                        )
        );
    }
}
