package com.webdecoy.keycloak.fcaptcha;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

final class FCaptchaVerifier {
    private static final ObjectMapper JSON = new ObjectMapper();

    boolean verify(HttpClient client, FCaptchaConfig config, String token, String remoteIp)
            throws IOException {
        if (token == null || token.isBlank()) {
            return false;
        }

        var fields = new ArrayList<BasicNameValuePair>();
        fields.add(new BasicNameValuePair("secret", config.verifySecret()));
        fields.add(new BasicNameValuePair("response", token));
        if (remoteIp != null && !remoteIp.isBlank()) {
            fields.add(new BasicNameValuePair("remoteip", remoteIp));
        }

        var request = new HttpPost(config.verifyUri());
        request.setHeader("Accept", "application/json");
        request.setEntity(new UrlEncodedFormEntity(fields, StandardCharsets.UTF_8));

        var response = client.execute(request);
        try {
            if (response.getStatusLine().getStatusCode() != 200 || response.getEntity() == null) {
                return false;
            }
            JsonNode body = JSON.readTree(EntityUtils.toByteArray(response.getEntity()));
            if (!body.path("success").asBoolean(false)) {
                return false;
            }
            return config.expectedHostname() == null
                    || config.expectedHostname().equalsIgnoreCase(body.path("hostname").asText(""));
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }
}

