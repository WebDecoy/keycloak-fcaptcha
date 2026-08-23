package com.webdecoy.keycloak.fcaptcha;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public final class FCaptchaAuthenticatorFactory implements AuthenticatorFactory {
    public static final String ID = "fcaptcha";
    private static final Authenticator INSTANCE = new FCaptchaAuthenticator();

    @Override public String getId() { return ID; }
    @Override public String getDisplayType() { return "FCaptcha"; }
    @Override public String getReferenceCategory() { return "captcha"; }
    @Override public String getHelpText() { return "Verify visitors with a self-hosted FCaptcha instance."; }
    @Override public boolean isConfigurable() { return true; }
    @Override public boolean isUserSetupAllowed() { return false; }
    @Override public Authenticator create(KeycloakSession session) { return INSTANCE; }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.CONDITIONAL,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                property(FCaptchaConfig.INSTANCE_URL, "Instance URL",
                        "Base URL of your FCaptcha server, for example https://captcha.example.com", false),
                property(FCaptchaConfig.SITE_KEY, "Site key", "FCaptcha site key presented to the browser", false),
                property(FCaptchaConfig.VERIFY_SECRET, "Verify secret",
                        "FCAPTCHA_VERIFY_SECRET used only for server-side Siteverify calls", true),
                property(FCaptchaConfig.EXPECTED_HOSTNAME, "Expected hostname",
                        "Optional exact hostname required in successful tokens", false));
    }

    private static ProviderConfigProperty property(String name, String label, String help, boolean secret) {
        return new ProviderConfigProperty(name, label, help,
                secret ? ProviderConfigProperty.PASSWORD : ProviderConfigProperty.STRING_TYPE,
                "", secret);
    }

    @Override public void init(Config.Scope config) { }
    @Override public void postInit(KeycloakSessionFactory factory) { }
    @Override public void close() { }
}

