package com.uca.juangarcia.adminpanel.views;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.ExerciseApiClient;
import com.uca.juangarcia.adminpanel.dto.ExerciseDetailDto;
import com.uca.juangarcia.adminpanel.dto.ExerciseSummaryDto;
import com.uca.juangarcia.adminpanel.dto.PageResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Catálogo de ejercicios. La grid es de solo lectura; cada fila abre un diálogo de
 * detalle con instrucciones e imágenes (consumiendo {@code GET /exercises/{id}}).
 * No hay endpoints de escritura en iFit para ejercicios.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "exercises", layout = MainLayout.class)
@PageTitle("Ejercicios · iFit Admin")
public class ExercisesView extends VerticalLayout {

    private static final int PAGE_SIZE = 500;

    private final ExerciseApiClient api;
    private final AuthService auth;

    /** Prefijo para resolver las {@code imageUrls} relativas contra el gateway. */
    private final String imageBaseUrl;

    private final Grid<ExerciseSummaryDto> grid = new Grid<>(ExerciseSummaryDto.class, false);
    private final TextField search = new TextField();
    private List<ExerciseSummaryDto> all = List.of();

    public ExercisesView(ExerciseApiClient api, AuthService auth,
            @Value("${ifit.gateway.base-url}") String gatewayBaseUrl) {
        this.api = api;
        this.auth = auth;
        this.imageBaseUrl = gatewayBaseUrl + "/ifit/api/v1";
        setSizeFull();
        add(new H2("Catálogo de ejercicios"), buildToolbar(), buildGrid());
        load();
    }

    private HorizontalLayout buildToolbar() {
        search.setPlaceholder("Buscar por nombre, músculo, categoría...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("360px");
        search.addValueChangeListener(e -> applyFilter());

        HorizontalLayout toolbar = new HorizontalLayout(search);
        toolbar.setWidthFull();
        toolbar.expand(search);
        return toolbar;
    }

    private Grid<ExerciseSummaryDto> buildGrid() {
        grid.addColumn(ExerciseSummaryDto::name).setHeader("Nombre").setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(e -> orDash(e.level())).setHeader("Nivel").setAutoWidth(true);
        grid.addColumn(e -> orDash(e.category())).setHeader("Categoría").setAutoWidth(true);
        grid.addColumn(e -> orDash(e.equipment())).setHeader("Equipo").setAutoWidth(true);
        grid.addColumn(e -> join(e.primaryMuscles())).setHeader("Músculos").setFlexGrow(1);
        grid.addComponentColumn(this::detailButton).setHeader("Detalle").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        return grid;
    }

    private Button detailButton(ExerciseSummaryDto exercise) {
        Button view = new Button(VaadinIcon.EYE.create(), e -> openDetail(exercise));
        view.getElement().setProperty("title", "Ver detalle");
        view.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        return view;
    }

    private void openDetail(ExerciseSummaryDto summary) {
        ExerciseDetailDto detail;
        try {
            detail = api.detail(summary.id());
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudo cargar el detalle del ejercicio", e);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(detail.name());
        dialog.setWidth("760px");
        dialog.setMaxWidth("95vw");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.add(buildMeta(detail));

        if (detail.imageUrls() != null && !detail.imageUrls().isEmpty()) {
            content.add(new H3("Imágenes"), buildGallery(detail));
        }

        if (detail.instructions() != null && !detail.instructions().isEmpty()) {
            OrderedList steps = new OrderedList();
            detail.instructions().forEach(step -> steps.add(new ListItem(step)));
            content.add(new H3("Instrucciones"), steps);
        }

        dialog.add(content);
        Button close = new Button("Cerrar", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(close);
        dialog.open();
    }

    private FormLayout buildMeta(ExerciseDetailDto d) {
        FormLayout meta = new FormLayout();
        meta.addFormItem(new Span(orDash(d.level())), "Nivel");
        meta.addFormItem(new Span(orDash(d.category())), "Categoría");
        meta.addFormItem(new Span(orDash(d.equipment())), "Equipo");
        meta.addFormItem(new Span(orDash(d.mechanic())), "Mecánica");
        meta.addFormItem(new Span(orDash(d.force())), "Fuerza");
        meta.addFormItem(new Span(orDash(join(d.primaryMuscles()))), "Músculos primarios");
        meta.addFormItem(new Span(orDash(join(d.secondaryMuscles()))), "Músculos secundarios");
        meta.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", 2));
        return meta;
    }

    private HorizontalLayout buildGallery(ExerciseDetailDto d) {
        HorizontalLayout gallery = new HorizontalLayout();
        gallery.setWidthFull();
        gallery.getStyle().set("flex-wrap", "wrap");
        for (String url : d.imageUrls()) {
            Image img = new Image(imageBaseUrl + url, d.name());
            img.setWidth("320px");
            img.getStyle().set("border-radius", "8px");
            gallery.add(img);
        }
        return gallery;
    }

    private void load() {
        try {
            PageResponse<ExerciseSummaryDto> page = api.page(0, PAGE_SIZE);
            all = page.content() == null ? List.of() : page.content();
            applyFilter();
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudo cargar el catálogo", e);
        }
    }

    private void applyFilter() {
        String term = search.getValue() == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        if (term.isEmpty()) {
            grid.setItems(all);
            return;
        }
        grid.setItems(all.stream().filter(e -> matches(e, term)).toList());
    }

    private boolean matches(ExerciseSummaryDto e, String term) {
        return contains(e.name(), term)
                || contains(e.category(), term)
                || contains(e.equipment(), term)
                || contains(join(e.primaryMuscles()), term);
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? "" : String.join(", ", values);
    }

    private String orDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
