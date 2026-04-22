document.addEventListener("DOMContentLoaded", function() {
    if (typeof AOS !== "undefined") {
        AOS.init({
            duration: 760,
            easing: "ease-out-cubic",
            once: true,
            offset: 90
        });
    } else {
        document.querySelectorAll("[data-aos]").forEach(el => {
            el.style.opacity = "1";
            el.style.transform = "none";
        });
    }

    document.querySelectorAll("form").forEach(form => {
        form.addEventListener("submit", function() {
            const btn = this.querySelector('button[type="submit"]');
            if (!btn || btn.dataset.allowRepeat === "true") {
                return;
            }
            btn.disabled = true;
            btn.dataset.originalHtml = btn.innerHTML;
            btn.innerHTML = '<span class="inline-flex items-center gap-2"><i class="fa-solid fa-circle-notch fa-spin"></i> Please wait</span>';
        });
    });
});
