package com.uca.juangarcia.adminpanel.views;

import java.util.List;
import java.util.Locale;

import org.springframework.web.client.RestClientResponseException;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.UserApiClient;
import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.uca.juangarcia.adminpanel.dto.RegisterRequestDto;
import com.uca.juangarcia.adminpanel.dto.UpdateAppUserRequestDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

/**
 * Vista de gestión de clientes: crear, listar, buscar, ver, editar y eliminar; y navegar a sus rutinas.
 *
 * <p>El alta usa {@code POST /ifit/api/v1/auth/register} (no {@code POST /users}), el único endpoint
 * que crea la identidad de forma coherente en Keycloak + BD. El nuevo cliente queda sin verificar y
 * recibe un email de verificación que debe confirmar antes de poder iniciar sesión.
 *
 * @author Juan Garcia
 * @version 1.1
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Clientes · iFit Admin")
public class UsersView extends VerticalLayout {

    private final UserApiClient api;
    private final AuthService auth;

    private final Grid<AppUserResponseDto> grid = new Grid<>(AppUserResponseDto.class, false);
    private final TextField search = new TextField();
    private List<AppUserResponseDto> all = List.of();

    public UsersView(UserApiClient api, AuthService auth) {
        this.api = api;
        this.auth = auth;
        setSizeFull();

        add(new H2("Clientes"), buildToolbar(), buildGrid());
        load();
    }

    private HorizontalLayout buildToolbar() {
        search.setPlaceholder("Buscar por nombre o email...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("320px");
        search.addValueChangeListener(e -> applyFilter());

        Button refresh = new Button("Refrescar", VaadinIcon.REFRESH.create(), e -> load());

        Button create = new Button("Nuevo cliente", VaadinIcon.PLUS.create(), e -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(search, refresh, create);
        toolbar.setWidthFull();
        toolbar.expand(search);
        return toolbar;
    }

    private Grid<AppUserResponseDto> buildGrid() {
        grid.addColumn(AppUserResponseDto::name).setHeader("Nombre").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(AppUserResponseDto::email).setHeader("Email").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(AppUserResponseDto::roleName).setHeader("Rol").setAutoWidth(true);
        grid.addColumn(u -> u.experienceLevelName() == null ? "—" : u.experienceLevelName())
                .setHeader("Nivel").setAutoWidth(true);
        grid.addColumn(u -> u.verified() ? "Sí" : "No").setHeader("Verificado").setAutoWidth(true);
        grid.addComponentColumn(this::rowActions).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout rowActions(AppUserResponseDto user) {
        Button routines = new Button(VaadinIcon.LIST.create(),
                e -> UI.getCurrent().navigate(RoutinesView.class,
                        new RouteParameters("userId", String.valueOf(user.id()))));
        routines.getElement().setProperty("title", "Ver rutinas");
        routines.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(user));
        edit.getElement().setProperty("title", "Editar");
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button delete = new Button(VaadinIcon.TRASH.create(), e -> openDeleteDialog(user));
        delete.getElement().setProperty("title", "Eliminar");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(routines, edit, delete);
    }

    private void load() {
        try {
            all = api.listAll();
            applyFilter();
        } catch (Exception e) {
            handleError("No se pudieron cargar los clientes", e);
        }
    }

    private void applyFilter() {
        String term = search.getValue() == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        if (term.isEmpty()) {
            grid.setItems(all);
            return;
        }
        grid.setItems(all.stream()
                .filter(u -> contains(u.name(), term) || contains(u.email(), term))
                .toList());
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuevo cliente");

        TextField name = new TextField("Nombre");
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);

        TextField surname = new TextField("Apellidos");
        surname.setWidthFull();

        EmailField email = new EmailField("Email");
        email.setWidthFull();
        email.setRequiredIndicatorVisible(true);

        PasswordField password = new PasswordField("Contraseña");
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);
        password.setHelperText("Mínimo 8 caracteres");

        Span hint = new Span("El cliente recibirá un email de verificación; deberá confirmarlo antes de poder iniciar sesión.");
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout form = new VerticalLayout(name, surname, email, password, hint);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Crear", e -> {
            String error = validateNewClient(name.getValue(), email.getValue(), email.isInvalid(), password.getValue());
            if (error != null) {
                Notification.show(error, 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                api.create(new RegisterRequestDto(
                        name.getValue().trim(),
                        blankToNull(surname.getValue()),
                        email.getValue().trim(),
                        password.getValue()));
                Notification.show("Cliente creado. Se ha enviado un email de verificación.",
                                4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                handleError("No se pudo crear el cliente", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    /** Validación de cliente nuevo en el cliente (refleja las restricciones de iFit). */
    private String validateNewClient(String name, String email, boolean emailInvalid, String password) {
        if (name == null || name.trim().length() < 2) {
            return "El nombre debe tener al menos 2 caracteres.";
        }
        if (email == null || email.isBlank() || emailInvalid) {
            return "Introduce un email válido.";
        }
        if (password == null || password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void openEditDialog(AppUserResponseDto user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar cliente #" + user.id());

        TextField name = new TextField("Nombre");
        name.setValue(user.name() == null ? "" : user.name());
        name.setWidthFull();

        EmailField email = new EmailField("Email");
        email.setValue(user.email() == null ? "" : user.email());
        email.setWidthFull();

        VerticalLayout form = new VerticalLayout(name, email);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Guardar", e -> {
            try {
                api.update(user.id(), new UpdateAppUserRequestDto(name.getValue(), email.getValue()));
                Notification.show("Cliente actualizado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                handleError("No se pudo actualizar el cliente", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(AppUserResponseDto user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar cliente");
        dialog.add(new Span("¿Seguro que quieres eliminar a " + user.email() + "? Esta acción no se puede deshacer."));

        Button confirm = new Button("Eliminar", e -> {
            try {
                api.delete(user.id());
                Notification.show("Cliente eliminado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                handleError("No se pudo eliminar el cliente", ex);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    /** Manejo común de errores HTTP: 401 → re-login; 409 → email duplicado; resto → notificación. */
    private void handleError(String context, Exception e) {
        if (e instanceof RestClientResponseException http) {
            int status = http.getStatusCode().value();
            if (status == 401) {
                auth.logout();
                UI.getCurrent().navigate(LoginView.class);
                Notification.show("Sesión expirada, vuelve a iniciar sesión", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (status == 409) {
                Notification.show("Ya existe un cliente con ese email.", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }
        Notification.show(context + ": " + e.getMessage(), 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
