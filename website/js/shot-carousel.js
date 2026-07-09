(function () {
  var root = document.getElementById("shot-carousel");
  var track = document.getElementById("shot-carousel-track");
  var dotsHost = document.getElementById("shot-carousel-dots");
  var captionTitle = document.getElementById("shot-caption-title");
  var captionDesc = document.getElementById("shot-caption-desc");
  if (!root || !track || !dotsHost) return;

  var slides = Array.prototype.slice.call(track.querySelectorAll(".shot-slide"));
  if (!slides.length) return;

  var prevBtn = root.querySelector(".shot-carousel-prev");
  var nextBtn = root.querySelector(".shot-carousel-next");
  var index = 0;
  var hoverPaused = false;
  var pauseUntil = 0;
  var autoTimer = null;
  var resumeTimer = null;
  var reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var AUTO_MS = 5500;
  var MANUAL_PAUSE_MS = 8000;

  var touchStartX = 0;
  var touchStartY = 0;
  var touchActive = false;

  function visibleCount() {
    if (window.innerWidth <= 560) return 1;
    if (window.innerWidth <= 960) return 2;
    return 4;
  }

  function maxIndex() {
    return Math.max(0, slides.length - visibleCount());
  }

  function slideStepPx() {
    var first = slides[0];
    if (!first) return 0;
    var gap = parseFloat(getComputedStyle(track).gap) || 22;
    return first.getBoundingClientRect().width + gap;
  }

  function clampIndex(i) {
    return Math.max(0, Math.min(i, maxIndex()));
  }

  function updateCaption() {
    var slide = slides[index];
    if (!slide || !captionTitle || !captionDesc) return;
    var title = slide.getAttribute("data-title") || "";
    var desc = slide.getAttribute("data-desc") || "";
    captionTitle.textContent = title.replace(/\.\s*$/, "") + ".";
    captionDesc.textContent = desc;
  }

  function renderDots() {
    dotsHost.innerHTML = "";
    var pages = maxIndex() + 1;
    for (var p = 0; p < pages; p += 1) {
      var dot = document.createElement("button");
      dot.type = "button";
      dot.className = "shot-carousel-dot" + (p === index ? " is-active" : "");
      dot.setAttribute("role", "tab");
      dot.setAttribute("aria-label", "Show screenshot set " + (p + 1));
      dot.setAttribute("aria-selected", p === index ? "true" : "false");
      (function (pageIndex) {
        dot.addEventListener("click", function () {
          goTo(pageIndex, true);
        });
      })(p);
      dotsHost.appendChild(dot);
    }
  }

  function applyTransform() {
    track.style.transform = "translate3d(" + (-index * slideStepPx()) + "px, 0, 0)";
  }

  function goTo(i, userTriggered) {
    index = clampIndex(i);
    applyTransform();
    updateCaption();
    renderDots();
    if (userTriggered) pauseAfterInteraction();
  }

  function next(userTriggered) {
    goTo(index >= maxIndex() ? 0 : index + 1, !!userTriggered);
  }

  function prev(userTriggered) {
    goTo(index <= 0 ? maxIndex() : index - 1, !!userTriggered);
  }

  function pauseAfterInteraction() {
    pauseUntil = Date.now() + MANUAL_PAUSE_MS;
    window.clearTimeout(resumeTimer);
    resumeTimer = window.setTimeout(function () {
      pauseUntil = 0;
    }, MANUAL_PAUSE_MS);
  }

  function canAutoAdvance() {
    if (reducedMotion) return false;
    if (hoverPaused) return false;
    if (Date.now() < pauseUntil) return false;
    if (document.hidden) return false;
    return true;
  }

  function startAuto() {
    window.clearInterval(autoTimer);
    if (reducedMotion) return;
    autoTimer = window.setInterval(function () {
      if (canAutoAdvance()) next(false);
    }, AUTO_MS);
  }

  function onResize() {
    index = clampIndex(index);
    applyTransform();
    renderDots();
    updateCaption();
  }

  if (prevBtn) {
    prevBtn.addEventListener("click", function () { prev(true); });
  }
  if (nextBtn) {
    nextBtn.addEventListener("click", function () { next(true); });
  }

  root.addEventListener("mouseenter", function () { hoverPaused = true; });
  root.addEventListener("mouseleave", function () { hoverPaused = false; });

  root.addEventListener("keydown", function (e) {
    if (e.key === "ArrowLeft") {
      e.preventDefault();
      prev(true);
    } else if (e.key === "ArrowRight") {
      e.preventDefault();
      next(true);
    }
  });

  track.addEventListener("touchstart", function (e) {
    if (!e.touches || e.touches.length !== 1) return;
    touchActive = true;
    touchStartX = e.touches[0].clientX;
    touchStartY = e.touches[0].clientY;
  }, { passive: true });

  track.addEventListener("touchend", function (e) {
    if (!touchActive || !e.changedTouches || !e.changedTouches.length) return;
    touchActive = false;
    var dx = e.changedTouches[0].clientX - touchStartX;
    var dy = e.changedTouches[0].clientY - touchStartY;
    if (Math.abs(dx) < 40 || Math.abs(dx) < Math.abs(dy)) return;
    if (dx < 0) next(true);
    else prev(true);
  }, { passive: true });

  var resizeTimer = null;
  window.addEventListener("resize", function () {
    window.clearTimeout(resizeTimer);
    resizeTimer = window.setTimeout(onResize, 150);
  }, { passive: true });

  onResize();
  startAuto();

  if (document.fonts && document.fonts.ready) {
    document.fonts.ready.then(onResize);
  }
  window.addEventListener("load", onResize, { once: true });
})();
