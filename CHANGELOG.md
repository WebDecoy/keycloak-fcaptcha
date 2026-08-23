# Changelog

## 0.1.0 - 2026-08-23

- Add a configurable FCaptcha browser-flow authenticator for Keycloak 26.x.
- Verify tokens server-side through the self-hosted Siteverify endpoint.
- Optionally require the hostname signed into successful tokens.
- Fail closed on missing tokens, invalid responses, and upstream failures.
- Package the challenge template and initializer as provider resources.
- Exercise installation and challenge rendering in a live Keycloak container.

