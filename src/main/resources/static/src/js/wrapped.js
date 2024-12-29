function countUp(counterElement) {
    let value = 0;
    const targetValue = parseInt(counterElement.attributes['data-count'].value);

    // No point in animating if it won't tick up every frame
    if (targetValue < 10) {
       counterElement.innerText = targetValue;
       return;
    }

    const increment = targetValue / (1000 / 16); // 1000 ms at 16 frames per second

    function updateCounter() {
      value += increment;
      if (value > targetValue) {
        value = targetValue;
      }

      counterElement.innerText = Math.round(value);

      if (value < targetValue) {
        setTimeout(updateCounter, 16); // 16 ms -> 60 fps
      }
    }

    updateCounter();
}

function animateReveal(element) {
    element.classList.remove('animate-rev');
    element.innerText = element.attributes['text'].value
    // CSS animation triggers on .animate-reveal
    element.classList.add('animate-reveal');
}

function animate(element) {
    if (element.classList.contains('animate-count-up')) {
        countUp(element);
    }
    if (element.classList.contains('animate-rev')) {
        animateReveal(element);
    }
}

function runOnFirstAppearance(element, callback) {
    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                callback(entry.target);
                observer.unobserve(entry.target);
            }
        });
    });

    observer.observe(element);
}

function addWebShare() {
   $('#share-button').click(async () => {
       console.log('Share button clicked');
       const year = $('#share-button')[0].attributes['data-year'].value;
       const username = $('#share-button')[0].attributes['data-username'].value;

       await navigator.share({
          title: username + '\'s Daily Games Wrapped ' + year,
          text: 'Check out ' + username +'\'s Daily Games Wrapped, ' + year +'!',
          url: '/wrapped/' + year + '/' + username
       });
   });
}

// Override the height on each element so it's no longer based on vh
// This way if the viewport height changes (like when you scroll), the height of the element won't change
// Without this, the height of the elements change and the page jumps around
function setElementHeights() {
    const elements = Array.from(document.getElementsByClassName('vh-90'));
    const initialHeight = window.innerHeight * 0.9;

    for (const e of elements) {
        e.style.height = initialHeight + 'px';
        e.classList.remove('vh-90');
    }
}

window.onload = function () {
    const counters = document.querySelectorAll('.animate');
    counters.forEach(counter => runOnFirstAppearance(counter, animate));

    addWebShare();

    setElementHeights();
};
