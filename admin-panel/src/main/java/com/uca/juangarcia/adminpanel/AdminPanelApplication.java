package com.uca.juangarcia.adminpanel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Punto de entrada del panel de administración de iFit.
 *
 * <p>Cliente web Vaadin Flow que consume la API de iFit <strong>siempre a través del
 * API Gateway</strong> ({@code ifit.gateway.base-url}). Aplica el tema {@code ifit-admin}
 * en variante oscura ({@link Lumo#DARK}) para alinear la identidad visual con la app móvil.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@SpringBootApplication
@Theme(value = "ifit-admin", variant = Lumo.DARK)
public class AdminPanelApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AdminPanelApplication.class, args);
    }

    /**
     * Configura el favicon de la pestaña del navegador con el logo de iFit (SVG).
     * El recurso vive en {@code META-INF/resources/icons/ifit-logo.svg}.
     */
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "icons/ifit-logo.svg", "64x64");
        settings.addLink("shortcut icon", "icons/ifit-logo.svg");
    }
}
