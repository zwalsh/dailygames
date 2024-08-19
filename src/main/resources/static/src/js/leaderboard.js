function buildLeaderboardChart(dataLabels, dataValues, elementId) {
    var ctxAllTime = document.getElementById(elementId);
    var data = {
        labels: dataLabels,
        datasets: [{
            label: 'Average Score',
            borderRadius: 5,
            data: dataValues
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
}


window.onload = function() {

    var dataLabels = ['zach', 'derknasty', 'jackiewalsh', 'ChatGPT', 'MikMap'];
    var dataValues = [5.5, 5.3, 4.3, 3.9, 3.2];
    buildLeaderboardChart(dataLabels, dataValues, 'game-leaderboard-all-time');
}