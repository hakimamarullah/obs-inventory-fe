package com.sg.obs.base.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.function.BiConsumer;

public class Pagination extends HorizontalLayout {

    private final transient BiConsumer<Integer, Integer> pageLoader;
    private int currentPage = 0;
    private int totalPages = 1;
    private int pageSize = 5;

    private final Button prev = new Button("Prev");
    private final Button next = new Button("Next");
    private final Span pageInfo = new Span();

    public Pagination(BiConsumer<Integer, Integer> pageLoader) {
        this.pageLoader = pageLoader;

        setWidthFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Create page size selector without label to avoid alignment issues
        ComboBox<Integer> pageSizeSelector = new ComboBox<>();
        pageSizeSelector.setItems(List.of(5, 10, 20, 40, 100));
        pageSizeSelector.setValue(pageSize);
        pageSizeSelector.setPlaceholder("Page Size");  // Use placeholder instead of label
        pageSizeSelector.addValueChangeListener(e -> {
            pageSize = e.getValue();
            pageLoader.accept(currentPage, pageSize);
        });

        // Alternative approach: Create a label span and the combobox separately
        Span pageSizeLabel = new Span("Page Size:");
        pageSizeLabel.getStyle().set("margin-right", "5px");

        prev.addClickListener(e -> goToPage(currentPage - 1));
        next.addClickListener(e -> goToPage(currentPage + 1));

        HorizontalLayout controls = new HorizontalLayout(pageSizeLabel, pageSizeSelector, prev, pageInfo, next);

        controls.setAlignItems(Alignment.CENTER);
        controls.setJustifyContentMode(JustifyContentMode.CENTER);
        controls.setWidthFull();

        add(controls);
        updateControls();
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        updateControls();
    }

    public void setTotalPages(int pages) {
        this.totalPages = pages;
        updateControls();
    }

    public int getCurrentPageSize() {
        return pageSize;
    }

    private void goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            pageLoader.accept(page, pageSize);
        }
    }

    private void updateControls() {
        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        prev.setEnabled(currentPage > 0);
        next.setEnabled(currentPage < totalPages - 1);
    }
}