document.addEventListener("DOMContentLoaded", function() {
    // AOS Init
    AOS.init({
        duration: 800,
        once: true,
        offset: 100
    });

    // Prevent double form submission
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            const btn = this.querySelector('button[type="submit"]');
            if (btn) {
                btn.disabled = true;
                btn.textContent = 'Please wait...';
            }
        });
    });
});
