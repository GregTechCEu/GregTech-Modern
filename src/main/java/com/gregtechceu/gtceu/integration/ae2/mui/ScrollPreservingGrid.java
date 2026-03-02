package com.gregtechceu.gtceu.integration.ae2.mui;

import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollData;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;

/**
 * Grid subclass that preserves scroll position across rebuilds.
 * When used inside a DynamicSyncedWidget, the old Grid is disposed and a new one created
 * each time the data changes. This class saves the scroll offset on dispose and restores
 * it after the new instance finishes layout.
 */
public class ScrollPreservingGrid extends Grid {

    private final int[] scrollHolder;
    private boolean shouldRestore = true;

    public ScrollPreservingGrid(int[] scrollHolder) {
        this.scrollHolder = scrollHolder;
    }

    @Override
    public void postResize() {
        super.postResize();
        if (shouldRestore) {
            ScrollData data = getScrollArea().getScrollY();
            if (data != null) {
                data.scrollTo(getScrollArea(), scrollHolder[0]);
            }
            shouldRestore = false;
        }
    }

    @Override
    public void dispose() {
        ScrollData data = getScrollArea().getScrollY();
        if (data != null) {
            scrollHolder[0] = data.getScroll();
        }
        super.dispose();
    }
}
