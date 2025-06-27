package com.sg.obs.inventorymanagement.domain;

public record InventoryRequest(Long itemId, int quantity, String type) {
}
