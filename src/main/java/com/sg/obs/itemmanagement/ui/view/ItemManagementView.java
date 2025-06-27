package com.sg.obs.itemmanagement.ui.view;


import com.sg.obs.base.ui.component.ConfirmDeleteDialog;
import com.sg.obs.base.ui.component.PaginatedGrid;
import com.sg.obs.base.utils.DatetimeUtils;
import com.sg.obs.itemmanagement.domain.ItemDto;
import com.sg.obs.itemmanagement.service.ItemService;
import com.sg.obs.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.security.RolesAllowed;

import java.util.Objects;

@Route("items")
@PageTitle("Item Management")
@Menu(title = "Items Management", icon = "vaadin:clipboard-check", order = 0)
@RolesAllowed({AppRoles.ADMIN})
public class ItemManagementView extends VerticalLayout {

    public static final String PRICE_FIELD_LABEL = "Price";
    private final transient ItemService itemService;

    private final TextField nameField = new TextField("Name");
    private final NumberField priceField = new NumberField(PRICE_FIELD_LABEL);

    private PaginatedGrid<ItemDto> paginatedGrid;

    public ItemManagementView(ItemService itemService) {
        this.itemService = itemService;

        nameField.setPlaceholder("Item name");
        priceField.setPlaceholder(PRICE_FIELD_LABEL);

        TextField filterField = new TextField("Filter by Name or ID");
        filterField.setPlaceholder("Filter by Name or ID...");
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(e -> paginatedGrid.setFilter(e.getValue()));

        Button addButton = new Button("Add Item");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addItem());

        HorizontalLayout formLayout = new HorizontalLayout(nameField, priceField, addButton);
        formLayout.setAlignItems(Alignment.END);

        paginatedGrid = new PaginatedGrid<>(
                ItemDto.class,
                (pageable, filter) -> Objects.requireNonNull(itemService.getItems(pageable, filter).block())
        );

        paginatedGrid.addColumn(ItemDto::getId, "ID").setSortable(true);
        paginatedGrid.addColumn(ItemDto::getName, "Name").setSortable(true);
        paginatedGrid.addColumn(ItemDto::getPrice, PRICE_FIELD_LABEL).setSortable(true);
        paginatedGrid.addColumn(item -> DatetimeUtils.formatDateShort(item.getCreatedDate()), "Created").setSortable(true).setAutoWidth(true);
        paginatedGrid.addColumn(item -> DatetimeUtils.formatDateShort(item.getUpdatedDate()), "Updated").setSortable(true).setAutoWidth(true);
        paginatedGrid.addColumn(ItemDto::getStock, "Stock").setSortable(true);

        paginatedGrid.addActionColumn(this::openEditDialog, this::confirmDelete);

        add(filterField, formLayout, paginatedGrid);
        setSizeFull();

        UI.getCurrent().access(() -> paginatedGrid.loadPage(0));
    }

    private void addItem() {
        var name = nameField.getValue();
        var price = priceField.getValue();

        if (StringUtils.isBlank(name) || price == null || price < 0) {
            Notification.show("Please fill all fields correctly", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            itemService.createItem(name, price.intValue()).block();
            Notification.show("Item added!", 3000, Notification.Position.TOP_CENTER);
            nameField.clear();
            priceField.clear();
            paginatedGrid.loadPage(0);
        } catch (Exception e) {
            Notification.show("Failed to add item", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openEditDialog(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Item");

        TextField updatedName = new TextField("Name");
        updatedName.setValue(item.getName());

        NumberField updatedPrice = new NumberField(PRICE_FIELD_LABEL);
        updatedPrice.setValue(item.getPrice());

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> {
            save.setEnabled(false);
            try {
                itemService.updateItem(item.getId(), updatedName.getValue(), updatedPrice.getValue().intValue()).block();
                Notification.show("Item updated", 3000, Notification.Position.TOP_CENTER);
                dialog.close();
                paginatedGrid.loadPage(0);
            } catch (Exception e) {
                Notification.show("Failed to update item", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                save.setEnabled(true);
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        VerticalLayout layout = new VerticalLayout(updatedName, updatedPrice, buttons);
        layout.setPadding(false);
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.open();
    }

    private void confirmDelete(ItemDto item) {
        new ConfirmDeleteDialog<ItemDto>(() -> paginatedGrid.loadPage(0))
                .show(item, ItemDto::getName, it -> itemService.deleteItem(it.getId()).block());

    }

}
