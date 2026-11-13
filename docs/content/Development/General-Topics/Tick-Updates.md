---
title: Tick Updates
---


# How to use `ITickable` / `update()`

The client update is always present and you can override the `clientTick()` method, which works just as well as in 1.12.

But for the sake of performance, our machines are no longer always in a tickable state. 
We introduced `ITickSubscription` for managed tick logic.  
Understand the basic concept of subscribing to periodic updates when they are needed, and unsubscribe them when 
they are not.


## Example implementation

Automatic output of our machine requires periodic output of internal items to an adjacent inventory.
But most of the time this logic doesn't need to be executed if any of the following conditions apply:

- there is no item inside the machine
- the automatic output is not set to active
- there is no adjacent block that can accept the item.

Lets look at how we implement it in `AutoOutputTrait` (used by e.g. the `QuantumChest`).


??? example "Implementation in `AutoOutputTrait`"

    ```java
    @Getter @SaveField @SyncToClient
    protected boolean autoOutputItems = false;
    protected final List<IItemHandler> itemHandlers; // inner inventories
    protected @Nullable TickableSubscription itemOutputSub;
    protected List<ISubscription> itemSubs = new ArrayList<>();
    
    // update subscription, subscribe if tick logic subscription is required, unsubscribe otherwise.
    protected void updateItemOutputSubscription() {
        if (isAutoOutputItems() && getItemOutputDirection() != null // has output facing
                && GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), getItemOutputDirection())) { // adjacent block has inventory.
            itemOutputSub = subscribeServerTick(itemOutputSub, this::autoOutputItems); // subscribe tick logic
        } else if (itemOutputSub != null) { // unsubscribe tick logic
            itemOutputSub.unsubscribe();
            itemOutputSub = null;
        }
    }
    
    // output to nearby block.
    protected void autoOutputItems() {
        if (getMachine().getOffsetTimer() % 5 == 0 && getItemOutputDirection() != null) {
            itemHandlers.forEach(this::exportItemToNearby);
        }
        updateItemOutputSubscription(); // dont foget to check if it's still available
    }
    
    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        
        // you cant check for adjacent inventories while chunk is loading, so lets defer it next tick.
        getMachine().scheduleForNextServerTick(this::updateItemOutputSubscription);
        // add a listener to listen the changes of inner inventory. (for ex, if inventory not empty anymore, we may need to unpdate logic)
        for (var handler : itemHandlers) {
            if (handler instanceof NotifiableItemStackHandler notifiable)
                itemSubs.add(notifiable.addChangedListener(this::updateItemOutputSubscription));
        }
    }
    
    @Override
    public void onMachineUnload() {
        if (itemOutputSub != null) {
            itemOutputSub.unsubscribe();
            itemOutputSub = null;
        }
        itemSubs.forEach(ISubscription::unsubscribe); // we should mannually release it.
        itemSubs.clear();
        super.onMachineUnload();
    }
    
    // For any change may affect the logic to invoke updateItemOutputSubscription at a time
    public void setAllowAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
        updateItemOutputSubscription();
    }
    
    public void setItemOutputDirection(@Nullable Direction outputFacing) {
        this.itemOutputDirection = outputFacing;
        syncDataHolder.markClientSyncFieldDirty("itemOutputDirection");
        updateItemOutputSubscription();
    }
    
    @Override
    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        updateItemOutputSubscription();
    }
    ```

I know the code is a kinda long, but it's for performance, and thanks to the SyncData system, we've eliminated a lot of
synchronization code, so please sacrifice a little for better performance.


## Using the `ConditionalSubscriptionHandler`

For ease of use in some situations, it is possible to eliminate some of the boilerplate code and delegate management of
your subscription to a `ConditionalSubscriptionHandler` instead.

Using that class, it is possible to simply provide an update method to run every tick while the subscription is active,
as well as a `Supplier<Boolean>` that determines whether it is active.

Whenever the input of its condition changes, you need to call `updateSubscription()` on the handler, so that it can
re-evaluate it and take the necessary steps if it has changed.  
You should also to call this method after executing your tick logic in most cases, to ensure the subscription doesn't
stay active any longer than it needs to.
 
??? example "Example using `ConditionalSubscriptionHandler`"

    ```java
    class MyMachine extends MetaMachine implements IControllable {
        @SaveField @Getter
        private boolean workingEnabled = true;
        
        private final ConditionalSubscriptionHandler subscriptionHandler;
        
        public MyMachine(BlockEntityCreationInfo info) {
            super(info);
    
            this.subscriptionHandler = new ConditionalSubscriptionHandler(
                this, this::update, this::isSubscriptionActive
            );
        }
        
        private void update() {
            // Only run once every second
            if (getOffsetTimer() % 20 != 0)
                return;
    
            // ...
    
            // Now that the update logic has been executed, update the subscription.
            // This will internally check if the subscription is still active and
            // unsubscribe otherwise.
            subscriptionHandler.updateSubscription();
        }
    
        private boolean isSubscriptionActive() {
            return isWorkingEnabled();
        }
    
        @Override
        public void setWorkingEnabled(boolean workingEnabled) {
            this.workingEnabled = workingEnabled;
            
            // Whether the subscription is currently active depends on whether working
            // is enabled for this machine. As soon as any of the condition inputs changes,
            // you need to update the subscription.
            subscriptionHandler.updateSubscription();
        }
    
        @Override
        public void onLoad() {
            super.onLoad();
            
            // As soon as you can get a reference to the dimension/level you're in,
            // you need to initialize your subscription handler.
            subscriptionHandler.initialize(getLevel());
        }
    }
    ```