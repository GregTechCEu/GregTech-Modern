package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ResourceKeyArgument.class)
public interface ResourceKeyArgumentAccessor {

    @Invoker
    static <T> Holder.Reference<T> callResolveKey(CommandContext<CommandSourceStack> context, String argument,
                                                  ResourceKey<Registry<T>> registryKey,
                                                  DynamicCommandExceptionType exception) throws CommandSyntaxException {
        throw new AssertionError();
    }
}
