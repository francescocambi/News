/**
 * Created by Francesco on 14/10/15.
 */
angular.module("NewsApp")
    .controller("FrontPagesVariabilityCtrl", function ($scope, SERVER_URL, $http, loadingSpinner) {

        $scope.chartOptions = {
            scaleShowGridLines: false,
            pointDot: false,
            pointDotRadius: 3,
            pointHitDetectionRadius: 5,
            scaleShowLabels: true,
            scaleFontSize: 0
        };

        $scope.params = {};

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/clusterings")
            .then(function (response) {
                $scope.clusterings = response.data;
            }).finally(function () {loadingSpinner.end();});

        $scope.generateChart = function (params) {

            var url = SERVER_URL+"/front-pages/variability?";

            angular.forEach(params, function (value, key) {
                if (value && value != "")
                    url += key+"="+value+"&";
            });

            $scope.clusteringA = params.clusteringA;
            $scope.clusteringB = params.clusteringB;

            loadingSpinner.begin();
            $http.get(url)
                .then(function (response) {
                    // 1 chart for each newspaper
                    $scope.charts = response.data;

                    angular.forEach($scope.charts, function (data, name) {
                        $scope.charts[name] = $scope.prepare(data);
                    });

                }).finally(function () {loadingSpinner.end();});


        };

        $scope.prepare = function (chart) {
            $scope.chartSeries = [$scope.clusteringA];
            if ($scope.clusteringB)
                $scope.chartSeries.push($scope.clusteringB);
            chart.chartData = [chart[$scope.clusteringA]];
            if ($scope.clusteringB)
                chart.chartData.push(chart[$scope.clusteringB]);
            chart.labels = [];
            angular.forEach(chart.dates, function (value) {
                chart.labels.push(new Date(value));
            });
            return chart;
        }

    });