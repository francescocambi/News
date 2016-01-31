/**
 * Created by Francesco on 05/11/15.
 */
angular.module("NewsApp")
    .controller("articlesDownloadCtrl", function ($scope, $http, SERVER_URL, $timeout) {
        $scope.progress = 0;
        $scope.downloadActive = false;
        $http.get(SERVER_URL+"/crawlers/status").then(function (response) {
            if (response.data) {
                $scope.downloadActive = true;
                $scope.updateProgress();
            }
        });

        $scope.beginDownload = function () {
            $http.get(SERVER_URL+"/crawlers/start").success(function (response) {
                console.log("Start response: "+response);
                $scope.downloadActive = response;
                if ($scope.downloadActive) {
                    $scope.updateProgress();
                }
            });
        };

        $scope.updateProgress = function () {
            $http.get(SERVER_URL+"/crawlers/progress").then(function (response) {
                var progress = response.data;
                console.log("Progress response: "+progress);
                $scope.progress = progress;
                $scope.downloadActive = ($scope.progress < 99.9);
            }, function (response) {
                console.log(response);
                $scope.downloadActive = false;
            });

            if ($scope.downloadActive)
                $timeout($scope.updateProgress, 1000);
        };

    });