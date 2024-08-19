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

function renderCharts(response) {
    buildLeaderboardChart(response.allTime.labels, response.allTime.dataPoints, 'game-leaderboard-all-time');
    buildLeaderboardChart(response.past30Days.labels, response.past30Days.dataPoints, 'game-leaderboard-past-30-days');
}


window.onload = function() {
    $.ajax(window.location.href + '/data').done(renderCharts);
}