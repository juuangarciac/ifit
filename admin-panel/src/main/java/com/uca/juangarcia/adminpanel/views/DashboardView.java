package com.uca.juangarcia.adminpanel.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.client.CoachApiClient;
import com.uca.juangarcia.adminpanel.client.ExerciseApiClient;
import com.uca.juangarcia.adminpanel.client.ExperienceLevelApiClient;
import com.uca.juangarcia.adminpanel.client.QuestionnaireApiClient;
import com.uca.juangarcia.adminpanel.client.RoutineApiClient;
import com.uca.juangarcia.adminpanel.client.UserApiClient;
import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Dashboard de estadísticas: tarjetas KPI con los totales del sistema y
 * gráficos en CSS (donut de verificación y barras de rutinas por usuario).
 *
 * <p>Todos los datos se obtienen de la API existente; las rutinas se cuentan
 * recorriendo los usuarios (1 llamada por usuario), lo que de paso da el
 * desglose "rutinas por usuario".
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard · iFit Admin")
public class DashboardView extends VerticalLayout {

    private final UserApiClient userApi;
    private final ExerciseApiClient exerciseApi;
    private final CoachApiClient coachApi;
    private final ExperienceLevelApiClient levelApi;
    private final QuestionnaireApiClient questionnaireApi;
    private final RoutineApiClient routineApi;
    private final AuthService auth;

    public DashboardView(UserApiClient userApi, ExerciseApiClient exerciseApi, CoachApiClient coachApi,
            ExperienceLevelApiClient levelApi, QuestionnaireApiClient questionnaireApi,
            RoutineApiClient routineApi, AuthService auth) {
        this.userApi = userApi;
        this.exerciseApi = exerciseApi;
        this.coachApi = coachApi;
        this.levelApi = levelApi;
        this.questionnaireApi = questionnaireApi;
        this.routineApi = routineApi;
        this.auth = auth;

        setSizeFull();
        setPadding(true);
        add(new H2("Dashboard"));

        render();
    }

    private void render() {
        // ── Recogida de datos (resiliente: cada métrica por separado) ────────
        List<AppUserResponseDto> users = safeList(userApi::listAll);
        long totalUsers = users.size();
        long verifiedUsers = users.stream().filter(AppUserResponseDto::verified).count();

        long totalExercises = 0;
        try { totalExercises = exerciseApi.page(0, 1).totalElements(); } catch (Exception ignored) { }

        long totalCoaches = safeCount(() -> coachApi.listAll().size());
        long totalLevels = safeCount(() -> levelApi.listAll().size());
        long totalQuestionnaires = safeCount(() -> questionnaireApi.listAll().size());

        // Rutinas por usuario (N llamadas) → total + desglose
        List<UserCount> routinesPerUser = new ArrayList<>();
        long totalRoutines = 0;
        for (AppUserResponseDto u : users) {
            int count = 0;
            try {
                count = (int) routineApi.byUser(u.id()).stream()
                        .filter(r -> !Boolean.TRUE.equals(r.deleted()))
                        .count();
            } catch (Exception ignored) { }
            totalRoutines += count;
            if (count > 0) {
                routinesPerUser.add(new UserCount(userLabel(u), count));
            }
        }
        routinesPerUser.sort(Comparator.comparingInt(UserCount::count).reversed());

        double avgRoutines = totalUsers == 0 ? 0 : (double) totalRoutines / totalUsers;

        // ── Tarjetas KPI ─────────────────────────────────────────────────────
        Div kpis = new Div();
        kpis.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
            .set("gap", "var(--lumo-space-m)")
            .set("width", "100%");
        kpis.add(
            kpiCard(VaadinIcon.USERS, "Usuarios", String.valueOf(totalUsers), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.CHECK_CIRCLE, "Verificados", verifiedUsers + " / " + totalUsers, "var(--lumo-success-color)"),
            kpiCard(VaadinIcon.CALENDAR, "Rutinas", String.valueOf(totalRoutines), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.CHART, "Rutinas/usuario", String.format(Locale.US, "%.1f", avgRoutines), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.LIST, "Ejercicios", String.valueOf(totalExercises), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.USER_HEART, "Modelos", String.valueOf(totalCoaches), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.TROPHY, "Niveles", String.valueOf(totalLevels), "var(--lumo-primary-color)"),
            kpiCard(VaadinIcon.CLIPBOARD, "Cuestionarios", String.valueOf(totalQuestionnaires), "var(--lumo-primary-color)")
        );
        add(kpis);

        // ── Gráficos ─────────────────────────────────────────────────────────
        Div charts = new Div();
        charts.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(320px, 1fr))")
            .set("gap", "var(--lumo-space-m)")
            .set("width", "100%")
            .set("margin-top", "var(--lumo-space-l)");
        charts.add(verificationDonut(verifiedUsers, totalUsers));
        charts.add(routinesBarChart(routinesPerUser));
        add(charts);
    }

    // ── Tarjeta KPI ──────────────────────────────────────────────────────────
    private Div kpiCard(VaadinIcon icon, String label, String value, String accent) {
        Div card = cardBox();
        Div top = new Div();
        top.getStyle().set("display", "flex").set("align-items", "center").set("gap", "var(--lumo-space-s)");
        Icon ic = icon.create();
        ic.setColor(accent);
        ic.setSize("22px");
        Span lbl = new Span(label);
        lbl.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
        top.add(ic, lbl);

        Span num = new Span(value);
        num.getStyle()
            .set("font-size", "2.2rem")
            .set("font-weight", "700")
            .set("line-height", "1.1")
            .set("margin-top", "var(--lumo-space-xs)")
            .set("display", "block");

        card.add(top, num);
        return card;
    }

    // ── Donut verificados vs no (conic-gradient) ──────────────────────────────
    private Div verificationDonut(long verified, long total) {
        Div card = cardBox();
        card.add(chartTitle("Verificación de usuarios"));

        long notVerified = Math.max(0, total - verified);
        double pct = total == 0 ? 0 : (double) verified / total;
        String pctTurn = String.format(Locale.US, "%.4f", pct);

        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("align-items", "center").set("gap", "var(--lumo-space-l)");

        Div donut = new Div();
        donut.getStyle()
            .set("width", "150px").set("height", "150px").set("border-radius", "50%")
            .set("background",
                "conic-gradient(var(--lumo-success-color) 0 " + pctTurn + "turn,"
                + " var(--lumo-contrast-20pct) " + pctTurn + "turn 1turn)")
            .set("display", "flex").set("align-items", "center").set("justify-content", "center")
            .set("flex", "0 0 auto");
        // Agujero central
        Div hole = new Div();
        hole.getStyle()
            .set("width", "96px").set("height", "96px").set("border-radius", "50%")
            .set("background", "var(--lumo-base-color)")
            .set("display", "flex").set("flex-direction", "column")
            .set("align-items", "center").set("justify-content", "center");
        Span pctText = new Span(Math.round(pct * 100) + "%");
        pctText.getStyle().set("font-size", "1.5rem").set("font-weight", "700");
        Span pctLbl = new Span("verificados");
        pctLbl.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");
        hole.add(pctText, pctLbl);
        donut.add(hole);

        Div legend = new Div();
        legend.add(legendItem("var(--lumo-success-color)", "Verificados", verified));
        legend.add(legendItem("var(--lumo-contrast-20pct)", "No verificados", notVerified));

        wrapper.add(donut, legend);
        card.add(wrapper);
        return card;
    }

    private Div legendItem(String color, String label, long value) {
        Div row = new Div();
        row.getStyle().set("display", "flex").set("align-items", "center")
            .set("gap", "var(--lumo-space-s)").set("margin-bottom", "var(--lumo-space-xs)");
        Div dot = new Div();
        dot.getStyle().set("width", "12px").set("height", "12px").set("border-radius", "3px").set("background", color);
        Span txt = new Span(label + ": " + value);
        row.add(dot, txt);
        return row;
    }

    // ── Barras: rutinas por usuario ───────────────────────────────────────────
    private Div routinesBarChart(List<UserCount> data) {
        Div card = cardBox();
        card.add(chartTitle("Rutinas por usuario"));

        if (data.isEmpty()) {
            Span empty = new Span("Aún no hay rutinas generadas.");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            card.add(empty);
            return card;
        }

        int max = data.stream().mapToInt(UserCount::count).max().orElse(1);
        Div list = new Div();
        list.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "var(--lumo-space-s)");

        for (UserCount uc : data) {
            double widthPct = max == 0 ? 0 : (double) uc.count() / max * 100.0;

            Div row = new Div();
            row.getStyle().set("display", "flex").set("align-items", "center").set("gap", "var(--lumo-space-s)");

            Span name = new Span(uc.label());
            name.getStyle()
                .set("width", "160px").set("flex", "0 0 160px")
                .set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis")
                .set("font-size", "var(--lumo-font-size-s)");

            Div track = new Div();
            track.getStyle()
                .set("flex", "1").set("height", "18px")
                .set("background", "var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-s)");
            Div fill = new Div();
            fill.getStyle()
                .set("width", String.format(Locale.US, "%.2f", widthPct) + "%").set("height", "100%")
                .set("background", "var(--lumo-primary-color)").set("border-radius", "var(--lumo-border-radius-s)");
            track.add(fill);

            Span val = new Span(String.valueOf(uc.count()));
            val.getStyle().set("width", "28px").set("text-align", "right").set("font-weight", "600");

            row.add(name, track, val);
            list.add(row);
        }
        card.add(list);
        return card;
    }

    // ── Helpers de estilo ─────────────────────────────────────────────────────
    private Div cardBox() {
        Div card = new Div();
        card.getStyle()
            .set("padding", "var(--lumo-space-m)")
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("box-sizing", "border-box")
            .set("background", "var(--lumo-base-color)");
        return card;
    }

    private H3 chartTitle(String text) {
        H3 h = new H3(text);
        h.getStyle().set("margin", "0 0 var(--lumo-space-m) 0").set("font-size", "var(--lumo-font-size-l)");
        return h;
    }

    private String userLabel(AppUserResponseDto u) {
        if (u.name() != null && !u.name().isBlank()) {
            return u.name();
        }
        return u.email() != null ? u.email() : ("Usuario " + u.id());
    }

    // ── Utilidades de carga resiliente ────────────────────────────────────────
    private List<AppUserResponseDto> safeList(java.util.function.Supplier<List<AppUserResponseDto>> supplier) {
        try {
            List<AppUserResponseDto> r = supplier.get();
            return r == null ? List.of() : r;
        } catch (Exception e) {
            ViewSupport.handleApiError(auth, "No se pudieron cargar los usuarios", e);
            return List.of();
        }
    }

    private long safeCount(java.util.function.Supplier<Integer> supplier) {
        try {
            Integer v = supplier.get();
            return v == null ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Par (etiqueta de usuario, nº de rutinas). */
    private record UserCount(String label, int count) {}
}
