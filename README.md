# Keycloak FCaptcha Provider

A Keycloak Authenticator SPI that adds self-hosted [FCaptcha](https://github.com/WebDecoy/FCaptcha) to browser authentication flows.

## Requirements

- Keycloak 26.x
- Java 21
- A reachable FCaptcha instance with a site key and verify secret

## Build and install

```bash
mvn clean verify
cp target/keycloak-fcaptcha-0.1.0.jar /opt/keycloak/providers/
/opt/keycloak/bin/kc.sh build
```

Restart Keycloak, duplicate the built-in browser flow, add the **FCaptcha** execution before the username/password form, configure it, set it to **Required**, and bind the copied flow.

Configuration:

- **Instance URL:** public base URL of the FCaptcha server
- **Site key:** browser-facing site key
- **Verify secret:** server-side `FCAPTCHA_VERIFY_SECRET`; never exposed to the browser
- **Expected hostname:** optional exact hostname check for successful tokens

Add the FCaptcha instance origin to the realm's Content Security Policy `script-src` and `connect-src` directives. The provider fails closed on missing tokens, non-200 responses, invalid JSON, upstream failures, and hostname mismatches.

## Docker

```dockerfile
FROM quay.io/keycloak/keycloak:26.3.3
COPY target/keycloak-fcaptcha-0.1.0.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
```

Prebuilt JARs and container images are published with each GitHub release.


## License

MIT
