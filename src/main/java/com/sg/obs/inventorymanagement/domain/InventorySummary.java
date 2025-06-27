package com.sg.obs.inventorymanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventorySummary {

    private String itemId;

    private String itemName;

    private String totalTopUp;

    private String totalWithdraw;

    private String remainingStock;

    private String topUpCount;

    private String withdrawCount;
}
