function buildLeaderboardChart(dataLabels, dataValues, datasetLabel, elementId) {
    var ctxAllTime = document.getElementById(elementId);
    var data = {
        labels: dataLabels,
        datasets: [{
            label: datasetLabel,
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

function buildHistogramChart(dataLabels, dataValues, datasetLabel, elementId) {
    var canvas = document.getElementById(elementId);
    var data = {
        labels: dataLabels,
        datasets: [{
            label: datasetLabel,
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
                        color: "white"
                    }
                }
            }
        }
    };
    var chart = new Chart(canvas, config);
}

function renderCharts(response) {
    buildLeaderboardChart(response.allTimePoints.labels, response.allTimePoints.dataPoints, 'Total Points', 'all-time-points');
    buildLeaderboardChart(response.allTimeGames.labels, response.allTimeGames.dataPoints, 'Games Played', 'all-time-games');
    buildLeaderboardChart(response.allTimeAverage.labels, response.allTimeAverage.dataPoints, 'Average Score', 'all-time-average');

    buildLeaderboardChart(response.thirtyDaysPoints.labels, response.thirtyDaysPoints.dataPoints, 'Total Points', 'thirty-days-points');
    buildLeaderboardChart(response.thirtyDaysGames.labels, response.thirtyDaysGames.dataPoints, 'Games Played', 'thirty-days-games');
    buildLeaderboardChart(response.thirtyDaysAverage.labels, response.thirtyDaysAverage.dataPoints, 'Average Score', 'thirty-days-average');

    buildHistogramChart(response.allTimeHistogram.labels, response.allTimeHistogram.dataPoints, 'Percentage', 'all-time-histogram');
}


window.onload = function() {
    $.ajax(window.location.href + '/data').done(renderCharts);
}