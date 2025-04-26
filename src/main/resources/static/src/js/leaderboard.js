import { buildLeaderboardChart, buildHistogramChart } from './leaderboard/charts.js';

function renderCharts(response) {
    buildLeaderboardChart(response.allTimePoints.labels, response.allTimePoints.dataPoints, 'Total Points', 'all-time-points');
    buildLeaderboardChart(response.allTimeGames.labels, response.allTimeGames.dataPoints, 'Games Played', 'all-time-games');
    buildLeaderboardChart(response.allTimeAverage.labels, response.allTimeAverage.dataPoints, 'Average Score', 'all-time-average');

    buildLeaderboardChart(response.thirtyDaysPoints.labels, response.thirtyDaysPoints.dataPoints, 'Total Points', 'thirty-days-points');
    buildLeaderboardChart(response.thirtyDaysGames.labels, response.thirtyDaysGames.dataPoints, 'Games Played', 'thirty-days-games');
    buildLeaderboardChart(response.thirtyDaysAverage.labels, response.thirtyDaysAverage.dataPoints, 'Average Score', 'thirty-days-average');

    buildHistogramChart(response.pointsHistogram.labels, response.pointsHistogram.dataPoints, '', 'points-histogram');
}


window.onload = function() {
    $.ajax(window.location.href + '/data').done(renderCharts);
}