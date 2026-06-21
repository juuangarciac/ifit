package com.uca.juangarcia.adminpanel.views;

import java.util.List;
import java.util.Locale;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.CoachApiClient;
import com.uca.juangarcia.adminpanel.client.ExperienceLevelApiClient;
import com.uca.juangarcia.adminpanel.client.QuestionnaireApiClient;
import com.uca.juangarcia.adminpanel.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.adminpanel.dto.CreateQuestionnaireRequestDto;
import com.uca.juangarcia.adminpanel.dto.ExperienceLevelDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireDto;
import com.uca.juangarcia.adminpanel.dto.UpdateQuestionnaireRequestDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * CRUD de cuestionarios. Crear/editar/eliminar exigen rol admin.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "questionnaires", layout = MainLayout.class)
@PageTitle("Cuestionarios · iFit Admin")
public class QuestionnairesView extends VerticalLayout {

    private final QuestionnaireApiClient api;
    private final CoachApiClient coachApi;
    private final ExperienceLevelApiClient levelApi;
    private final AuthService auth;
    private final Grid<QuestionnaireDto> grid = new Grid<>(QuestionnaireDto.class, false);

    public QuestionnairesView(QuestionnaireApiClient api, CoachApiClient coachApi,
            ExperienceLevelApiClient levelApi, AuthService auth) {
        this.api = api;
        this.coachApi = coachApi;
        this.levelApi = levelApi;
        this.auth = auth;
        setSizeFull();

        Button create = new Button("Nuevo cuestionario", VaadinIcon.PLUS.create(), e -> openEditor(null));
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        H2 title = new H2("Cuestionarios");
        HorizontalLayout header = new HorizontalLayout(title, create);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);

        add(header, buildToolbar(), buildGrid());
        load();
    }

    private HorizontalLayout buildToolbar() {
        TextField search = new TextField();
        search.setPlaceholder("Buscar por nombre...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("360px");
        search.addValueChangeListener(e -> applyFilter(search.getValue()));

        HorizontalLayout toolbar = new HorizontalLayout(search);
        toolbar.setWidthFull();
        toolbar.expand(search);
        return toolbar;
    }

    private Grid<QuestionnaireDto> buildGrid() {
        grid.addColumn(QuestionnaireDto::name).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(q -> q.coachModelTypeEmoji() == null ? "—" : q.coachModelTypeEmoji())
                .setHeader("Coach").setWidth("60px").setFlexGrow(0);
        grid.addColumn(q -> q.experienceLevelName() == null ? "—" : q.experienceLevelName())
                .setHeader("Nivel").setAutoWidth(true);
        grid.addColumn(q -> Boolean.TRUE.equals(q.isEnabled()) ? "Sí" : "No")
                .setHeader("Habilitado").setAutoWidth(true);
        grid.addComponentColumn(this::rowActions).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private HorizontalLayout rowActions(QuestionnaireDto questionnaire) {
        Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditor(questionnaire));
        edit.getElement().setProperty("title", "Editar");
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(questionnaire));
        delete.getElement().setProperty("title", "Eliminar");
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        return new HorizontalLayout(edit, delete);
    }

    private void openEditor(QuestionnaireDto questionnaire) {
        boolean isNew = questionnaire == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Nuevo cuestionario" : "Editar cuestionario");
        dialog.setWidth("480px");

        TextField name = new TextField("Nombre");
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);

        TextArea description = new TextArea("Descripción");
        description.setWidthFull();
        description.setRequiredIndicatorVisible(true);

        ComboBox<CoachModelTypeResponseDto> coachCombo = new ComboBox<>("Entrenador");
        coachCombo.setWidthFull();
        coachCombo.setItemLabelGenerator(c -> {
            if (c.emojiCharacter() != null && c.name() != null) {
                return c.emojiCharacter() + " " + c.name();
            }
            return c.name();
        });
        loadCoachs(coachCombo);

        ComboBox<ExperienceLevelDto> levelCombo = new ComboBox<>("Nivel de experiencia");
        levelCombo.setWidthFull();
        levelCombo.setItemLabelGenerator(ExperienceLevelDto::name);
        loadLevels(levelCombo);

        Checkbox enabledCheckbox = new Checkbox("Habilitado");
        enabledCheckbox.setValue(true);

        if (!isNew) {
            name.setValue(safe(questionnaire.name()));
            description.setValue(safe(questionnaire.description()));
            enabledCheckbox.setValue(Boolean.TRUE.equals(questionnaire.isEnabled()));

            if (questionnaire.coachModelTypeName() != null) {
                coachCombo.getListDataView().getItems()
                        .filter(c -> c.name().equals(questionnaire.coachModelTypeName()))
                        .findFirst()
                        .ifPresent(coachCombo::setValue);
            }

            if (questionnaire.experienceLevelName() != null) {
                levelCombo.getListDataView().getItems()
                        .filter(l -> l.name().equals(questionnaire.experienceLevelName()))
                        .findFirst()
                        .ifPresent(levelCombo::setValue);
            }
        }

        VerticalLayout form = new VerticalLayout(name, description, coachCombo, levelCombo, enabledCheckbox);
        form.setPadding(false);
        form.setSpacing(false);
        dialog.add(form);

        Button save = new Button("Guardar", e -> {
            if (name.getValue() == null || name.getValue().trim().isEmpty()) {
                Notification.show("El nombre es obligatorio", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (description.getValue() == null || description.getValue().trim().isEmpty()) {
                Notification.show("La descripción es obligatoria", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Long coachId = coachCombo.getValue() != null ? coachCombo.getValue().id() : null;
                Long levelId = levelCombo.getValue() != null ? levelCombo.getValue().id() : null;

                if (isNew) {
                    api.create(new CreateQuestionnaireRequestDto(
                            name.getValue(),
                            description.getValue(),
                            coachId,
                            levelId,
                            null
                    ));
                    Notification.show("Cuestionario creado", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    api.update(questionnaire.id(), new UpdateQuestionnaireRequestDto(
                            name.getValue(),
                            description.getValue(),
                            coachId,
                            levelId,
                            null,
                            enabledCheckbox.getValue()
                    ));
                    Notification.show("Cuestionario actualizado", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                dialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo guardar el cuestionario", ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void confirmDelete(QuestionnaireDto questionnaire) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.add(new com.vaadin.flow.component.html.Paragraph(
                "¿Está seguro de que desea eliminar el cuestionario \"" + questionnaire.name() + "\"? Esta acción no se puede deshacer."));

        Button confirm = new Button("Eliminar", e -> {
            try {
                api.delete(questionnaire.id());
                Notification.show("Cuestionario eliminado", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                load();
            } catch (Exception ex) {
                ViewSupport.handleApiError(auth, "No se pudo eliminar el cuestionario", ex);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancel = new Button("Cancelar", e -> confirmDialog.close());
        confirmDialog.getFooter().add(cancel, confirm);
        confirmDialog.open();
    }

    private void loadCoachs(ComboBox<CoachModelTypeResponseDto> combo) {
        try {
            List<CoachModelTypeResponseDto> coachs = coachApi.listAll();
            combo.setItems(coachs);
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los entrenadores", e);
        }
    }

    private void loadLevels(ComboBox<ExperienceLevelDto> combo) {
        try {
            List<ExperienceLevelDto> levels = levelApi.listAll();
            combo.setItems(levels);
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los niveles", e);
        }
    }

    private void load() {
        try {
            List<QuestionnaireDto> all = api.listAll();
            grid.setItems(all);
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los cuestionarios", e);
        }
    }

    private void applyFilter(String term) {
        try {
            List<QuestionnaireDto> all = api.listAll();
            String filterTerm = term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
            if (filterTerm.isEmpty()) {
                grid.setItems(all);
            } else {
                grid.setItems(all.stream()
                        .filter(q -> q.name().toLowerCase(Locale.ROOT).contains(filterTerm))
                        .toList());
            }
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "Error al filtrar", e);
        }
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
