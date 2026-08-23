#!/usr/bin/env bash
set -euo pipefail

container=keycloak-fcaptcha-runtime
base_url=http://127.0.0.1:8080

cleanup() {
  docker logs "$container" > /tmp/keycloak-fcaptcha.log 2>&1 || true
  docker rm -f "$container" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker run --detach --name "$container" --publish 127.0.0.1:8080:8080 \
  --env KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  --env KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  keycloak-fcaptcha:test start-dev >/dev/null

for _ in $(seq 1 60); do
  if curl --fail --silent "$base_url/realms/master" >/dev/null; then
    break
  fi
  sleep 2
done
curl --fail --silent "$base_url/realms/master" >/dev/null

token=$(curl --fail --silent \
  --data-urlencode client_id=admin-cli \
  --data-urlencode username=admin \
  --data-urlencode password=admin \
  --data-urlencode grant_type=password \
  "$base_url/realms/master/protocol/openid-connect/token" | jq --raw-output .access_token)

api="$base_url/admin/realms/fcaptcha-test"
auth=(-H "Authorization: Bearer $token" -H "Content-Type: application/json")

curl --fail --silent --show-error "${auth[@]}" -X POST "$base_url/admin/realms" \
  --data '{"realm":"fcaptcha-test","enabled":true}' >/dev/null
curl --fail --silent --show-error "${auth[@]}" -X POST "$api/authentication/flows" \
  --data '{"alias":"browser-fcaptcha","providerId":"basic-flow","topLevel":true,"builtIn":false}' >/dev/null
curl --fail --silent --show-error "${auth[@]}" -X POST \
  "$api/authentication/flows/browser-fcaptcha/executions/execution" \
  --data '{"provider":"fcaptcha"}' >/dev/null

executions=$(curl --fail --silent --show-error "${auth[@]}" \
  "$api/authentication/flows/browser-fcaptcha/executions")
execution_id=$(jq --exit-status --raw-output '.[] | select(.providerId == "fcaptcha") | .id' <<<"$executions")

curl --fail --silent --show-error "${auth[@]}" -X PUT \
  "$api/authentication/flows/browser-fcaptcha/executions" \
  --data "{\"id\":\"$execution_id\",\"requirement\":\"REQUIRED\"}" >/dev/null
curl --fail --silent --show-error "${auth[@]}" -X POST \
  "$api/authentication/executions/$execution_id/config" \
  --data '{"alias":"fcaptcha-test","config":{"fcaptcha.instanceUrl":"https://captcha.example.com","fcaptcha.siteKey":"test-site-key","fcaptcha.verifySecret":"test-verify-secret"}}' >/dev/null
curl --fail --silent --show-error "${auth[@]}" -X PUT "$api" \
  --data '{"realm":"fcaptcha-test","browserFlow":"browser-fcaptcha"}' >/dev/null
curl --fail --silent --show-error "${auth[@]}" -X POST "$api/clients" \
  --data '{"clientId":"smoke-client","enabled":true,"publicClient":true,"redirectUris":["http://localhost/callback"]}' >/dev/null

page=$(curl --fail --silent --show-error --get \
  --data-urlencode client_id=smoke-client \
  --data-urlencode redirect_uri=http://localhost/callback \
  --data-urlencode response_type=code \
  --data-urlencode scope=openid \
  "$base_url/realms/fcaptcha-test/protocol/openid-connect/auth")

grep -F 'id="fcaptcha-widget"' <<<"$page" >/dev/null
grep -F 'data-site-key="test-site-key"' <<<"$page" >/dev/null
grep -F 'src="https://captcha.example.com/fcaptcha.js"' <<<"$page" >/dev/null

