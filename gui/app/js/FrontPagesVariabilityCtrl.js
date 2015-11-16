/**
 * Created by Francesco on 14/10/15.
 */
angular.module("NewsApp")
.controller("FrontPagesVariabilityCtrl", function ($scope, SERVER_URL, $http, loadingSpinner) {

        $scope.chartSeries = ['Manual', 'Automatic'];
        $scope.chartOptions = {
            scaleShowGridLines: false,
            pointDot: false,
            pointDotRadius: 3,
            pointHitDetectionRadius: 5,
            scaleShowLabels: true,
            scaleFontSize: 0
        };

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/front-pages/test")
            .then(function (response) {
                $scope.newspapers = response.data;
                console.log($scope.newspapers);
            }).finally(function () { loadingSpinner.end(); });

        $scope.prepare = function (newspaper) {
            newspaper.chartData = [newspaper.manual, newspaper.auto_test];
            newspaper.labels = [];
            angular.forEach(newspaper.dates, function (value) {
                newspaper.labels.push(new Date(value));
            });
            return newspaper;
        }

    });