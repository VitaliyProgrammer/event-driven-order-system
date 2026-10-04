package com.orderline.common.event;

public final class Topics {

    public static final String ORDER_EVENTS = "order-events";
    public static final String INVENTORY_EVENTS = "inventory-events";
    public static final String DLT_SUFFIX = ".DLT";

    public static final String ORDER_EVENTS_DLT = ORDER_EVENTS + DLT_SUFFIX;
    public static final String INVENTORY_EVENTS_DLT = INVENTORY_EVENTS + DLT_SUFFIX;

    private Topics() {
    }
}
