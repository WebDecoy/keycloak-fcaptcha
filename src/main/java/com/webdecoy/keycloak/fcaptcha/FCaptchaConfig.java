package com.webdecoy.keycloak.fcaptcha;

import java.net.URI;
import java.net.URISyntaxException;
import org.keycloak.models.AuthenticatorConfigModel;

record FCaptchaConfig(URI instanceUri, String siteKey, String verifySecret, String expectedHostname) {
    static final String INSTANCE_URL = "fcaptcha.instanceUrl";
    static final String SITE_KEY = "fcaptcha.siteKey";
    static final String VERIFY_SECRET = "fcaptcha.verifySecret";
    static final String EXPECTED_HOSTNAME = "fcaptcha.expectedHostname";

    static FCaptchaConfig from(AuthenticatorConfigModel model) {
        if (model == null || model.getConfig() == null) {
            throw new IllegalArgumentException("FCaptcha is not configured");
        }
        var values = model.getConfig();
        var instanceUrl = required(values.get(INSTANCE_URL), "instance URL");
        var siteKey = required(values.get(SITE_KEY), "site key");
        var verifySecret = required(values.get(VERIFY_SECRET), "verify secret");

        try {
            var uri = new URI(instanceUrl);
            if (uri.getScheme() == null || uri.getHost() == null
                    || !(uri.getScheme().equals("https") || uri.getScheme().equals("http"))
                    || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("FCaptcha instance URL must be an HTTP(S) origin or base URL");
            }
            var normalized = new URI(
                    uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", ""), null, null);
            return new FCaptchaConfig(normalized, siteKey, verifySecret,
                    blankToNull(values.get(EXPECTED_HOSTNAME)));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("FCaptcha instance URL is invalid", exception);
        }
    }

    String clientScriptUrl() {
        return instanceUri + "/fcaptcha.js";
    }

    URI verifyUri() {
        return instanceUri.resolve(instanceUri.getPath() + "/siteverify");
    }

    private static String required(String value, String name) {
        var result = blankToNull(value);
        if (result == null) {
            throw new IllegalArgumentException("FCaptcha " + name + " is required");
        }
        return result;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

