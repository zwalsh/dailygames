function countUp(counterElement) {
    let value = 0;
    const targetValue = parseInt(counterElement.attributes['data-count'].value);

    // No point in animating if it won't tick up every frame
    if (targetValue < 10) {
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


window.onload = function () {
    const counters = document.querySelectorAll('.animate-count-up');
    counters.forEach(counter => runOnFirstAppearance(counter, countUp));
};
