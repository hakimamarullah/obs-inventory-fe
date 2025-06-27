package com.sg.obs.inventorymanagement.ui.view;


import com.sg.obs.base.ui.component.PaginatedGrid;
import com.sg.obs.base.utils.DatetimeUtils;
import com.sg.obs.inventorymanagement.domain.InventoryDetails;
import com.sg.obs.inventorymanagement.domain.InventoryRequest;
import com.sg.obs.inventorymanagement.domain.InventorySummary;
import com.sg.obs.inventorymanagement.service.InventoryService;
import com.sg.obs.itemmanagement.domain.ItemDto;
import com.sg.obs.itemmanagement.service.ItemService;
import com.sg.obs.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Route("inventories")
@PageTitle("Inventory Management")
@Menu(title = "Inventory Management", icon = "vaadin:stock", order = 1)
@RolesAllowed({AppRoles.ADMIN})
@Slf4j
public class InventoryManagementView extends VerticalLayout {

    private final ComboBox<ItemDto> itemComboBox = new ComboBox<>("Item");
    private final NumberField quantityField = new NumberField("Quantity");
    private final Button saveInvBtn = new Button("Save");

    private final TextField totalTopUp = new TextField("Total Top-Up");
    private final TextField totalWithdrawal = new TextField("Total Withdrawal");
    private final TextField totalStock = new TextField("Remaining Stock");
    private final TextField topUpCount = new TextField("Top-Up Count");
    private final TextField withdrawalCount = new TextField("Withdrawal Count");
    private final TextField itemName = new TextField("Item Name");
    private final TextField itemId = new TextField("Item ID");

    private final PaginatedGrid<InventoryDetails> paginatedGrid;

    private Long itemIdToAdd = null;
    private int quantityToAdd = 0;
    private String typeToAdd = "W";

    private final transient InventoryService inventoryService;
    private final transient ItemService itemService;

    public InventoryManagementView(InventoryService inventoryService, ItemService itemService) {
        this.itemService = itemService;
        this.inventoryService = inventoryService;
        this.paginatedGrid = new PaginatedGrid<>(
                InventoryDetails.class,
                (pageable, filter) -> Objects.requireNonNull(inventoryService.getAllInventory(pageable, filter).block())
        );

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createTopSection();
        createTableSection();
        setSaveInvBtn();

        paginatedGrid.addActionColumn(it -> Notification.show("Edit Inventory").setPosition(Notification.Position.TOP_CENTER), it -> Notification.show("Delete Inventory").setPosition(Notification.Position.TOP_CENTER));
        UI.getCurrent().access(() -> paginatedGrid.loadPage(0));
    }

    private void createTopSection() {
        VerticalLayout recordSection = createRecordSection();
        recordSection.setWidth("400px");

        VerticalLayout summarySection = createSummarySection();
        summarySection.setWidth("500px");

        HorizontalLayout topLayout = new HorizontalLayout(recordSection, summarySection);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(JustifyContentMode.START);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);
        topLayout.setAlignItems(FlexComponent.Alignment.START);

        add(topLayout);
    }

    private VerticalLayout createRecordSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", "var(--lumo-base-color)");

        Span title = new Span("Record Inventory");
        title.addClassNames(LumoUtility.TextColor.HEADER, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);

        itemComboBox.setItemsPageable((p, filter) -> Objects.requireNonNull(itemService.getItems(p, filter).block()).getContent());
        itemComboBox.setItemLabelGenerator(it -> StringUtils.capitalize(it.getId() + "-" + it.getName()));
        itemComboBox.setAutoOpen(true);
        itemComboBox.setRequiredIndicatorVisible(true);
        itemComboBox.setWidthFull();
        itemComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                itemIdToAdd = e.getValue().getId();
            }
        });

        quantityField.setMin(1);
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setI18n(new NumberField.NumberFieldI18n()
                .setMinErrorMessage("Minimum quantity is 1")
                .setBadInputErrorMessage("Please enter a valid number")
                .setRequiredErrorMessage("Quantity is required")
        );
        quantityField.setWidthFull();
        quantityField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                quantityToAdd = e.getValue().intValue();
            }
        });

        ComboBox<String> invType = new ComboBox<>("Inventory Type");
        invType.setItems(List.of("T", "W"));
        invType.setAllowCustomValue(false);
        invType.setValue("W");
        invType.setWidthFull();
        invType.setHelperText("T: Top-Up; W: Withdrawal");
        invType.addValueChangeListener(e -> typeToAdd = e.getValue());

        saveInvBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveInvBtn.setWidthFull();

        section.add(title, itemComboBox, quantityField, invType, saveInvBtn);
        return section;
    }

    private VerticalLayout createSummarySection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(false);
        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", "var(--lumo-base-color)");

        Span title = new Span("Inventory Summary");
        title.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.MEDIUM);

        itemId.setWidthFull();
        itemId.setPlaceholder("Enter Item ID");
        itemId.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                paginatedGrid.setFilter(e.getValue());
                setSummaryData(e.getValue());
            }
        });

        itemName.setWidthFull();

        // Left column: top-up and withdrawal counts
        VerticalLayout leftColumn = new VerticalLayout(topUpCount, withdrawalCount, new Span()); // empty span for alignment
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("50%");

        // Right column: totals + remaining stock
        VerticalLayout rightColumn = new VerticalLayout(totalTopUp, totalWithdrawal, totalStock);
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("50%");

        HorizontalLayout summaryRow = new HorizontalLayout(leftColumn, rightColumn);
        summaryRow.setWidthFull();
        summaryRow.setSpacing(true);
        summaryRow.setAlignItems(FlexComponent.Alignment.START);

        // Configure fields for consistent look
        configureSummaryField(itemName);
        configureSummaryField(topUpCount);
        configureSummaryField(totalTopUp);
        configureSummaryField(withdrawalCount);
        configureSummaryField(totalWithdrawal);
        configureSummaryField(totalStock);

        section.add(title, itemId, itemName, summaryRow);
        return section;
    }

    private void configureSummaryField(TextField field) {
        field.setReadOnly(true);
        field.getStyle()
                .set("border", "none")
                .set("box-shadow", "none")
                .set("background", "transparent")
                .set("color", "var(--lumo-body-text-color)")
                .set("cursor", "default");
    }

    private void createTableSection() {
        VerticalLayout tableSection = new VerticalLayout();
        tableSection.setSizeFull();
        tableSection.setPadding(true);
        tableSection.setSpacing(true);

        Span tableTitle = new Span("Inventory Details");
        tableTitle.addClassNames(LumoUtility.TextColor.HEADER, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);

        paginatedGrid.setSizeFull();
        paginatedGrid.addColumn(InventoryDetails::getId).setHeader("ID").setAutoWidth(true);
        paginatedGrid.addColumn(new ComponentRenderer<>(inventoryDetails -> {
            Button itemNameBtn = new Button(inventoryDetails.getItemName());
            itemNameBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            itemNameBtn.addClickListener(e -> itemId.setValue(String.valueOf(inventoryDetails.getItemId())));
            return itemNameBtn;
        })).setHeader("Item Name").setAutoWidth(true);
        paginatedGrid.addColumn(InventoryDetails::getQuantity).setHeader("Quantity").setAutoWidth(true);
        paginatedGrid.addColumn(InventoryDetails::getType).setHeader("Type").setAutoWidth(true);
        paginatedGrid.addColumn(it -> DatetimeUtils.formatDateShort(it.getCreatedDate())).setHeader("Created Date").setAutoWidth(true);
        paginatedGrid.addColumn(it -> DatetimeUtils.formatDateShort(it.getUpdatedDate())).setHeader("Updated Date").setAutoWidth(true);

        tableSection.add(tableTitle, paginatedGrid);
        add(tableSection);
    }

    private void setSummaryData(String itemId) {
        try {
            if (StringUtils.isBlank(itemId)) {
                clearSummary();
                return;
            }
            InventorySummary summary = inventoryService.getSummaryByItemId(itemId).block();
            if (summary != null) {
                itemName.setValue(summary.getItemName());
                totalTopUp.setValue(summary.getTotalTopUp());
                totalWithdrawal.setValue(summary.getTotalWithdraw());
                totalStock.setValue(summary.getRemainingStock());
                topUpCount.setValue(summary.getTopUpCount());
                withdrawalCount.setValue(summary.getWithdrawCount());
                return;
            }
            throw new RuntimeException("Summary not found");
        } catch (Exception e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        clearSummary();
    }

    private void clearSummary() {
        itemName.clear();
        totalTopUp.clear();
        totalWithdrawal.clear();
        totalStock.clear();
        topUpCount.clear();
        withdrawalCount.clear();
    }

    private void setSaveInvBtn() {
        saveInvBtn.addClickListener(e -> {
            if (itemIdToAdd != null && quantityToAdd > 0) {
                log.info("Saving inventory for item: {}, quantity: {}, Type: {}", itemIdToAdd, quantityToAdd, typeToAdd);
                InventoryRequest body = new InventoryRequest(itemIdToAdd, quantityToAdd, typeToAdd);
                inventoryService.saveInventory(body);

                Notification.show("Inventory saved", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                itemComboBox.clear();
                quantityField.clear();
                itemComboBox.setInvalid(false);
                quantityField.setInvalid(false);

                itemIdToAdd = null;
                quantityToAdd = 0;

                paginatedGrid.loadPage(0);
            } else {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
