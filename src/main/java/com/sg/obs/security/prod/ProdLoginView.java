package com.sg.obs.security.prod;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Login")
@AnonymousAllowed
class ProdLoginView extends Main implements BeforeEnterObserver {

    static final String LOGIN_PATH = "login";

    private final AuthenticationContext authenticationContext;
    private final LoginForm login;

    ProdLoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        // Create the components
        login = new LoginForm();
        login.setAction(LOGIN_PATH);


        // Configure the view
        setSizeFull();
        var centerDiv = new Div(login);
        add(centerDiv);


        // Style the view
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER,
                LumoUtility.Background.CONTRAST_5);
        centerDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            // Redirect to the main view if the user is already logged in. This makes impersonation easier to work with.
            event.forwardTo("");
            return;
        }

        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}

