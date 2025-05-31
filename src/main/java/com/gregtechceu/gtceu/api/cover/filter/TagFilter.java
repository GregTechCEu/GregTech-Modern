package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class TagFilter<T, S extends Filter<T, S>> implements Filter<T, S> {

    private static final Pattern DOUBLE_WILDCARD = Pattern.compile("\\*{2,}");
    private static final Pattern DOUBLE_AND = Pattern.compile("&{2,}");
    private static final Pattern DOUBLE_OR = Pattern.compile("\\|{2,}");
    private static final Pattern DOUBLE_NOT = Pattern.compile("!{2,}");
    private static final Pattern DOUBLE_XOR = Pattern.compile("\\^{2,}");
    private static final Pattern DOUBLE_SPACE = Pattern.compile(" {2,}");

    @Getter
    protected String oreDictFilterExpression = "";

    protected Consumer<S> itemWriter = filter -> {};
    protected Consumer<S> onUpdated = filter -> itemWriter.accept(filter);

    protected TagExprFilter.TagExprParser.MatchExpr matchExpr = null;

    protected TagFilter() {}

    @Override
    public boolean isBlank() {
        return oreDictFilterExpression.isBlank();
    }

    public CompoundTag saveFilter() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putString("oreDict", oreDictFilterExpression);
        return tag;
    }

    public void setOreDict(String oreDict) {
        this.oreDictFilterExpression = oreDict;
        matchExpr = TagExprFilter.parseExpression(oreDictFilterExpression);
        onUpdated.accept((S) this);
    }

    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        group.addWidget(new ImageWidget(0, 0, 20, 20, GuiTextures.INFO_ICON)
                .setHoverTooltips(
                        LangHandler.getMultiLang("cover.tag_filter.info").toArray(new MutableComponent[0])));
        group.addWidget(new TextFieldWidget(0, 29, 18 * 3 + 25, 12, () -> oreDictFilterExpression, this::setOreDict)
                .setMaxStringLength(64)
                .setValidator(input -> {
                    // remove all operators that are double
                    input = DOUBLE_WILDCARD.matcher(input).replaceAll("*");
                    input = DOUBLE_AND.matcher(input).replaceAll("&");
                    input = DOUBLE_OR.matcher(input).replaceAll("|");
                    input = DOUBLE_NOT.matcher(input).replaceAll("!");
                    input = DOUBLE_XOR.matcher(input).replaceAll("^");
                    input = DOUBLE_SPACE.matcher(input).replaceAll(" ");
                    // move ( and ) so it doesn't create invalid expressions f.e. xxx (& yyy) => xxx & (yyy)
                    // append or prepend ( and ) if the amount is not equal
                    StringBuilder builder = new StringBuilder();
                    int unclosed = 0;
                    char last = ' ';
                    for (int i = 0; i < input.length(); i++) {
                        char c = input.charAt(i);
                        if (c == ' ') {
                            if (last != '(')
                                builder.append(" ");
                            continue;
                        }
                        if (c == '(')
                            unclosed++;
                        else if (c == ')') {
                            unclosed--;
                            if (last == '&' || last == '|' || last == '^') {
                                int l = builder.lastIndexOf(" " + last);
                                int l2 = builder.lastIndexOf(String.valueOf(last));
                                builder.insert(l == l2 - 1 ? l : l2, ")");
                                continue;
                            }
                            if (i > 0 && builder.charAt(builder.length() - 1) == ' ') {
                                builder.deleteCharAt(builder.length() - 1);
                            }
                        } else if ((c == '&' || c == '|' || c == '^') && last == '(') {
                            builder.deleteCharAt(builder.lastIndexOf("("));
                            builder.append(c).append(" (");
                            continue;
                        }

                        builder.append(c);
                        last = c;
                    }
                    if (unclosed > 0) {
                        builder.append(")".repeat(unclosed));
                    } else if (unclosed < 0) {
                        unclosed = -unclosed;
                        for (int i = 0; i < unclosed; i++) {
                            builder.insert(0, "(");
                        }
                    }
                    input = builder.toString();
                    input = input.replaceAll(" {2,}", " ");
                    return input;
                }));
        return group;
    }

    @Override
    public void setOnUpdated(Consumer<S> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }
}
