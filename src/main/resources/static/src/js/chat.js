
// See https://getbootstrap.com/docs/5.3/components/tooltips/#overview
function enableTooltips() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
}

function chatAdded(form, data) {
    console.log('Submission was successful.');
    console.log(data);

    const feed = $('#chat-feed');
    const chatToCopy = $('#chat-to-copy');

    const newChat = chatToCopy.clone();
    newChat.find('.chat-username').text(data.username);
    newChat.find('.chat-displaytime').text(data.displayTime);
    newChat.find('.chat-text').text(data.text);
    newChat.removeClass('d-none');
    newChat.id = '';

    feed.append(newChat);
    form.trigger('reset');
    window.scrollTo(0, document.body.scrollHeight);
}

function chatFailed(form, data) {
    console.log('Submission was not successful.');
    console.log(data);
    alert("Failed to add chat");
}

function formHook() {
    const form = $('#chat-form');
    form.submit(function(event) {
        event.preventDefault();

        $.ajax({
            type: form.attr('method'),
            url: form.attr('action'),
            data: form.serialize(),
            success: function (data) {
                chatAdded(form, data);
            },
            error: function (data) {
                chatFailed(form, data);
            },
        });

        console.log('Form submitted');
    });
}

function disableSubmitButton() {
    $('#submit-button').addClass('disabled');
    $('#submit-spinner').removeClass('d-none');
}

window.onload = function() {
    enableTooltips();
    formHook();
    window.scrollTo(0, document.body.scrollHeight);
    $('#submit-button').click(disableSubmitButton);
}