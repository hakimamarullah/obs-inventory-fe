package com.sg.obs.inventorymanagement.ui.view;


import com.sg.obs.inventorymanagement.domain.InventoryDetails;
import com.sg.obs.inventorymanagement.domain.InventoryRequest;
import com.sg.obs.inventorymanagement.domain.ItemInfo;
import com.sg.obs.inventorymanagement.service.InventoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Route("inventories")
@PageTitle("Inventory Management")
@Menu(title = "Inventory Management", icon = "vaadin:stock", order = 1)
@PermitAll
@Slf4j
public class InventoryManagementView extends VerticalLayout {

    private final ComboBox<ItemInfo> itemComboBox = new ComboBox<>("Item");
    private final NumberField quantityField = new NumberField("Quantity");
    private final Button saveInvBtn = new Button("Save");

    // Summary Section
    private final TextField totalTopUp = new TextField("Total Top Up");
    private final TextField totalWithdrawal = new TextField("Total Withdrawal");
    private final TextField totalStock = new TextField("Total Stock");
    private final TextField itemName = new TextField("Item Name");
    private final NumberField itemId = new NumberField("Item Id");
    private final ComboBox<String> invTypeSummarySection = new ComboBox<>("Inventory Type");
    private final Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create());

    // Table Section
    private final Grid<InventoryDetails> inventoryGrid = new Grid<>(InventoryDetails.class, false);

    private Long itemIdToAdd = null;
    private int quantityToAdd = 0;
    private String typeToAdd = "W";

    private final InventoryService inventoryService;

    public InventoryManagementView(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createTopSection();
        createTableSection();
        setSaveInvBtn();
        setRefreshBtn();
        refreshInventoryData();
    }

    private void createTopSection() {
        // Left side - Record Inventory
        VerticalLayout recordSection = createRecordSection();
        recordSection.setWidth("400px");

        // Right side - Summary Section
        VerticalLayout summarySection = createSummarySection();
        summarySection.setWidth("500px");

        // Top horizontal layout
        HorizontalLayout topLayout = new HorizontalLayout(recordSection, summarySection);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(JustifyContentMode.START);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        add(topLayout);
    }

    private VerticalLayout createRecordSection() {
        VerticalLayout recordSection = new VerticalLayout();
        recordSection.setPadding(true);
        recordSection.setSpacing(true);
        recordSection.addClassName("record-section");
        recordSection.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", "var(--lumo-base-color)");

        // Title
        Span title = new Span("Record Inventory");
        title.addClassNames(LumoUtility.TextColor.HEADER, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);

        // Item ComboBox
        itemComboBox.setItemsPageable((inventoryService::getItemsInfo));
        itemComboBox.setItemLabelGenerator(it -> StringUtils.capitalize(it.getId() + "-" + it.getName()));
        itemComboBox.setAutoOpen(true);
        itemComboBox.setWidthFull();
        itemComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                itemIdToAdd = e.getValue().getId();
            }
        });

        // Quantity Field
        quantityField.setMin(1);
        quantityField.setWidthFull();
        quantityField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                quantityToAdd = e.getValue().intValue();
            }
        });

        // Inventory Type
        ComboBox<String> invType = new ComboBox<>("Inventory Type");
        invType.setItems(List.of("T", "W"));
        invType.setAllowCustomValue(false);
        invType.setValue("W");
        invType.setWidthFull();
        invType.addValueChangeListener(e -> typeToAdd = e.getValue());

        Span invTypeDesc = new Span("T: Top-Up; W: Withdrawal");
        invTypeDesc.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        saveInvBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveInvBtn.setWidthFull();

        recordSection.add(title, itemComboBox, quantityField, invType, invTypeDesc, saveInvBtn);
        return recordSection;
    }

    private VerticalLayout createSummarySection() {
        VerticalLayout summarySection = new VerticalLayout();
        summarySection.setPadding(true);
        summarySection.setSpacing(true);
        summarySection.addClassName("summary-section");
        summarySection.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", "var(--lumo-base-color)");

        // Title
        Span title = new Span("Summary");
        title.getElement().getStyle().set("font-weight", "bold");

        // Setup fields
        itemId.setWidthFull();
        itemId.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                refreshInventoryData();
            }
        });

        itemName.setWidthFull();
        itemName.setReadOnly(true);
        itemName.setEnabled(false);

        totalTopUp.setWidthFull();
        totalTopUp.setReadOnly(true);
        totalTopUp.setEnabled(false);

        totalWithdrawal.setWidthFull();
        totalWithdrawal.setReadOnly(true);
        totalWithdrawal.setEnabled(false);

        totalStock.setWidthFull();
        totalStock.setReadOnly(true);
        totalStock.setEnabled(false);

        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.setWidthFull();

        // Left: ID + Name
        VerticalLayout left = new VerticalLayout(itemId, itemName);
        left.setPadding(false);
        left.setSpacing(false);
        left.setWidth("50%");

        // Right: the rest
        VerticalLayout right = new VerticalLayout(totalTopUp, totalWithdrawal, totalStock, refreshBtn);
        right.setPadding(false);
        right.setSpacing(false);
        right.setWidth("50%");

        HorizontalLayout row = new HorizontalLayout(left, right);
        row.setWidthFull();
        row.setSpacing(true);
        row.setPadding(true);

        summarySection.add(title, row);
        return summarySection;
    }


    private void createTableSection() {
        VerticalLayout tableSection = new VerticalLayout();
        tableSection.setSizeFull();
        tableSection.setPadding(true);
        tableSection.setSpacing(true);

        // Title
        Span tableTitle = new Span("Inventory Details");
        tableTitle.addClassNames(LumoUtility.TextColor.HEADER, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);

        // Configure Grid
        inventoryGrid.setSizeFull();
        inventoryGrid.addColumn(InventoryDetails::getId).setHeader("ID").setAutoWidth(true);
        inventoryGrid.addColumn(InventoryDetails::getItemId).setHeader("Item ID").setAutoWidth(true);

        // Make item name clickable
        inventoryGrid.addColumn(new ComponentRenderer<>(inventoryDetails -> {
            Button itemNameBtn = new Button(inventoryDetails.getItemName());
            itemNameBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            itemNameBtn.addClickListener(e -> {
                itemId.setValue(Double.valueOf(inventoryDetails.getItemId()));
                refreshInventoryData();
            });
            return itemNameBtn;
        })).setHeader("Item Name").setAutoWidth(true);

        inventoryGrid.addColumn(InventoryDetails::getQuantity).setHeader("Quantity").setAutoWidth(true);
        inventoryGrid.addColumn(InventoryDetails::getType).setHeader("Type").setAutoWidth(true);
        inventoryGrid.addColumn(InventoryDetails::getCreatedDate).setHeader("Created Date").setAutoWidth(true);
        inventoryGrid.addColumn(InventoryDetails::getUpdatedDate).setHeader("Updated Date").setAutoWidth(true);

        // Add pagination (you'll need to implement this based on your InventoryService)
        // PaginatedGrid or custom pagination component can be used here

        tableSection.add(tableTitle, inventoryGrid);
        add(tableSection);
    }

    private void setSaveInvBtn() {
        saveInvBtn.addClickListener(e -> {
            if (itemIdToAdd != null && quantityToAdd > 0) {
                log.info("Saving inventory for item: {}, quantity: {}, Type: {}", itemIdToAdd, quantityToAdd, typeToAdd);
                InventoryRequest body = new InventoryRequest(itemIdToAdd, quantityToAdd, typeToAdd);
                inventoryService.saveInventory(body);

                Notification.show("Inventory saved", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Clear form
                itemComboBox.clear();
                quantityField.clear();

                // Refresh data
                refreshInventoryData();
            } else {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void setRefreshBtn() {
        refreshBtn.addClickListener(e -> refreshInventoryData());
    }

    private void refreshInventoryData() {
        try {
//            Long selectedItemId = itemId.getValue() != null ? itemId.getValue().longValue() : null;
//            String selectedType = invTypeSummarySection.getValue();
//
//            // Fetch inventory data based on filters
//            List<InventoryDetails> inventoryData;
//            if (selectedItemId != null) {
////                inventoryData = inventoryService.getInventoryByItemId(selectedItemId);
//                inventoryData = new ArrayList<>();
//
//                // Update item name if item ID is set
//                ItemInfo itemInfo = inventoryService.getItemInfo(selectedItemId);
//                if (itemInfo != null) {
//                    itemName.setValue(itemInfo.getName());
//                }
//            } else {
//                inventoryData = inventoryService.getAllInventory();
//                itemName.clear();
//            }
//
//            // Filter by type if not "All"
//            if (!"All".equals(selectedType)) {
//                inventoryData = inventoryData.stream()
//                        .filter(inv -> selectedType.equals(inv.getType()))
//                        .collect(Collectors.toList());
//            }

            // Update grid
            inventoryGrid.setItems(new ArrayList<>());

            // Calculate and update summary
            updateSummary(new ArrayList<>());

        } catch (Exception ex) {
            log.error("Error refreshing inventory data", ex);
            Notification.show("Error loading data: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateSummary(List<InventoryDetails> inventoryData) {
        int topUpTotal = inventoryData.stream()
                .filter(inv -> "T".equals(inv.getType()))
                .mapToInt(InventoryDetails::getQuantity)
                .sum();

        int withdrawalTotal = inventoryData.stream()
                .filter(inv -> "W".equals(inv.getType()))
                .mapToInt(InventoryDetails::getQuantity)
                .sum();

        int stockTotal = topUpTotal - withdrawalTotal;

        totalTopUp.setValue(String.valueOf(topUpTotal));
        totalWithdrawal.setValue(String.valueOf(withdrawalTotal));
        totalStock.setValue(String.valueOf(stockTotal));
    }
}
