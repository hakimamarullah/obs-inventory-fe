package com.sg.obs.inventorymanagement.ui.view;


import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("inventories")
@PageTitle("Inventory Management")
@Menu(title = "Inventory Management", icon = "vaadin:stock", order = 1)
@PermitAll
public class InventoryManagementView {
}
