package com.uca.juangarcia.adminpanel.views;

import java.util.Optional;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Pantalla de login del panel. Verifica credenciales + rol admin vía {@link AuthService}.
 * No usa layout (es una vista independiente, accesible sin sesión).
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Route("login")
@PageTitle("Acceso · iFit Admin")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService auth;
    private final LoginForm loginForm = new LoginForm();

    public LoginView(AuthService auth) {
        this.auth = auth;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("iFit · Panel de administración");
        Paragraph subtitle = new Paragraph("Acceso restringido a administradores");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Iniciar sesión");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Contraseña");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setForgotPassword("");
        loginForm.setI18n(i18n);
        loginForm.setForgotPasswordButtonVisible(false);

        loginForm.addLoginListener(event -> {
            Optional<String> error = auth.login(event.getUsername(), event.getPassword());
            if (error.isEmpty()) {
                loginForm.setError(false);
                UI.getCurrent().navigate(UsersView.class);
            } else {
                loginForm.setError(true);
                Notification n = Notification.show(error.get(), 4000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        add(title, subtitle, loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (auth.isAuthenticated()) {
            event.forwardTo(UsersView.class);
        }
    }
}
