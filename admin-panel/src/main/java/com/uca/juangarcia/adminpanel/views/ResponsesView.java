package com.uca.juangarcia.adminpanel.views;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.QuestionnaireResponseApiClient;
import com.uca.juangarcia.adminpanel.client.UserApiClient;
import com.uca.juangarcia.adminpanel.dto.AnswerDto;
import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireResponseDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireResponseSummaryDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Respuestas: permite seleccionar un usuario y ver las sesiones de cuestionario
 * que ha <strong>completado</strong>, con el detalle de cada respuesta.
 *
 * <p>Usa el endpoint admin {@code /questionnaires/responses/user/{userId}/completed}
 * para listar y {@code /responses/{responseId}/summary} para el detalle.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "responses", layout = MainLayout.class)
@PageTitle("Respuestas · iFit Admin")
public class ResponsesView extends VerticalLayout {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final QuestionnaireResponseApiClient responseApi;
    private final UserApiClient userApi;
    private final AuthService auth;

    private final ComboBox<AppUserResponseDto> userCombo = new ComboBox<>("Usuario");
    private final Grid<QuestionnaireResponseSummaryDto> grid = new Grid<>(QuestionnaireResponseSummaryDto.class, false);
    private final Span emptyState = new Span("Selecciona un usuario para ver sus sesiones completadas.");

    public ResponsesView(QuestionnaireResponseApiClient responseApi, UserApiClient userApi, AuthService auth) {
        this.responseApi = responseApi;
        this.userApi = userApi;
        this.auth = auth;
        setSizeFull();

        add(new H2("Respuestas"));
        add(buildToolbar());

        emptyState.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(emptyState);

        buildGrid();
        grid.setVisible(false);
        add(grid);
    }

    private HorizontalLayout buildToolbar() {
        userCombo.setWidth("360px");
        userCombo.setPlaceholder("Buscar usuario...");
        userCombo.setClearButtonVisible(true);
        userCombo.setItemLabelGenerator(this::userLabel);
        loadUsers();
        userCombo.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                grid.setItems(List.of());
                grid.setVisible(false);
                emptyState.setText("Selecciona un usuario para ver sus sesiones completadas.");
                emptyState.setVisible(true);
            } else {
                loadResponses(e.getValue());
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(userCombo);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void buildGrid() {
        grid.addColumn(s -> s.questionnaireName() == null ? "—" : s.questionnaireName())
                .setHeader("Cuestionario").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(s -> fmt(s.startedAt())).setHeader("Iniciado").setAutoWidth(true);
        grid.addColumn(s -> fmt(s.completedAt())).setHeader("Completado").setAutoWidth(true);
        grid.addColumn(s -> s.answers() == null ? 0 : s.answers().size())
                .setHeader("Respuestas").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::detailButton).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
    }

    private Button detailButton(QuestionnaireResponseSummaryDto summary) {
        Button view = new Button("Ver detalle", VaadinIcon.EYE.create(), e -> openDetail(summary));
        view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return view;
    }

    private void loadUsers() {
        try {
            List<AppUserResponseDto> users = userApi.listAll();
            userCombo.setItems(users);
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los usuarios", e);
        }
    }

    private void loadResponses(AppUserResponseDto user) {
        try {
            List<QuestionnaireResponseDto> completed = responseApi.listCompletedByUser(user.id());

            List<QuestionnaireResponseSummaryDto> summaries = new ArrayList<>();
            for (QuestionnaireResponseDto r : completed) {
                if (r.responseId() == null) {
                    continue;
                }
                try {
                    QuestionnaireResponseSummaryDto summary = responseApi.getSummary(r.responseId());
                    if (summary != null) {
                        summaries.add(summary);
                    }
                } catch (Exception ignored) {
                    // Si una sesión concreta falla, se omite sin romper el listado
                }
            }

            grid.setItems(summaries);
            if (summaries.isEmpty()) {
                grid.setVisible(false);
                emptyState.setText("Este usuario no tiene sesiones de cuestionario completadas.");
                emptyState.setVisible(true);
            } else {
                emptyState.setVisible(false);
                grid.setVisible(true);
            }
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar las respuestas del usuario", e);
        }
    }

    private void openDetail(QuestionnaireResponseSummaryDto summary) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(summary.questionnaireName() == null ? "Detalle de sesión" : summary.questionnaireName());
        dialog.setWidth("640px");
        dialog.setMaxHeight("85vh");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        if (summary.questionnaireDescription() != null && !summary.questionnaireDescription().isBlank()) {
            Paragraph desc = new Paragraph(summary.questionnaireDescription());
            desc.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
            content.add(desc);
        }

        content.add(metaLine("Usuario", summary.userName()));
        content.add(metaLine("Iniciado", fmt(summary.startedAt())));
        content.add(metaLine("Completado", fmt(summary.completedAt())));

        H3 answersTitle = new H3("Respuestas");
        answersTitle.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
        content.add(answersTitle);

        List<AnswerDto> answers = summary.answers();
        if (answers == null || answers.isEmpty()) {
            content.add(new Span("No hay respuestas registradas."));
        } else {
            int idx = 1;
            for (AnswerDto a : answers) {
                content.add(answerCard(idx++, a));
            }
        }

        dialog.add(content);
        Button close = new Button("Cerrar", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);
        dialog.open();
    }

    private VerticalLayout answerCard(int index, AnswerDto a) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidthFull();
        card.getStyle()
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius)")
            .set("margin-bottom", "var(--lumo-space-s)");

        Span question = new Span(index + ". " + (a.questionText() == null ? "" : a.questionText()));
        question.getStyle().set("font-weight", "600");
        card.add(question);

        Span option = new Span("→ " + (a.selectedOption() == null ? "(sin opción)" : a.selectedOption()));
        option.getStyle().set("color", "var(--lumo-primary-text-color)");
        card.add(option);

        if (a.additionalText() != null && !a.additionalText().isBlank()) {
            Span extra = new Span("Texto: " + a.additionalText());
            extra.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
            card.add(extra);
        }
        if (a.aiDescription() != null && !a.aiDescription().isBlank()) {
            Span ai = new Span("IA: " + a.aiDescription());
            ai.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-style", "italic");
            card.add(ai);
        }
        return card;
    }

    private HorizontalLayout metaLine(String label, String value) {
        Span l = new Span(label + ": ");
        l.getStyle().set("font-weight", "600");
        Span v = new Span(value == null ? "—" : value);
        HorizontalLayout line = new HorizontalLayout(l, v);
        line.setSpacing(false);
        line.getStyle().set("gap", "var(--lumo-space-xs)");
        return line;
    }

    private String userLabel(AppUserResponseDto u) {
        if (u == null) {
            return "";
        }
        String name = u.name() != null ? u.name() : "";
        String email = u.email() != null ? "(" + u.email() + ")" : "";
        return (name + " " + email).trim();
    }

    private String fmt(java.time.LocalDateTime dt) {
        return dt == null ? "—" : dt.format(FMT);
    }
}
