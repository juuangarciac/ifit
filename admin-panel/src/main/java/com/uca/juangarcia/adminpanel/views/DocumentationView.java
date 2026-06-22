package com.uca.juangarcia.adminpanel.views;

import org.springframework.beans.factory.annotation.Value;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;

/**
 * Módulo de documentación: reúne los enlaces a las APIs (OpenAPI/Swagger) y a
 * las consolas de operación (Eureka, Keycloak), y permite abrirlos directamente.
 *
 * <p>Los enlaces se construyen con el <strong>host de la petición actual</strong>
 * (IP o dominio), no con {@code localhost}, para que funcionen al abrirlos desde
 * el navegador del administrador aunque el panel corra en un servidor remoto.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route(value = "documentation", layout = MainLayout.class)
@PageTitle("Documentación · iFit Admin")
public class DocumentationView extends VerticalLayout {

    private final int ifitPort;
    private final int ronniePort;
    private final int eurekaPort;
    private final int keycloakPort;

    public DocumentationView(
            @Value("${ifit.docs.ifit-port:8081}") int ifitPort,
            @Value("${ifit.docs.ronnie-port:8082}") int ronniePort,
            @Value("${ifit.docs.eureka-port:8761}") int eurekaPort,
            @Value("${ifit.docs.keycloak-port:9090}") int keycloakPort) {
        this.ifitPort = ifitPort;
        this.ronniePort = ronniePort;
        this.eurekaPort = eurekaPort;
        this.keycloakPort = keycloakPort;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Documentación"));
        Paragraph intro = new Paragraph(
                "Accede a la documentación interactiva de las APIs (OpenAPI/Swagger) y a las "
                + "consolas de operación del sistema. Los enlaces se abren en una pestaña nueva.");
        intro.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
        add(intro);

        // ── APIs con OpenAPI ────────────────────────────────────────────────
        add(sectionTitle("APIs (OpenAPI / Swagger)"));
        HorizontalLayout apis = new HorizontalLayout();
        apis.setWidthFull();
        apis.getStyle().set("flex-wrap", "wrap");
        apis.add(
            apiCard("iFit", "Lógica de negocio: usuarios, cuestionarios, rutinas, ejercicios.",
                    VaadinIcon.COG, ifitPort, true),
            apiCard("Ronnie", "Motor de IA (LangChain4j): generación de rutinas y chat.",
                    VaadinIcon.MAGIC, ronniePort, true)
        );
        add(apis);

        // ── Consolas de operación ───────────────────────────────────────────
        add(sectionTitle("Consolas de operación"));
        HorizontalLayout consoles = new HorizontalLayout();
        consoles.setWidthFull();
        consoles.getStyle().set("flex-wrap", "wrap");
        consoles.add(
            consoleCard("Eureka", "Service discovery: microservicios registrados y su estado.",
                    VaadinIcon.CLUSTER, baseUrl() + ":" + eurekaPort),
            consoleCard("Keycloak", "Gestión de autenticación: realms, clientes y usuarios.",
                    VaadinIcon.KEY, baseUrl() + ":" + keycloakPort)
        );
        add(consoles);
    }

    /** Esquema + host de la petición actual (sin puerto), p. ej. {@code http://203.0.113.10}. */
    private String baseUrl() {
        VaadinServletRequest req = (VaadinServletRequest) VaadinServletRequest.getCurrent();
        if (req != null) {
            String scheme = req.getScheme();
            String host = req.getServerName();
            return scheme + "://" + host;
        }
        return "http://localhost";
    }

    private H3 sectionTitle(String text) {
        H3 h = new H3(text);
        h.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        return h;
    }

    /** Tarjeta para un servicio con Swagger UI + OpenAPI JSON. */
    private VerticalLayout apiCard(String name, String description, VaadinIcon icon, int port, boolean hasJson) {
        String swaggerUrl = baseUrl() + ":" + port + "/swagger-ui.html";
        String jsonUrl = baseUrl() + ":" + port + "/v3/api-docs";

        VerticalLayout card = baseCard(name, description, icon);
        card.add(new Span(baseUrl().replaceFirst("^https?://", "") + ":" + port));

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(openButtonAnchor("Abrir Swagger UI", swaggerUrl, true));
        if (hasJson) {
            actions.add(openButtonAnchor("OpenAPI JSON", jsonUrl, false));
        }
        card.add(actions);
        return card;
    }

    /** Tarjeta para una consola genérica (un único enlace). */
    private VerticalLayout consoleCard(String name, String description, VaadinIcon icon, String url) {
        VerticalLayout card = baseCard(name, description, icon);
        card.add(openButtonAnchor("Abrir", url, true));
        return card;
    }

    private VerticalLayout baseCard(String name, String description, VaadinIcon icon) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("320px");
        card.setSpacing(false);
        card.setPadding(true);
        card.getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)");

        HorizontalLayout head = new HorizontalLayout();
        head.setAlignItems(FlexComponent.Alignment.CENTER);
        head.setSpacing(true);
        var ic = icon.create();
        ic.setColor("var(--lumo-primary-color)");
        H3 title = new H3(name);
        title.getStyle().set("margin", "0");
        head.add(ic, title);

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin", "var(--lumo-space-xs) 0 var(--lumo-space-s) 0");

        card.add(head, desc);
        return card;
    }

    /** Botón que abre una URL en pestaña nueva (Anchor para no depender del backend). */
    private Anchor openButtonAnchor(String text, String url, boolean primary) {
        Button button = new Button(text, VaadinIcon.EXTERNAL_LINK.create());
        button.addThemeVariants(primary ? ButtonVariant.LUMO_PRIMARY : ButtonVariant.LUMO_TERTIARY);
        Anchor anchor = new Anchor(url, button);
        anchor.setTarget("_blank");
        anchor.getElement().setAttribute("rel", "noopener noreferrer");
        return anchor;
    }
}
