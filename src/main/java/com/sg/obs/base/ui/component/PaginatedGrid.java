package com.sg.obs.base.ui.component;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PaginatedGrid<T> extends VerticalLayout {

    @Getter
    private final Grid<T> grid;
    @Getter
    private final ListDataProvider<T> dataProvider;
    private final Pagination pagination;


    private final transient BiFunction<PageRequest, String, Page<T>> fetchFunction;


    private String currentFilter = "";

    public PaginatedGrid(Class<T> beanType,
                         BiFunction<PageRequest, String, Page<T>> fetchFunction) {
        this.fetchFunction = fetchFunction;
        this.grid = new Grid<>(beanType, false);
        this.dataProvider = new ListDataProvider<>(new ArrayList<>());
        this.grid.setDataProvider(dataProvider);

        this.pagination = new Pagination(this::loadPage);

        setSizeFull();
        add(grid, pagination);
    }

    public void setFilter(String filter) {
        this.currentFilter = Optional.of(filter).orElse("");
        loadPage(0, pagination.getCurrentPageSize());
    }

    public void loadPage(int pageIndex, int pageSize) {
        var sort = grid.getSortOrder().isEmpty() ?
                Sort.unsorted() :
                Sort.by(grid.getSortOrder().stream().map(this::getSortBy).toList());

        var pageable = PageRequest.of(pageIndex, pageSize, sort);

        Page<T> results = fetchFunction.apply(pageable, currentFilter);

        Collection<T> modifiableItems = results.getContent();
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(modifiableItems);
        dataProvider.refreshAll();

        pagination.setTotalPages(results.getTotalPages());
        pagination.setCurrentPage(pageIndex);
    }

    private Sort.Order getSortBy(GridSortOrder<T> order) {
        return order.getDirection() == SortDirection.ASCENDING ?
                Sort.Order.asc(order.getSorted().getKey()) :
                Sort.Order.desc(order.getSorted().getKey());
    }

    public void loadPage(int pageIndex) {
        loadPage(pageIndex, pagination.getCurrentPageSize());
    }

    public void addActionColumn(Consumer<T> editHandler, Consumer<T> deleteHandler) {
        grid.addComponentColumn(item -> {
            Button edit = new Button("Edit", VaadinIcon.EDIT.create());
            Button delete = new Button("Delete", VaadinIcon.TRASH.create());

            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            edit.addClickListener(e -> editHandler.accept(item));
            delete.addClickListener(e -> deleteHandler.accept(item));

            return new HorizontalLayout(edit, delete);
        }).setHeader("Actions").setAutoWidth(true);
    }

    public <V> Column<T> addColumn(ValueProvider<T, V> valueProvider, String header) {
        return grid.addColumn(valueProvider).setHeader(header).setAutoWidth(true).setSortable(true).setKey(getColKey());
    }

    public <V> Column<T> addColumn(ValueProvider<T, V> valueProvider) {
        return grid.addColumn(valueProvider).setSortable(true).setKey(getColKey());
    }

    public Column<T> addColumn(Renderer<T> renderer) {
        return grid.addColumn(renderer).setSortable(true).setKey(getColKey());
    }

    private String getColKey() {
        return "col_" + grid.getColumns().size();
    }
}

