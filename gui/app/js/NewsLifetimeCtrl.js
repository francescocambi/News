angular.module("NewsApp")
    .controller("NewsLifetimeCtrl", function ($scope, $rootScope, $http, SERVER_URL, loadingSpinner) {

        $scope.chartSeries = ["Lifetime"];

        $scope.chartOptions = {
            scaleShowGridLines: false,
            pointDot: false,
            pointDotRadius: 3,
            pointHitDetectionRadius: 5,
            scaleShowLabels: true,
            scaleFontSize: 0
        };

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/clusterings")
            .then(function (response) {
                $scope.clusterings = response.data;

                if ($rootScope.selectedClustering == undefined)
                    $scope.selectedClustering = 'manual';
                else
                    $scope.selectedClustering = $rootScope.selectedClustering;
                $scope.clusteringChange();

            }).finally(function () {loadingSpinner.end();});

        $scope.clusteringChange = function () {
            $rootScope.selectedClustering = $scope.selectedClustering;
            loadingSpinner.begin();
            $http.get(SERVER_URL+"/news-lifetime?clustering="+$scope.selectedClustering)
                .then(function (response) {
                    $scope.data = response.data;
                    $scope.chartData = prepare(response.data);
                }).finally(function () {
                loadingSpinner.end()
            });
        };

        function prepare(data) {
            var chartData = {
                labels: [],
                values: [ [] ]
            };
            angular.forEach(data.distribution, function (point) {
                chartData.labels.push(point.label);
                chartData.values[0].push(point.value);
            });
            console.log(chartData);
            return chartData;
        }

    });