package name.lapidary.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class CanisterFluidStorage {

    public static final long BUCKET =
            FluidConstants.BUCKET;

    public static final long CAPACITY =
            8L * BUCKET;

    private final Runnable changeCallback;

    private CanisterLiquid liquid;
    private long amount;

    public CanisterFluidStorage(
            Runnable changeCallback
    ) {
        this.changeCallback =
                changeCallback;
    }

    public CanisterLiquid getLiquid() {
        return liquid;
    }

    public long getAmount() {
        return amount;
    }

    public long getCapacity() {
        return CAPACITY;
    }

    public long getRemainingCapacity() {
        return CAPACITY - amount;
    }

    public boolean isEmpty() {
        return amount <= 0L
                || liquid == null;
    }

    public boolean isFull() {
        return amount >= CAPACITY;
    }

    public boolean contains(
            CanisterLiquid testedLiquid
    ) {
        return !isEmpty()
                && liquid == testedLiquid;
    }

    /**
     * Attempts to insert up to maxAmount.
     *
     * @param simulate when true, reports what could be inserted
     *                 without modifying the storage
     *
     * @return the amount that was or could be inserted
     */
    public long insert(
            CanisterLiquid incomingLiquid,
            long maxAmount,
            boolean simulate
    ) {
        if (incomingLiquid == null
                || maxAmount <= 0L) {

            return 0L;
        }

        /*
         * An occupied canister may only accept its existing liquid.
         */
        if (!isEmpty()
                && liquid != incomingLiquid) {

            return 0L;
        }

        long inserted =
                Math.min(
                        maxAmount,
                        getRemainingCapacity()
                );

        if (inserted <= 0L) {
            return 0L;
        }

        if (!simulate) {
            if (isEmpty()) {
                liquid =
                        incomingLiquid;
            }

            amount += inserted;

            normalize();
            changeCallback.run();
        }

        return inserted;
    }

    /**
     * Attempts to extract a particular liquid.
     */
    public long extract(
            CanisterLiquid requestedLiquid,
            long maxAmount,
            boolean simulate
    ) {
        if (requestedLiquid == null
                || requestedLiquid != liquid
                || maxAmount <= 0L
                || isEmpty()) {

            return 0L;
        }

        long extracted =
                Math.min(
                        maxAmount,
                        amount
                );

        if (!simulate) {
            amount -= extracted;

            normalize();
            changeCallback.run();
        }

        return extracted;
    }

    /**
     * Extracts whichever liquid is currently present.
     *
     * This is useful for an empty bucket, backpack, or machine that
     * does not already know which liquid the canister contains.
     */
    public Transfer extractAny(
            long maxAmount,
            boolean simulate
    ) {
        if (maxAmount <= 0L
                || isEmpty()) {

            return Transfer.EMPTY;
        }

        CanisterLiquid extractedLiquid =
                liquid;

        long extractedAmount =
                Math.min(
                        maxAmount,
                        amount
                );

        if (!simulate) {
            amount -= extractedAmount;

            normalize();
            changeCallback.run();
        }

        return new Transfer(
                extractedLiquid,
                extractedAmount
        );
    }

    /**
     * Used only when reading saved block-entity data.
     *
     * It intentionally does not invoke the change callback.
     */
    public void loadContents(
            CanisterLiquid loadedLiquid,
            long loadedAmount
    ) {
        if (loadedLiquid == null
                || loadedAmount <= 0L) {

            liquid = null;
            amount = 0L;
            return;
        }

        liquid =
                loadedLiquid;

        amount =
                Math.min(
                        loadedAmount,
                        CAPACITY
                );

        normalize();
    }

    public void clear() {
        if (isEmpty()) {
            return;
        }

        liquid = null;
        amount = 0L;

        changeCallback.run();
    }

    private void normalize() {
        amount =
                Math.max(
                        0L,
                        Math.min(
                                amount,
                                CAPACITY
                        )
                );

        if (amount == 0L) {
            liquid = null;
        }
    }

    public record Transfer(
            CanisterLiquid liquid,
            long amount
    ) {

        public static final Transfer EMPTY =
                new Transfer(
                        null,
                        0L
                );

        public boolean isEmpty() {
            return liquid == null
                    || amount <= 0L;
        }
    }
}