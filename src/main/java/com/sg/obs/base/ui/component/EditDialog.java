package com.sg.obs.base.ui.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class EditDialog<T> {

    public static final String UPDATE_OPERATION_COMPLETED = "Update Operation Completed";
    public static final String FAILED_TO_PERFORM_UPDATE = "Failed to perform update";
    @Getter
    private final Dialog mainDialog = new Dialog();
    private final HorizontalLayout rightLayout = new HorizontalLayout();
    private final HorizontalLayout leftLayout = new HorizontalLayout();
    private final Button saveBtn = new Button("Save");


    private final List<InputBinding> inputBindings = new ArrayList<>();
    private Runnable onSuccess;

    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    public EditDialog() {
        VerticalLayout mainLayout = new VerticalLayout();
        Button cancelBtn = new Button("Cancel");

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        mainLayout.add(leftLayout, rightLayout, new HorizontalLayout(saveBtn, cancelBtn));
        mainDialog.add(mainLayout);
        onSuccess = () -> {
        };

        cancelBtn.addClickListener(e -> mainDialog.close());
    }

    public EditDialog<T> show() {
        mainDialog.open();
        return this;
    }

    public void close() {
        mainDialog.close();
    }

    public void onSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void addLeftComponent(String targetField, Component component) {
        inputBindings.add(new InputBinding(targetField, component));
        leftLayout.add(component);
    }

    public void addRightComponent(String targetField, Component component) {
        inputBindings.add(new InputBinding(targetField, component));
        rightLayout.add(component);
    }

    public void addRawMapSaveListener(Consumer<Map<String, String>> listener) {
        saveBtn.addClickListener(event -> {
            saveBtn.setEnabled(false);
            try {
                listener.accept(collectValues());
                Notification.show(UPDATE_OPERATION_COMPLETED, 3000, Notification.Position.TOP_CENTER);
                setSuccess();
            } catch (Exception e) {
                Notification.show(FAILED_TO_PERFORM_UPDATE, 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                saveBtn.setEnabled(true);
            }
        });
    }


    public void addSaveListener(Class<T> clazz, Consumer<T> listener) {
        saveBtn.addClickListener(event -> {
            Map<String, String> values = collectValues();
            saveBtn.setEnabled(false);
            try {
                T obj = objectMapper.convertValue(values, clazz);
                listener.accept(obj);
                Notification.show(UPDATE_OPERATION_COMPLETED, 3000, Notification.Position.TOP_CENTER);
                setSuccess();
            } catch (Exception e) {
                log.error("Failed to convert values to target type {}", e.getMessage(), e);
                Notification.show(FAILED_TO_PERFORM_UPDATE, 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                saveBtn.setEnabled(true);
            }
        });
    }

    public void addSaveListener(TypeReference<T> typeRef, Consumer<T> listener) {
        saveBtn.addClickListener(event -> {
            Map<String, String> values = collectValues();
            saveBtn.setEnabled(false);
            try {
                T obj = objectMapper.convertValue(values, typeRef);
                listener.accept(obj);
                Notification.show(UPDATE_OPERATION_COMPLETED, 3000, Notification.Position.TOP_CENTER);
                setSuccess();
            } catch (Exception e) {
                log.error("Failed to convert values to target type {}", e.getMessage(), e);
                Notification.show(FAILED_TO_PERFORM_UPDATE, 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                saveBtn.setEnabled(true);
            }
        });
    }


    private Map<String, String> collectValues() {
        Map<String, String> values = new HashMap<>();
        for (InputBinding binding : inputBindings) {
            String key = binding.targetField;
            Component comp = binding.component;

            String value = switch (comp) {
                case TextField tf -> tf.getValue();
                case NumberField nf -> nf.getValue() != null ? nf.getValue().toString() : null;
                case TextArea ta -> ta.getValue();
                case ComboBox<?> cb -> cb.getValue() != null ? cb.getValue().toString() : null;
                case Checkbox cb -> String.valueOf(cb.getValue());
                case DatePicker dp -> dp.getValue() != null ? dp.getValue().toString() : null;
                default -> null;
            };

            if (value != null) {
                values.put(key, value);
            }
        }
        return values;
    }


    private void setSuccess() {
        onSuccess.run();
    }


    private record InputBinding(String targetField, Component component) {
    }
}
