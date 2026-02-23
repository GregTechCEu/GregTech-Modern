package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnclosedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnexpectedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnknownPlaceholderException;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.data.datagen.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlaceholderHandler {

    private static final char ARG_SEPARATOR = ' ';
    private static final char PLACEHOLDER_BEGIN = '{';
    private static final char PLACEHOLDER_END = '}';
    private static final char ESCAPE = '\\';
    private static final char LITERAL_ESCAPE = '"';
    private static final char NEWLINE = '\n';
    private static final char ESCAPED_NEWLINE = 'n';

    private static final Map<String, Placeholder> placeholders = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static final class RendererHolder {

        public static final Map<String, IPlaceholderRenderer> renderers = new HashMap<>();
    }

    public static void addPlaceholder(Placeholder placeholder) {
        if (placeholders.containsKey(placeholder.getName())) {
            if (placeholders.get(placeholder.getName()).getPriority() <= placeholder.getPriority()) {
                placeholders.put(placeholder.getName(), placeholder);
            }
        } else placeholders.put(placeholder.getName(), placeholder);
    }

    public static boolean placeholderExists(MultiLineComponent placeholder) {
        return placeholders.containsKey(placeholder.toString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void addRenderer(String id, IPlaceholderRenderer renderer) {
        RendererHolder.renderers.put(id, renderer);
    }

    @OnlyIn(Dist.CLIENT)
    public static @Nullable IMonitorRenderer getRenderer(String id, CompoundTag renderData) {
        if (!RendererHolder.renderers.containsKey(id)) {
            GTCEu.LOGGER.warn("Attempt to access a placeholder renderer that doesn't exist ({})", id);
            return null;
        }
        IPlaceholderRenderer renderer = RendererHolder.renderers.get(id);
        CompoundTag tag = renderData.copy();
        return (machine, group,
                partialTick, poseStack, buffer,
                packedLight, packedOverlay) -> renderer.render(
                        machine, group,
                        partialTick, poseStack, buffer,
                        packedLight, packedOverlay, tag);
    }

    public static MultiLineComponent processPlaceholder(List<MultiLineComponent> placeholder,
                                                        PlaceholderContext context,
                                                        Object2IntOpenHashMap<String> indices) throws PlaceholderException {
        if (!placeholderExists(placeholder.get(0)))
            throw new UnknownPlaceholderException(placeholder.get(0).toString());
        String name = placeholder.get(0).toString();
        indices.addTo(name, 1);
        return placeholders.get(name).apply(context.withIndex(indices.getInt(name)),
                placeholder.subList(1, placeholder.size()));
    }

    public static MultiLineComponent processPlaceholders(String s, PlaceholderContext ctx) {
        if (ctx.level().isClientSide)
            GTCEu.LOGGER.warn("Placeholder processing is running on client instead of server!");
        List<Exception> exceptions = new ArrayList<>();
        Object2IntOpenHashMap<String> indices = new Object2IntOpenHashMap<>();
        boolean escape = false;
        boolean escapeNext = false;
        boolean literalEscape = false;
        int line = 1;
        int symbol = 1;
        Stack<List<MultiLineComponent>> stack = new Stack<>();
        stack.push(GTUtil.list(MultiLineComponent.empty()));
        try {
            for (char c : s.toCharArray()) {
                if (escape || (literalEscape && c != LITERAL_ESCAPE)) {
                    if (c == ESCAPED_NEWLINE && !literalEscape) {
                        stack.peek().getLast().appendNewline();
                        line++;
                        symbol = 0;
                    } else if (c == NEWLINE) continue;
                    else stack.peek().getLast().append(c);
                } else {
                    switch (c) {
                        case ESCAPE -> escapeNext = true;
                        case LITERAL_ESCAPE -> literalEscape = !literalEscape;
                        case NEWLINE -> {
                            stack.peek().getLast().appendNewline();
                            line++;
                            symbol = 0;
                        }
                        case ARG_SEPARATOR -> {
                            if (stack.size() == 1) stack.peek().getLast().append(c);
                            else stack.peek().add(MultiLineComponent.empty());
                        }
                        case PLACEHOLDER_BEGIN -> stack.push(GTUtil.list(MultiLineComponent.empty()));
                        case PLACEHOLDER_END -> {
                            List<MultiLineComponent> placeholder = stack.pop();
                            if (stack.isEmpty()) throw new UnexpectedBracketException();
                            MultiLineComponent result = processPlaceholder(placeholder, ctx, indices);
                            if (result.isIgnoreSpaces() || stack.size() == 1) {
                                stack.peek().getLast().append(result);
                            } else {
                                for (int i = 0; i < result.size(); i++) {
                                    MutableComponent component = result.get(i);
                                    component.visit((style, string) -> {
                                        String[] split = string.split(String.valueOf(ARG_SEPARATOR));
                                        for (int j = 0; j < split.length; j++) {
                                            String idk = split[j];
                                            stack.peek().getLast()
                                                    .append(MultiLineComponent.literal(idk).withStyle(style));
                                            if (j == split.length - 1) continue;
                                            if (stack.size() == 1) {
                                                stack.peek().getLast().append(ARG_SEPARATOR);
                                            } else {
                                                stack.peek().add(MultiLineComponent.empty());
                                            }
                                        }
                                        return Optional.empty();
                                    }, component.getStyle());
                                    if (i != result.size() - 1) stack.peek().getLast().appendNewline();
                                }
                            }
                        }
                        default -> stack.peek().getLast().append(c);
                    }
                }
                escape = escapeNext;
                escapeNext = false;
                symbol++;
            }
        } catch (PlaceholderException e) {
            e.setLineInfo(line, symbol);
            exceptions.add(e);
        } catch (RuntimeException e) {
            exceptions.add(e);
        }
        if (stack.size() > 1) {
            PlaceholderException exception = new UnclosedBracketException();
            exception.setLineInfo(line, symbol);
            exceptions.add(exception);
        }
        if (exceptions.isEmpty())
            return stack.peek().stream().reduce(MultiLineComponent.empty(), MultiLineComponent::append);
        MultiLineComponent out = MultiLineComponent.literal("Exceptions:");
        out.appendNewline();
        exceptions.forEach(exception -> {
            out.append(exception.getMessage());
            out.appendNewline();
        });
        return out.withStyle(ChatFormatting.DARK_RED);
    }

    public static Set<String> getAllPlaceholderNames() {
        return placeholders.keySet();
    }

    public static Widget getPlaceholderHandlerUI(String filter) {
        DraggableScrollableWidgetGroup placeholderReference = new DraggableScrollableWidgetGroup(280, 15, 100, 200);
        Consumer<String> onSearch = (newSearch) -> {
            placeholderReference.clearAllWidgets();
            int y = 2;
            ArrayList<String> placeholders = new ArrayList<>(getAllPlaceholderNames());
            placeholders.removeIf(s -> s == null || !s.contains(newSearch));
            placeholders.sort(String::compareTo);
            for (String placeholder : placeholders) {
                TextTextureWidget placeholderName = new TextTextureWidget(0, y, 80, 15, placeholder);
                placeholderName.getTextTexture().type = TextTexture.TextType.LEFT;
                placeholderName.setHoverTooltips(GTStringUtils
                        .toImmutable(LangHandler.getSingleOrMultiLang("gtceu.placeholder_info." + placeholder)));
                placeholderReference.addWidget(placeholderName);
                y += 15;
            }
        };
        onSearch.accept(filter);
        TextTextureWidget placeholderReferenceLabel = new TextTextureWidget(
                280, 0,
                160, 15,
                GTStringUtils.componentsToString(
                        LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.placeholder_reference")));
        placeholderReferenceLabel.getTextTexture().type = TextTexture.TextType.LEFT;
        WidgetGroup out = new WidgetGroup();
        out.addWidget(placeholderReferenceLabel);
        out.addWidget(placeholderReference);
        return out;
    }
}
