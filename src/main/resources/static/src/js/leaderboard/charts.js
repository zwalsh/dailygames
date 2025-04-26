export function buildLeaderboardChart(dataLabels, dataValues, datasetLabel, elementId) {
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

export function buildHistogramChart(dataLabels, dataValues, datasetLabel, elementId) {
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
                },
                tooltip: {
                   callbacks: {
                     label: function(context) {
                        return context.parsed.y + '%';
                     }
                   }
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