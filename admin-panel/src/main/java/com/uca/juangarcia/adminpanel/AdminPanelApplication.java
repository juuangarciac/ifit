package com.uca.juangarcia.adminpanel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
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
}
