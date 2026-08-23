package com.webdecoy.keycloak.fcaptcha;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

final class FCaptchaAuthenticator implements Authenticator {
    private static final Logger LOG = Logger.getLogger(FCaptchaAuthenticator.class);
    private static final String TOKEN_FIELD = "fcaptcha_token";
    private final FCaptchaVerifier verifier = new FCaptchaVerifier();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            challenge(context, FCaptchaConfig.from(context.getAuthenticatorConfig()), null);
        } catch (IllegalArgumentException exception) {
            LOG.error("FCaptcha provider is misconfigured", exception);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("FCaptcha is not configured").createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        try {
            var config = FCaptchaConfig.from(context.getAuthenticatorConfig());
            var token = context.getHttpRequest().getDecodedFormParameters().getFirst(TOKEN_FIELD);
            var client = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
            var remoteIp = context.getConnection().getRemoteAddr();

            if (verifier.verify(client, config, token, remoteIp)) {
                context.success();
            } else {
                challenge(context, config, "FCaptcha verification failed. Please try again.");
            }
        } catch (IllegalArgumentException exception) {
            LOG.error("FCaptcha provider is misconfigured", exception);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        } catch (Exception exception) {
            LOG.warn("FCaptcha verification request failed", exception);
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("FCaptcha is temporarily unavailable").createErrorPage(Response.Status.BAD_GATEWAY));
        }
    }

    private void challenge(AuthenticationFlowContext context, FCaptchaConfig config, String error) {
        var form = context.form()
                .setAttribute("fcaptchaScriptUrl", config.clientScriptUrl())
                .setAttribute("fcaptchaSiteKey", config.siteKey())
                .setAttribute("fcaptchaInstanceUrl", config.instanceUri().toString());
        if (error != null) {
            form.setError(error);
        }
        context.challenge(form.createForm("fcaptcha.ftl"));
    }

    @Override public boolean requiresUser() { return false; }
    @Override public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }
    @Override public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) { }
    @Override public void close() { }
}

