
function copyShareTextToClipboard() {
    const shareText = $('#share-text').text();

    navigator.clipboard
        .writeText(shareText)
        .catch((e) => console.log(e.message));;
}


window.onload = function () {
    $('#share-text-modal').modal('show');
    $('#copy-share-text-button').click(copyShareTextToClipboard);
}