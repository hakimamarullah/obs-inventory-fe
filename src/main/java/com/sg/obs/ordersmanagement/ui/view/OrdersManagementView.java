package com.sg.obs.ordersmanagement.ui.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("orders")
@PageTitle("Orders Management")
@Menu(title = "Orders Management", icon = "vaadin:package", order = 2)
@PermitAll
public class OrdersManagementView {
}
