package com.uca.juangarcia.adminpanel.views;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.ExperienceLevelApiClient;
import com.uca.juangarcia.adminpanel.dto.CreateExperienceLevelDto;
import com.uca.juangarcia.adminpanel.dto.ExperienceLevelDto;
import com.uca.juangarcia.adminpanel.dto.UpdateExperienceLevelDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
 * CRUD de niveles de experiencia. Crear/editar/eliminar exigen rol admin.
 * El backend solo permite actualizar la <em>descripción</em> (el nombre es fijo al editar).
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "experience-levels", layout = MainLayout.class)
@PageTitle("Niveles · iFit Admin")
public class ExperienceLevelsView extends VerticalLayout {

    private final ExperienceLevelApiClient api;
    private final AuthService auth;
    private final Grid<ExperienceLevelDto> grid = new Grid<>(ExperienceLevelDto.class, false);

    public ExperienceLevelsView(ExperienceLevelApiClient api, AuthService auth) {
        this.api = api;
        this.auth = auth;
        setSizeFull();

        Button create = new Button("Nuevo nivel", VaadinIcon.PLUS.create(), e -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        H2 title = new H2("Niveles de experiencia");
        HorizontalLayout header = new HorizontalLayout(title, create);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);

        add(header, buildGrid());
        load();
    }

    private Grid<ExperienceLevelDto> buildGrid() {
        grid.addColumn(ExperienceLevelDto::name).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(l -> l.description() == null ? "—" : l.description()).setHeader("Descripción").setFlexGrow(1);
        grid.addComponentColumn(this::rowActions).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout rowActions(ExperienceLevelDto level) {
        Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(level));
        edit.getElement().setProperty("title", "Editar descripción");
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button delete = new Button(VaadinIcon.TRASH.create(), e -> openDeleteDialog(level));
        delete.getElement().setProperty("title", "Eliminar");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(edit, delete);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuevo nivel de experiencia");
        dialog.setWidth("420px");

        TextField name = new TextField("Nombre");
        name.setWidthFull();
        TextArea description = new TextArea("Descripción");
        description.setWidthFull();

        VerticalLayout form = new VerticalLayout(name, description);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Crear", e -> {
            try {
                api.create(new CreateExperienceLevelDto(name.getValue(), description.getValue()));
                Notification.show("Nivel creado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo crear el nivel", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openEditDialog(ExperienceLevelDto level) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar nivel: " + level.name());
        dialog.setWidth("420px");

        TextField name = new TextField("Nombre");
        name.setValue(level.name() == null ? "" : level.name());
        name.setReadOnly(true);
        name.setWidthFull();

        TextArea description = new TextArea("Descripción");
        description.setValue(level.description() == null ? "" : level.description());
        description.setWidthFull();

        VerticalLayout form = new VerticalLayout(name, description);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Guardar", e -> {
            try {
                api.update(level.id(), new UpdateExperienceLevelDto(description.getValue()));
                Notification.show("Nivel actualizado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo actualizar el nivel", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(ExperienceLevelDto level) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar nivel");
        dialog.add(new Span("¿Eliminar el nivel \"" + level.name() + "\"? Esta acción no se puede deshacer."));

        Button confirm = new Button("Eliminar", e -> {
            try {
                api.delete(level.id());
                Notification.show("Nivel eliminado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo eliminar el nivel", ex);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private void load() {
        try {
            grid.setItems(api.listAll());
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los niveles", e);
        }
    }
}
