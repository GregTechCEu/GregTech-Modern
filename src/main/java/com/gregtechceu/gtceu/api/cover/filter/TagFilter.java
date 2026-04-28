package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.utils.TagExprFilter;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class TagFilter<T, S extends Filter<T, S>> implements Filter<T, S> {

    static final Pattern DOUBLE_WILDCARD = Pattern.compile("\\*{2,}");
    static final Pattern DOUBLE_AND = Pattern.compile("&{2,}");
    static final Pattern DOUBLE_OR = Pattern.compile("\\|{2,}");
    static final Pattern DOUBLE_NOT = Pattern.compile("!{2,}");
    static final Pattern DOUBLE_XOR = Pattern.compile("\\^{2,}");
    static final Pattern DOUBLE_SPACE = Pattern.compile(" {2,}");

    @Getter
    protected String tagFilterExpression = "";

    protected Consumer<S> itemWriter = filter -> {};
    protected Consumer<S> onUpdated = filter -> itemWriter.accept(filter);

    @Nullable
    protected TagExprFilter.TagExprParser.MatchExpr matchExpr = null;

    protected TagFilter() {}

    @Override
    public boolean isBlank() {
        return tagFilterExpression.isBlank();
    }

    public void setFilterExpr(String filterExpr) {
        this.tagFilterExpression = filterExpr;
        matchExpr = TagExprFilter.parseExpression(tagFilterExpression);
        // noinspection unchecked
        onUpdated.accept((S) this);
    }

    public Object openConfigurator(int x, int y) {
        return TagFilterUI.openConfigurator(this, x, y);
    }

    @Override
    public void setOnUpdated(Consumer<S> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagFilter<?, ?> tagFilter = (TagFilter<?, ?>) o;
        return tagFilterExpression.equals(tagFilter.tagFilterExpression);
    }

    @Override
    public int hashCode() {
        return tagFilterExpression.hashCode();
    }
}
