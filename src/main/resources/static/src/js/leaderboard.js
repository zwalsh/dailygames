var ctxAllTime = document.getElementById('game-leaderboard-all-time');
var data = {
    labels: ['zach', 'derknasty', 'jackiewalsh', 'ChatGPT', 'MikMap'],
    datasets: [{
        label: 'Average Score',
        borderRadius: 5,
        data: [5.5, 5.3, 4.3, 3.9, 3.2]
    }]
};
var config = {
    type: 'bar',
    data: data,
    options: {
        plugins: {
            legend: {
                display: false
            }
        },
        indexAxis: 'y',
        aspectRatio: 1.25,
        scales: {
            x: {
                grid: {
                    display: false
                }
            },
            y: {
                grid: {
                    display: false
                },
                ticks: {
                    mirror: true,
                    color: "white",
                    font: {
                        weight: "bold",
                        size: 15
                    }
                }
            }
        }
    }
};
var chart = new Chart(ctxAllTime, config);