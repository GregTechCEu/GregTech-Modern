package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class PagedWidget<W extends PagedWidget<W>> extends Widget<W> {

    @Getter
    private final List<IWidget> pages = new ArrayList<>();
    @Getter
    private IWidget currentPage;
    @Getter
    private int currentPageIndex = 0;

    @Nullable
    private IntConsumer onPageChange;

    @Override
    public void afterInit() {
        setPage(this.currentPageIndex);
    }

    /**
     * Set a consumer that is accepted <b>right after</b> the page is actually changed and the next page widget is
     * enabled. <br/>
     * Will also be called with {@code 0} when after this widget is initialized.
     */
    public W onPageChange(@Nullable IntConsumer onPageChange) {
        this.onPageChange = onPageChange;
        return getThis();
    }

    public void setPage(int page) {
        if (page < 0 || page >= this.pages.size()) {
            throw new IndexOutOfBoundsException("Setting page of " + this + " to " + page +
                    " failed. Only values from 0 to " + (this.pages.size() - 1) + " are allowed.");
        }
        this.currentPageIndex = page;
        if (this.currentPage != null) {
            this.currentPage.setEnabled(false);
        }
        this.currentPage = this.pages.get(this.currentPageIndex);
        this.currentPage.setEnabled(true);

        if (this.onPageChange != null) {
            this.onPageChange.accept(page);
        }
    }

    public void nextPage() {
        if (++this.currentPageIndex == this.pages.size()) {
            this.currentPageIndex = 0;
        }
        setPage(this.currentPageIndex);
    }

    public void previousPage() {
        if (--this.currentPageIndex == -1) {
            this.currentPageIndex = this.pages.size() - 1;
        }
        setPage(this.currentPageIndex);
    }

    @Override
    public @Unmodifiable @NotNull List<IWidget> getChildren() {
        return this.pages;
    }

    public W initialPage(int page) {
        if (!isValid()) {
            this.currentPageIndex = page;
        }
        return getThis();
    }

    public W addPage(IWidget widget) {
        this.pages.add(widget);
        widget.setEnabled(false);
        return getThis();
    }

    public W controller(Controller controller) {
        controller.setPagedWidget(this);
        return getThis();
    }

    public static class Controller {

        private PagedWidget<?> pagedWidget;

        public boolean isInitialised() {
            return this.pagedWidget != null && this.pagedWidget.isValid();
        }

        private void validate() {
            if (!isInitialised()) {
                throw new IllegalStateException(
                        "PagedWidget controller does not have a valid PagedWidget! Current PagedWidget: " +
                                this.pagedWidget);
            }
        }

        private void setPagedWidget(PagedWidget<?> pagedWidget) {
            this.pagedWidget = pagedWidget;
        }

        public void setPage(int page) {
            validate();
            this.pagedWidget.setPage(page);
        }

        public void nextPage() {
            validate();
            this.pagedWidget.nextPage();
        }

        public void previousPage() {
            validate();
            this.pagedWidget.previousPage();
        }

        public IWidget getActivePage() {
            validate();
            return this.pagedWidget.getCurrentPage();
        }

        public int getActivePageIndex() {
            validate();
            return this.pagedWidget.getCurrentPageIndex();
        }
    }
}
