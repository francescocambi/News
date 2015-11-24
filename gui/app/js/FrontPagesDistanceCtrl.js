/**
 * Created by Francesco on 17/11/15.
 */
angular.module("NewsApp")
    .controller("FrontPagesDistanceCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $filter) {
        $scope.params = {
            timeStep: 1,
            timeStepUm: "h"
        };

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/clusterings")
            .then(function (response) {
                $scope.clusterings = response.data
            })
            .finally(function () {loadingSpinner.end();});

        function prepareData(data) {

            var newspapers = {};
            var coloridx = 0;

            angular.forEach(data, function (points, timestamp) {

                angular.forEach(points, function (point, newspaper) {

                    if (newspapers[newspaper] == undefined) {
                        newspapers[newspaper] = {
                            label: $filter('NewspaperName')(newspaper),
                            pointColor: Chart.defaults.global.colours[coloridx++],
                            data: []
                        };
                    }

                    newspapers[newspaper].data.push({ x: point[0], y: point[1], timestamp: timestamp });
                });

            });

            result = [];
            $scope.labels = [];
            angular.forEach(newspapers, function (value, key) {
                $scope.labels.push($filter('NewspaperName')(key));
                result.push(value);
            });

            $scope.scatterChart = new Chart(document.getElementById("scatter").getContext("2d")).Scatter(result, {
                datasetStroke: false,
                pointDotRadius: 4,
                pointHitDetectionRadius: 4,
                tooltipTemplate: "<%=datasetLabel%>",
                multiTooltipTemplate: "<%=datasetLabel%>",
                legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li style=\"width: 100%;\"><span style=\"background-color:<%=datasets[i].pointColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>"
            });

            $('#legend').empty().append($($scope.scatterChart.generateLegend()));

            return newspapers;
        }

        $scope.generateChart = function (params) {
            var url = SERVER_URL+"/front-pages/newspapers-distance?";

            angular.forEach(params, function (value, key) {
                if (value && value != "")
                    url += key+"="+value+"&"
            });

            loadingSpinner.begin();
            $http.get(url)
                .then(function (response) {
                    $scope.newspapers = prepareData(response.data);
                }).finally(function () { loadingSpinner.end(); });
        }



    });