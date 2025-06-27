package com.sg.obs.base.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConfirmDeleteDialog<T> {

    private final Runnable onSuccess;

    public ConfirmDeleteDialog(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void show(T item,
                     Function<T, String> nameGetter,
                     Consumer<T> deleteFunction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete");
        dialog.add(new Paragraph("Are you sure you want to delete item: " + nameGetter.apply(item) + "?"));

        Button confirm = new Button("Delete", event -> {
            try {
                deleteFunction.accept(item);
                dialog.close();
                Notification.show("Item deleted", 3000, Notification.Position.TOP_CENTER);
                onSuccess.run();
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
}

