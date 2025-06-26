package com.sg.obs.itemmanagement.ui.view;


import com.sg.obs.base.ui.component.Pagination;
import com.sg.obs.itemmanagement.domain.ItemDto;
import com.sg.obs.itemmanagement.service.ItemService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private String currentFilter = "";

    private final Grid<ItemDto> grid = new Grid<>(ItemDto.class, false);
    private final ListDataProvider<ItemDto> dataProvider = new ListDataProvider<>(Collections.emptyList());
    private final Pagination pagination;
    private List<ItemDto> allItems = new ArrayList<>(); // Store all items for filtering


    public ItemManagementView(ItemService itemService) {
        this.itemService = itemService;

        nameField.setPlaceholder("Item name");
        priceField.setPlaceholder("Price");

        filterField.setPlaceholder("Filter by Name or ID...");
        filterField.addValueChangeListener(e -> {
            currentFilter = e.getValue();
            applyFilter();
        });
        filterField.setValueChangeMode(ValueChangeMode.EAGER);

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addItem());

        HorizontalLayout formLayout = new HorizontalLayout(nameField, priceField, addButton);
        formLayout.setAlignItems(Alignment.END);

        setupGrid();

        pagination = new Pagination(this::loadPage);

        add(filterField, formLayout, grid, pagination);
        setSizeFull();

        UI.getCurrent().access(() -> loadPage(0, pagination.getCurrentPageSize()));
    }

    private void setupGrid() {
        grid.setDataProvider(dataProvider);
        grid.addColumn(ItemDto::getId).setHeader("ID").setAutoWidth(true).setSortable(true).setKey("id");
        grid.addColumn(ItemDto::getName).setHeader("Name").setAutoWidth(true).setSortable(true).setKey("name");
        grid.addColumn(ItemDto::getPrice).setHeader("Price").setAutoWidth(true).setSortable(true).setKey("price");
        grid.addColumn(item -> formatDate(item.getCreatedDate()))
                .setHeader("Created").setSortable(true).setKey("createdDate");
        grid.addColumn(item -> formatDate(item.getUpdatedDate()))
                .setHeader("Updated").setSortable(true).setKey("updatedDate");
        grid.addColumn(ItemDto::getStock).setHeader("Stock").setAutoWidth(true).setSortable(true).setKey("stock");
        grid.addComponentColumn(this::buildActionButtons).setHeader("Actions").setAutoWidth(true);
        grid.setSizeFull();
    }

    private void loadPage(int pageIndex, int size) {
        var pageable = PageRequest.of(pageIndex, pagination.getCurrentPageSize(), grid.getSortOrder().isEmpty() ?
                Sort.unsorted() :
                Sort.by(grid.getSortOrder().stream().map(order ->
                        order.getDirection() == SortDirection.ASCENDING ?
                                Sort.Order.asc(order.getSorted().getKey()) :
                                Sort.Order.desc(order.getSorted().getKey())
                ).toList()));

        try {
            var page = itemService.getItems(pageable, "").block(); // Load all items, no backend filter
            allItems = new ArrayList<>(page.getContent()); // Store all items
            applyFilter(); // Apply client-side filter
            pagination.setTotalPages(page.getTotalPages());
            pagination.setCurrentPage(pageIndex);
        } catch (Exception e) {
            Notification.show("Failed to load items", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void applyFilter() {
        if (currentFilter == null || currentFilter.isBlank()) {
            // No filter, show all items
            grid.setItems(new ListDataProvider<>(allItems));
        } else {
            // Filter by name or ID
            List<ItemDto> filteredItems = allItems.stream()
                    .filter(item -> {
                        String filter = currentFilter.toLowerCase().trim();
                        // Check if filter matches name (case insensitive)
                        boolean nameMatch = item.getName() != null &&
                                item.getName().toLowerCase().contains(filter);
                        // Check if filter matches ID (convert ID to string)
                        boolean idMatch = String.valueOf(item.getId()).contains(filter);
                        return nameMatch || idMatch;
                    })
                    .toList();
            grid.setItems(new ListDataProvider<>(filteredItems));
        }
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
            loadPage(0, pagination.getCurrentPageSize()); // Reload to get updated list including new item
        } catch (Exception e) {
            Notification.show("Failed to add item", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Component buildActionButtons(ItemDto item) {
        Button edit = new Button("Edit", VaadinIcon.EDIT.create());
        Button delete = new Button("Delete", VaadinIcon.TRASH.create());

        edit.addClickListener(e -> openEditDialog(item));
        delete.addClickListener(e -> confirmDelete(item));

        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        return new HorizontalLayout(edit, delete);
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
                Notification.show("Item updated", 3000, Notification.Position.TOP_STRETCH);
                dialog.close();
                loadPage(0, pagination.getCurrentPageSize());
            } catch (Exception e) {
                Notification.show("Failed to update item", 3000, Notification.Position.TOP_STRETCH)
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
                Notification.show("Item deleted", 3000, Notification.Position.TOP_STRETCH);
                loadPage(0, pagination.getCurrentPageSize());
            } catch (Exception e) {
                Notification.show("Failed to delete item", 3000, Notification.Position.TOP_STRETCH)
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