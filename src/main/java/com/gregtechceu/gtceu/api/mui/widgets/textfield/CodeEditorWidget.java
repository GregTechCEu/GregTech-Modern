package com.gregtechceu.gtceu.api.mui.widgets.textfield;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Accessors(chain = true, fluent = true)
public class CodeEditorWidget<W extends CodeEditorWidget<W>> extends TextEditorWidget<W> {

    @Setter
    @Getter
    @Nullable
    protected LanguageDefinition language;

    @Override
    public List<Component> getTextAsComponents() {
        if (language() == null) return super.getTextAsComponents();
        return language().formatCode(this.handler.getTextAsString());
    }

    public record LanguageDefinition(Pattern tokenPattern, Supplier<Function<String, Component>> tokenFormatter) {

        private static String makeSeparator(List<String> separators) {
            if (separators.isEmpty()) return "\\X+";
            else {
                return separators.stream().reduce("", (s1, s2) -> s1 + '|' + s2).substring(1);
            }
        }

        public LanguageDefinition(List<String> separators, Supplier<Function<String, Component>> tokenFormatter) {
            this(Pattern.compile(makeSeparator(separators)), tokenFormatter);
        }

        public List<String> splitIntoTokens(String s) {
            Matcher matcher = tokenPattern.matcher(s);
            List<String> tokens = new ArrayList<>();
            List<String> betweenTokens = List.of(tokenPattern.split(s, -1));
            int i = 0;
            int skipped = 0;
            while (matcher.find()) {
                if (i != 0 || matcher.start() != 0) {
                    tokens.add(betweenTokens.get(i - skipped));
                } else skipped++;
                tokens.add(matcher.group());
                i++;
            }
            if (i != betweenTokens.size()) tokens.add(betweenTokens.get(i));
            tokens.add("\0");
            return tokens;
        }

        public List<Component> formatCode(String code) {
            List<MutableComponent> output = new ArrayList<>();
            splitIntoTokens(code).stream()
                    .map(tokenFormatter.get())
                    .map(Component::copy)
                    .reduce(Component.empty(), MutableComponent::append)
                    .visit((style, s) -> {
                        List<MutableComponent> lines = Arrays.stream(s.split("\\n", -1))
                                .map(line -> Component.literal(line).withStyle(style))
                                .toList();
                        if (lines.isEmpty()) return Optional.empty();
                        if (!output.isEmpty()) {
                            GTUtil.getLast(output).append(lines.get(0));
                            for (int i = 1; i < lines.size(); i++) output.add(lines.get(i));
                        } else output.addAll(lines);
                        return Optional.empty();
                    }, Style.EMPTY);
            return output.stream()
                    .map(c -> (Component) c.withStyle(style -> style.withFont(GTGuiTextures.MONOCRAFT_FONT))).toList();
        }
    }
}
