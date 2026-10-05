package com.orderline.common.event;

public final class EventTypes {

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_PAID = "OrderPaid";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String ORDER_SHIPPED = "OrderShipped";
    public static final String ORDER_DELIVERED = "OrderDelivered";

    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_REJECTED = "InventoryRejected";

    private EventTypes() {
    }
}
