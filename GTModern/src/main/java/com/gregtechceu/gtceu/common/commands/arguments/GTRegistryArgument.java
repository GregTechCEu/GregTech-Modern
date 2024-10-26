package com.gregtechceu.gtceu.common.commands.arguments;

import com.gregtechceu.gtceu.api.registry.GTRegistry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class GTRegistryArgument<K, V> implements ArgumentType<V> {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(
            Component.translatable("argument.id.invalid"));

    private static final Collection<String> EXAMPLES = Arrays.asList("gtceu:iron_vein", "gtceu:pitchblende_vein_end",
            "gtceu:lava_deposit");

    private final GTRegistry<K, V> registry;
    private final Class<K> keyClass;

    public GTRegistryArgument(GTRegistry<K, V> registry, Class<K> keyClass) {
        this.registry = registry;
        this.keyClass = keyClass;
    }

    public static <K, V> GTRegistryArgument<K, V> registry(GTRegistry<K, V> registry, Class<K> keyClass) {
        return new GTRegistryArgument<>(registry, keyClass);
    }

    @SuppressWarnings("unchecked")
    public V parse(StringReader reader) throws CommandSyntaxException {
        String id = readId(reader);
        if (ResourceLocation.class.isAssignableFrom(keyClass)) {
            K loc = (K) new ResourceLocation(id);
            if (!registry.containKey(loc)) {
                throw new SimpleCommandExceptionType(new LiteralMessage("Failed to find object" + id + " in registry"))
                        .createWithContext(reader);
            }

            return registry.get(loc);
        } else if (String.class.isAssignableFrom(keyClass)) {
            K loc = (K) id;
            if (!registry.containKey(loc)) {
                throw new SimpleCommandExceptionType(Component.literal("Failed to find object " + id + " in registry"))
                        .createWithContext(reader);
            }
            return registry.get(loc);
        }
        throw new SimpleCommandExceptionType(Component.literal("Invalid key class! this should never happen!"))
                .createWithContext(reader);
    }

    public static String readId(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(cursor, reader.getCursor());

        try {
            return s;
        } catch (ResourceLocationException var4) {
            reader.setCursor(cursor);
            throw ERROR_INVALID.createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext,
                                                              SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(registry.keys(), string, Function.identity(), id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    static <T, K> void filterResources(Iterable<T> resources, String input, Function<T, K> locationFunction,
                                       Consumer<T> resourceConsumer) {
        for (T object : resources) {
            K id = locationFunction.apply(object);
            String string = id.toString();
            if (matchesSubStr(input, string)) {
                resourceConsumer.accept(object);
            }
        }
    }

    static boolean matchesSubStr(String input, String substring) {
        for (int i = 0; !substring.startsWith(input, i); ++i) {
            i = substring.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @MethodsReturnNonnullByDefault
    public static class Info<K, V>
                            implements
                            ArgumentTypeInfo<GTRegistryArgument<K, V>, GTRegistryArgument.Info<K, V>.Template> {

        public void serializeToNetwork(GTRegistryArgument.Info<K, V>.Template template, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(template.registryKey.getRegistryName());
            buffer.writeBoolean(ResourceLocation.class.isAssignableFrom(template.keyClass));
        }

        @SuppressWarnings("unchecked")
        public GTRegistryArgument.Info<K, V>.Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            ResourceLocation resourceLocation = buffer.readResourceLocation();
            Class<K> keyClass = (Class<K>) String.class;
            if (buffer.readBoolean()) {
                keyClass = (Class<K>) ResourceLocation.class;
            }
            // noinspection unchecked
            return new GTRegistryArgument.Info<K, V>.Template(
                    (GTRegistry<K, V>) GTRegistry.REGISTERED.get(resourceLocation), keyClass);
        }

        public void serializeToJson(GTRegistryArgument.Info<K, V>.Template template, JsonObject json) {
            json.addProperty("registry", template.registryKey.getRegistryName().toString());
        }

        public GTRegistryArgument.Info<K, V>.Template unpack(GTRegistryArgument<K, V> argument) {
            return new GTRegistryArgument.Info<K, V>.Template(argument.registry, argument.keyClass);
        }

        public final class Template implements ArgumentTypeInfo.Template<GTRegistryArgument<K, V>> {

            final GTRegistry<K, V> registryKey;
            final Class<K> keyClass;

            Template(GTRegistry<K, V> registryKey, Class<K> keyClass) {
                this.registryKey = registryKey;
                this.keyClass = keyClass;
            }

            public GTRegistryArgument<K, V> instantiate(@NotNull CommandBuildContext context) {
                return new GTRegistryArgument<>(this.registryKey, keyClass);
            }

            @Override
            public ArgumentTypeInfo<GTRegistryArgument<K, V>, ?> type() {
                return GTRegistryArgument.Info.this;
            }
        }
    }
}
