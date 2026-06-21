package com.uca.juangarcia.adminpanel.views;

import java.util.List;

import org.springframework.web.client.RestClientResponseException;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.RoutineApiClient;
import com.uca.juangarcia.adminpanel.dto.RoutineDayDto;
import com.uca.juangarcia.adminpanel.dto.RoutineExerciseDto;
import com.uca.juangarcia.adminpanel.dto.RoutineResponseDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Rutinas asignadas a un cliente: listar, ver detalle (días/ejercicios, solo lectura),
 * activar/desactivar y eliminar.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "users/:userId/routines", layout = MainLayout.class)
@PageTitle("Rutinas · iFit Admin")
public class RoutinesView extends VerticalLayout implements BeforeEnterObserver {

    private final RoutineApiClient api;
    private final AuthService auth;

    private final H2 title = new H2("Rutinas");
    private final Grid<RoutineResponseDto> grid = new Grid<>(RoutineResponseDto.class, false);
    private Long userId;

    public RoutinesView(RoutineApiClient api, AuthService auth) {
        this.api = api;
        this.auth = auth;
        setSizeFull();

        Button back = new Button("Volver a clientes", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(UsersView.class));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(back, title, buildGrid());
    }

    private Grid<RoutineResponseDto> buildGrid() {
        grid.addColumn(r -> r.description() == null ? "—" : r.description())
                .setHeader("Descripción").setFlexGrow(1);
        grid.addColumn(RoutineResponseDto::trainingDays).setHeader("Días").setAutoWidth(true);
        grid.addColumn(r -> Boolean.TRUE.equals(r.isActive()) ? "Activa" : "Inactiva")
                .setHeader("Estado").setAutoWidth(true);
        grid.addColumn(r -> r.currentDay() == null ? "—" : r.currentDay())
                .setHeader("Día actual").setAutoWidth(true);
        grid.addComponentColumn(this::rowActions).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout rowActions(RoutineResponseDto routine) {
        Button detail = new Button(VaadinIcon.EYE.create(), e -> openDetail(routine));
        detail.getElement().setProperty("title", "Ver detalle");
        detail.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        boolean active = Boolean.TRUE.equals(routine.isActive());
        Button toggle = new Button(active ? "Desactivar" : "Activar", e -> toggle(routine, !active));
        toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button delete = new Button(VaadinIcon.TRASH.create(), e -> openDeleteDialog(routine));
        delete.getElement().setProperty("title", "Eliminar");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(detail, toggle, delete);
    }

    private void toggle(RoutineResponseDto routine, boolean active) {
        try {
            api.toggleActive(routine.id(), active);
            Notification.show(active ? "Rutina activada" : "Rutina desactivada", 3000,
                    Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            load();
        } catch (Exception e) {
            handleError("No se pudo cambiar el estado", e);
        }
    }

    private void openDeleteDialog(RoutineResponseDto routine) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar rutina");
        dialog.add(new Span("¿Eliminar la rutina #" + routine.id() + "? Esta acción no se puede deshacer."));

        Button confirm = new Button("Eliminar", e -> {
            try {
                api.delete(routine.id());
                Notification.show("Rutina eliminada", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                load();
            } catch (Exception ex) {
                handleError("No se pudo eliminar la rutina", ex);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private void openDetail(RoutineResponseDto routine) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(routine.description() == null ? "Detalle de rutina" : routine.description());
        dialog.setWidth("640px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        if (routine.days() == null || routine.days().isEmpty()) {
            content.add(new Paragraph("Esta rutina no tiene días registrados."));
        } else {
            for (RoutineDayDto day : routine.days()) {
                content.add(new H4("Día " + day.dayNumber() + " · " + (day.dayName() == null ? "" : day.dayName())));
                if (day.description() != null && !day.description().isBlank()) {
                    Paragraph desc = new Paragraph(day.description());
                    desc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
                    content.add(desc);
                }
                content.add(buildExercisesGrid(day.exercises()));
            }
        }

        dialog.add(content);
        Button close = new Button("Cerrar", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(close);
        dialog.open();
    }

    private Grid<RoutineExerciseDto> buildExercisesGrid(List<RoutineExerciseDto> exercises) {
        Grid<RoutineExerciseDto> g = new Grid<>(RoutineExerciseDto.class, false);
        g.addColumn(RoutineExerciseDto::exerciseName).setHeader("Ejercicio").setFlexGrow(1);
        g.addColumn(RoutineExerciseDto::sets).setHeader("Series").setAutoWidth(true);
        g.addColumn(RoutineExerciseDto::reps).setHeader("Reps").setAutoWidth(true);
        g.addColumn(r -> r.restSeconds() == null ? "—" : r.restSeconds() + "s")
                .setHeader("Descanso").setAutoWidth(true);
        g.setItems(exercises == null ? List.of() : exercises);
        g.setAllRowsVisible(true);
        return g;
    }

    private void load() {
        try {
            List<RoutineResponseDto> routines = api.byUser(userId);
            grid.setItems(routines);
            title.setText("Rutinas del cliente #" + userId + " (" + routines.size() + ")");
        } catch (Exception e) {
            handleError("No se pudieron cargar las rutinas", e);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String param = event.getRouteParameters().get("userId").orElse(null);
        if (param == null) {
            UI.getCurrent().navigate(UsersView.class);
            return;
        }
        try {
            this.userId = Long.valueOf(param);
        } catch (NumberFormatException e) {
            UI.getCurrent().navigate(UsersView.class);
            return;
        }
        load();
    }

    private void handleError(String context, Exception e) {
        if (e instanceof RestClientResponseException http && http.getStatusCode().value() == 401) {
            auth.logout();
            UI.getCurrent().navigate(LoginView.class);
            Notification.show("Sesión expirada, vuelve a iniciar sesión", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        Notification.show(context + ": " + e.getMessage(), 4000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
