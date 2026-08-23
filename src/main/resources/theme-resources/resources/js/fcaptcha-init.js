(() => {
  "use strict";

  const container = document.getElementById("fcaptcha-widget");
  const token = document.getElementById("fcaptcha-token");
  const submit = document.getElementById("kc-fcaptcha-submit");
  if (!container || !token || !submit || !window.FCaptcha) return;

  const instanceUrl = container.dataset.instanceUrl.replace(/\/+$/, "");
  window.FCaptcha.configure({ serverUrl: instanceUrl });
  window.FCaptcha.render(container, {
    siteKey: container.dataset.siteKey,
    callback: (value) => {
      token.value = value;
      submit.disabled = !value;
    },
    errorCallback: () => {
      token.value = "";
      submit.disabled = true;
    },
  });
})();

