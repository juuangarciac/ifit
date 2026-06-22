package com.uca.juangarcia.adminpanel.views;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/**
 * Layout principal (cabecera + drawer de navegación) y <strong>guard de sesión</strong>:
 * cualquier vista bajo este layout exige sesión iniciada; si no, redirige a {@link LoginView}.
 *
 * @author Juan Garcia
 * @version 1.0
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthService auth;

    public MainLayout(AuthService auth) {
        this.auth = auth;
        setPrimarySection(Section.DRAWER);
        buildNavbar();
        buildDrawer();
    }

    private void buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();

        Image logo = new Image("icons/ifit-logo.svg", "iFit");
        logo.setHeight("32px");
        logo.setWidth("32px");

        H1 title = new H1("iFit · Admin");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        HorizontalLayout brand = new HorizontalLayout(logo, title);
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(true);

        AppUserResponseDto current = auth.getCurrentUser();
        Span user = new Span(current != null && current.email() != null ? current.email() : "");
        user.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-inline", "var(--lumo-space-m)");

        Button logout = new Button("Salir", VaadinIcon.SIGN_OUT.create(), e -> {
            auth.logout();
            UI.getCurrent().navigate(LoginView.class);
        });
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(toggle, brand, user, logout);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(brand);
        header.getStyle().set("padding-inline", "var(--lumo-space-m)");
        addToNavbar(header);
    }

    private void buildDrawer() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Clientes", UsersView.class, VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Ejercicios", ExercisesView.class, VaadinIcon.LIST.create()));
        nav.addItem(new SideNavItem("Entrenadores", CoachesView.class, VaadinIcon.USER_HEART.create()));
        nav.addItem(new SideNavItem("Niveles", ExperienceLevelsView.class, VaadinIcon.TROPHY.create()));
        nav.addItem(new SideNavItem("Cuestionarios", QuestionnairesView.class, VaadinIcon.CLIPBOARD.create()));
        nav.addItem(new SideNavItem("Respuestas", ResponsesView.class, VaadinIcon.RECORDS.create()));
        nav.addItem(new SideNavItem("Documentación", DocumentationView.class, VaadinIcon.BOOK.create()));
        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!auth.isAuthenticated()) {
            event.forwardTo(LoginView.class);
        }
    }
}
