/**
 * Contact form — Web3Forms (no mailto / no email client).
 * Same access key as in-app bug reports (WEB3FORMS_ACCESS_KEY in local.properties).
 * Restrict allowed domains in the Web3Forms dashboard (swaniedesigns.com).
 */
(function () {
  var WEB3FORMS_ACCESS_KEY = "a4ecdd49-5273-432e-89a5-2ad0be291c08";
  var WEB3FORMS_SUBMIT_URL = "https://api.web3forms.com/submit";

  var form = document.getElementById("contact-form");
  if (!form) return;

  var nameInput = document.getElementById("contact-name");
  var emailInput = document.getElementById("contact-email");
  var topicInput = document.getElementById("contact-topic");
  var messageInput = document.getElementById("contact-message");
  var submitBtn = document.getElementById("contact-submit");
  var statusEl = document.getElementById("contact-status");
  var sentCopyEl = document.getElementById("contact-sent-copy");
  var sentCopyBodyEl = document.getElementById("contact-sent-copy-body");
  var honey = form.querySelector('input[name="_honey"]');
  var emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (topicInput) {
    var topicParam = new URLSearchParams(window.location.search).get("topic");
    if (topicParam === "tester" || topicParam === "tester-feedback") {
      topicInput.value = "Tester feedback";
    }
  }

  function setStatus(kind, text) {
    if (!statusEl) return;
    statusEl.hidden = false;
    statusEl.textContent = text;
    statusEl.classList.remove("is-success", "is-error");
    if (kind) statusEl.classList.add(kind === "success" ? "is-success" : "is-error");
  }

  function hideSentCopy() {
    if (sentCopyEl) sentCopyEl.hidden = true;
    if (sentCopyBodyEl) sentCopyBodyEl.textContent = "";
  }

  function showSentCopy(name, email, topic, body) {
    if (!sentCopyEl || !sentCopyBodyEl) return;
    sentCopyBodyEl.textContent =
      "Name: " + name + "\n" + "Email: " + email + "\n" + "Topic: " + topic + "\n\n" + body;
    sentCopyEl.hidden = false;
  }

  function clearInvalid() {
    if (nameInput) nameInput.removeAttribute("aria-invalid");
    if (emailInput) emailInput.removeAttribute("aria-invalid");
    if (messageInput) messageInput.removeAttribute("aria-invalid");
  }

  function buildContactMessage(name, email, topic, body) {
    return (
      "Website contact form\n\n" +
      "Name: " +
      name +
      "\n" +
      "Reply-To: " +
      email +
      "\n" +
      "Topic: " +
      topic +
      "\n\n" +
      body +
      "\n\n---\n" +
      "Sent from https://swaniedesigns.com/contact.html"
    );
  }

  form.addEventListener("submit", function (e) {
    e.preventDefault();
    clearInvalid();
    hideSentCopy();
    if (honey && honey.value.trim()) return;

    var name = nameInput ? nameInput.value.trim() : "";
    var em = emailInput ? emailInput.value.trim() : "";
    var topic = topicInput ? topicInput.value : "General question";
    var body = messageInput ? messageInput.value.trim() : "";
    var invalid = false;

    if (!name) {
      if (nameInput) {
        nameInput.focus();
        nameInput.setAttribute("aria-invalid", "true");
      }
      invalid = true;
    }
    if (!emailRe.test(em)) {
      if (emailInput) {
        emailInput.focus();
        emailInput.setAttribute("aria-invalid", "true");
      }
      invalid = true;
    }
    if (!body) {
      if (messageInput) {
        messageInput.focus();
        messageInput.setAttribute("aria-invalid", "true");
      }
      invalid = true;
    }
    if (invalid) {
      setStatus("error", "Please enter your name, a valid email address, and a message.");
      return;
    }
    if (!WEB3FORMS_ACCESS_KEY) {
      setStatus("error", "The contact form is not configured yet. Please try again later.");
      return;
    }

    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "Sending…";
    }
    setStatus(null, "Sending your message…");

    fetch(WEB3FORMS_SUBMIT_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify({
        access_key: WEB3FORMS_ACCESS_KEY,
        subject: "Swanie's Portfolio — website contact (" + topic + ")",
        name: name,
        email: em,
        message: buildContactMessage(name, em, topic, body),
        botcheck: "",
      }),
    })
      .then(function (res) {
        return res.json().then(function (data) {
          if (!res.ok || !data || data.success !== true) {
            var msg = data && data.message ? data.message : "Request failed";
            throw new Error(msg);
          }
        });
      })
      .then(function () {
        form.reset();
        if (topicInput) topicInput.value = "General question";
        showSentCopy(name, em, topic, body);
        setStatus("success", "Thanks — your message was sent. A copy is below.");
      })
      .catch(function () {
        setStatus("error", "Could not send right now. Please try again in a moment.");
      })
      .finally(function () {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = "Send message";
        }
      });
  });
})();
