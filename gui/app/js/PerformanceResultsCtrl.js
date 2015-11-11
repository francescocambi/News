/**
 * Created by Francesco on 10/11/15.
 */

angular.module("NewsApp")
    .controller("PerformanceResultsCtrl", function ($scope, $http, SERVER_URL,
                                                    loadingSpinner, $routeParams, $filter) {
        $scope.taskId = $routeParams['id'];
        $scope.chartSeries = ["Threshold"];

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/threshold-performance/results/"+$routeParams['id'])
            .then(function (response) {

                $scope.result = response.data;
                $scope.chartLabels = [];
                $scope.chartData = [ [] ];
                $scope.result.bestThreshold = 0.0;
                $scope.result.bestOkPercent = 0.0;
                var max = 0;

                //Prepare data for graph
                angular.forEach($scope.result.thresholdsResults, function (value, key) {

                    $scope.chartLabels.push($filter('number')(key, 3));
                    $scope.chartData[0].push($filter('number')(value/$scope.result.itemCount*100, 2));

                    //Compute best threshold
                    if (value > max) {
                        max = value;
                        $scope.result.bestThreshold = key;
                    }

                });

                $scope.result.bestOkPercent = (max/$scope.result.itemCount)*100;

            })
            .finally(function () {loadingSpinner.end();});



    });