package com.uca.juangarcia.adminpanel.views;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.CoachApiClient;
import com.uca.juangarcia.adminpanel.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.adminpanel.dto.CreateCoachModelTypeRequestDto;
import com.uca.juangarcia.adminpanel.dto.UpdateCoachModelTypeRequestDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * CRUD de entrenadores (tipos de modelo de coach). Crear/editar/(des)habilitar exigen rol admin.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "coaches", layout = MainLayout.class)
@PageTitle("Entrenadores · iFit Admin")
public class CoachesView extends VerticalLayout {

    private final CoachApiClient api;
    private final AuthService auth;
    private final Grid<CoachModelTypeResponseDto> grid = new Grid<>(CoachModelTypeResponseDto.class, false);

    public CoachesView(CoachApiClient api, AuthService auth) {
        this.api = api;
        this.auth = auth;
        setSizeFull();

        Button create = new Button("Nuevo entrenador", VaadinIcon.PLUS.create(), e -> openEditor(null));
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        H2 title = new H2("Entrenadores");
        HorizontalLayout header = new HorizontalLayout(title, create);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);

        add(header, buildGrid());
        load();
    }

    private Grid<CoachModelTypeResponseDto> buildGrid() {
        grid.addColumn(c -> c.emojiCharacter() == null ? "" : c.emojiCharacter()).setHeader("").setWidth("60px").setFlexGrow(0);
        grid.addColumn(CoachModelTypeResponseDto::name).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> c.description() == null ? "—" : c.description()).setHeader("Descripción").setFlexGrow(1);
        grid.addColumn(c -> Boolean.TRUE.equals(c.enabled()) ? "Sí" : "No").setHeader("Habilitado").setAutoWidth(true);
        grid.addComponentColumn(this::rowActions).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout rowActions(CoachModelTypeResponseDto coach) {
        Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditor(coach));
        edit.getElement().setProperty("title", "Editar");
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        boolean enabled = Boolean.TRUE.equals(coach.enabled());
        Button toggle = new Button(enabled ? "Deshabilitar" : "Habilitar", e -> {
            try {
                if (enabled) {
                    api.disable(coach.id());
                } else {
                    api.enable(coach.id());
                }
                Notification.show(enabled ? "Entrenador deshabilitado" : "Entrenador habilitado",
                        3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo cambiar el estado", ex);
            }
        });
        toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        return new HorizontalLayout(edit, toggle);
    }

    private void openEditor(CoachModelTypeResponseDto coach) {
        boolean isNew = coach == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Nuevo entrenador" : "Editar entrenador");
        dialog.setWidth("420px");

        TextField name = new TextField("Nombre");
        name.setWidthFull();
        TextField emoji = new TextField("Emoji");
        TextArea description = new TextArea("Descripción");
        description.setWidthFull();

        if (!isNew) {
            name.setValue(safe(coach.name()));
            emoji.setValue(safe(coach.emojiCharacter()));
            description.setValue(safe(coach.description()));
        }

        VerticalLayout form = new VerticalLayout(name, emoji, description);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Guardar", e -> {
            try {
                if (isNew) {
                    api.create(new CreateCoachModelTypeRequestDto(
                            name.getValue(), description.getValue(), emoji.getValue(), Boolean.TRUE));
                } else {
                    api.update(coach.id(), new UpdateCoachModelTypeRequestDto(
                            name.getValue(), description.getValue(), emoji.getValue(), coach.enabled()));
                }
                Notification.show(isNew ? "Entrenador creado" : "Entrenador actualizado",
                        3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo guardar", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void load() {
        try {
            grid.setItems(api.listAll());
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los entrenadores", e);
        }
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
