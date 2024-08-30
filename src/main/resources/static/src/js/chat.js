
// See https://getbootstrap.com/docs/5.3/components/tooltips/#overview
function enableTooltips() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
}

window.onload = function() {
    enableTooltips();
    // Scroll to bottom
    // check if query param scrollToBottom is set to true
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('scrollToBottom')) {
        window.scrollTo(0, document.body.scrollHeight);
    }
}