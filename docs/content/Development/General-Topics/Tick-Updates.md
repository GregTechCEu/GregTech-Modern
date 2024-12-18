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

Lets look at how we implement it in `QuantumChest`.


??? example "Implementation in `QuantumChest`"

    ```java
    @Getter @Persisted @DescSynced
    protected boolean autoOutputItems;
    @Persisted @DropSaved
    protected final NotifiableItemStackHandler cache; // inner inventory
    protected TickableSubscription autoOutputSubs;
    protected ISubscription exportItemSubs;
    
    // update subscription, subscribe if tick logic subscription is required, unsubscribe otherwise.
    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems(); // get output facing
        if ((isAutoOutputItems() && !cache.isEmpty()) // inner item non empty
                && outputFacing != null // has output facing
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(outputFacing), outputFacing.getOpposite()) != null) { // adjacent block has inventory.
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::checkAutoOutput); // subscribe tick logic
        } else if (autoOutputSubs != null) { // unsubscribe tick logic
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }
    
    // output to nearby block.
    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                cache.exportToNearby(getOutputFacingItems());
            }
            updateAutoOutputSubscription(); // dont foget to check if it's still available
        }
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            // you cant call ItemTransferHelper.getItemTransfer while chunk is loading, so lets defer it next tick.
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        // add a listener to listen the changes of inner inventory. (for ex, if inventory not empty anymore, we may need to unpdate logic)
        exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
    }
    
    @Override
    public void onUnload() {
        super.onUnload(); //autoOutputSubs will be released automatically when machine unload
        if (exportItemSubs != null) {  //we should mannually release it.
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }
    
    // For any change may affect the logic to invoke updateAutoOutputSubscription at a time
    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }
    
    @Override
    public void setOutputFacingItems(Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }
    
    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
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
        @Persisted @Getter
        private boolean workingEnabled = true;
        
        private final ConditionalSubscriptionHandler subscriptionHandler;
        
        public MyMachine() {
            super(/* ... */);
    
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