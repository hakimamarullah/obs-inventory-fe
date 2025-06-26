package com.sg.obs.itemmanagement.ui.view;


import com.sg.obs.base.ui.component.PaginatedGrid;
import com.sg.obs.itemmanagement.domain.ItemDto;
import com.sg.obs.itemmanagement.service.ItemService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
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
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

@Route("items")
@PageTitle("Item Management")
@Menu(title = "Items Management", icon = "vaadin:clipboard-check", order = 0)
@PermitAll
public class ItemManagementView extends VerticalLayout {

    private final ItemService itemService;

    private final TextField nameField = new TextField("Name");
    private final NumberField priceField = new NumberField("Price");
    private final Button addButton = new Button("Add Item");
    private final TextField filterField = new TextField("Filter by Name or ID");

    private PaginatedGrid<ItemDto> paginatedGrid;

    public ItemManagementView(ItemService itemService) {
        this.itemService = itemService;

        nameField.setPlaceholder("Item name");
        priceField.setPlaceholder("Price");

        filterField.setPlaceholder("Filter by Name or ID...");
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(e -> paginatedGrid.setFilter(e.getValue()));

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
        paginatedGrid.addColumn(ItemDto::getPrice, "Price").setSortable(true);
        paginatedGrid.addColumn(item -> formatDate(item.getCreatedDate()), "Created").setSortable(true).setAutoWidth(true);
        paginatedGrid.addColumn(item -> formatDate(item.getUpdatedDate()), "Updated").setSortable(true).setAutoWidth(true);
        paginatedGrid.addColumn(ItemDto::getStock, "Stock").setSortable(true);

        paginatedGrid.addActionColumn(this::openEditDialog, this::confirmDelete);

        add(filterField, formLayout, paginatedGrid);
        setSizeFull();

        UI.getCurrent().access(() -> paginatedGrid.loadPage(0));
    }

    private void addItem() {
        var name = nameField.getValue();
        var price = priceField.getValue();

        if (name == null || name.isBlank() || price == null || price < 0) {
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

        NumberField updatedPrice = new NumberField("Price");
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
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete");
        dialog.add(new Paragraph("Are you sure you want to delete item: " + item.getName() + "?"));

        Button confirm = new Button("Delete", event -> {
            try {
                itemService.deleteItem(item.getId()).block();
                dialog.close();
                Notification.show("Item deleted", 3000, Notification.Position.TOP_CENTER);
                paginatedGrid.loadPage(0);
            } catch (Exception e) {
                Notification.show("Failed to delete item", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancel = new Button("Cancel", event -> dialog.close());

        dialog.add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ?
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(dateTime) :
                "-";
    }
}
