package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.MutableComponent;

final class TagFilterUI {

    private TagFilterUI() {}

    static Object openConfigurator(TagFilter<?, ?> filter, int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        group.addWidget(new ImageWidget(0, 0, 20, 20, GuiTextures.INFO_ICON)
                .setHoverTooltips(
                        LangHandler.getMultiLang("cover.tag_filter.info").toArray(new MutableComponent[0])));
        group.addWidget(new TextFieldWidget(0, 29, 18 * 3 + 25, 12, () -> filter.tagFilterExpression,
                filter::setFilterExpr)
                .setMaxStringLength(64)
                .setValidator(TagFilterUI::normalizeExpression));
        return group;
    }

    private static String normalizeExpression(String input) {
        input = TagFilter.DOUBLE_WILDCARD.matcher(input).replaceAll("*");
        input = TagFilter.DOUBLE_AND.matcher(input).replaceAll("&");
        input = TagFilter.DOUBLE_OR.matcher(input).replaceAll("|");
        input = TagFilter.DOUBLE_NOT.matcher(input).replaceAll("!");
        input = TagFilter.DOUBLE_XOR.matcher(input).replaceAll("^");
        input = TagFilter.DOUBLE_SPACE.matcher(input).replaceAll(" ");
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
    }
}
