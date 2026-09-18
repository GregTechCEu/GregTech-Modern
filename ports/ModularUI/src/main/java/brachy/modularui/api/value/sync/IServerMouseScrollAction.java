package brachy.modularui.api.value.sync;

import brachy.modularui.utils.MouseData;

public interface IServerMouseScrollAction {

    void onServerMouseAction(MouseData mouseData, double scrollX, double scrollY);
}
