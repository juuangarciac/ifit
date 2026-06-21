package com.uca.juangarcia.adminpanel.views;

import org.springframework.web.client.RestClientResponseException;

import com.uca.juangarcia.adminpanel.client.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Utilidades compartidas por las vistas (manejo común de errores de API).
 */
public final class ViewSupport {

    private ViewSupport() {
    }

    /**
     * Traduce excepciones de las llamadas REST a notificaciones:
     * <ul>
     *   <li>401 → cierra sesión y redirige al login.</li>
     *   <li>403 → aviso de permisos insuficientes.</li>
     *   <li>resto → mensaje con el contexto.</li>
     * </ul>
     */
    public static void handleApiError(AuthService auth, String context, Exception e) {
        if (e instanceof RestClientResponseException http) {
            int status = http.getStatusCode().value();
            if (status == 401) {
                auth.logout();
                error("Sesión expirada, vuelve a iniciar sesión");
                UI.getCurrent().navigate(LoginView.class);
                return;
            }
            if (status == 403) {
                error("No tienes permisos para esta operación (403).");
                return;
            }
        }
        error(context + ": " + e.getMessage());
    }

    private static void error(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
