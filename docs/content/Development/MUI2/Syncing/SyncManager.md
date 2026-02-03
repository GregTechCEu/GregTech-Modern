# SyncManager
A `PanelSyncManager` is the panel-level coordinator that keeps server state and client UI in sync.  

You register `SyncValue`s and other `SyncHandler`s on it during `buildUI`. Then, these are checked every server tick, and any changes are shipped to the other side.  

This is what makes widgets reflect live data and makes client-side interactions safely update the server.
