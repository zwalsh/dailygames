
// See https://getbootstrap.com/docs/5.3/components/tooltips/#overview
function enableTooltips() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
}

function copyShareTextToClipboard() {
    const shareText = $('#share-text').text();

    navigator.clipboard
        .writeText(shareText)
        .catch((e) => console.log(e.message));;
}

function popModal() {
    enableTooltips();
    $('#share-text-modal').modal('show');
    $('#copy-share-text-button').click(copyShareTextToClipboard);
}

function disableSubmitButton() {
    $('#submit-button').addClass('disabled');
    $('#submit-spinner').removeClass('d-none');
}

window.onload = function () {
    if (window.location.href.indexOf("showModal") != -1) {
       popModal();
    }
    $('#share-text-button').click(popModal);
    $('#submit-button').click(disableSubmitButton);
}