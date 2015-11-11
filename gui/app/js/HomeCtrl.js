/**
 * Created by Francesco on 10/11/15.
 */

angular.module("NewsApp")
    .controller("homeCtrl", function ($scope, $http, SERVER_URL, loadingSpinner) {
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/articles/stats")
            .then(function (response) {
                $scope.serverOnline = true;
                $scope.stats = response.data;

                $scope.pieLabels = ["Matched", "Not Matched"];
                $scope.pieData = [$scope.stats.matchedArticlesCount, $scope.stats.notMatchedArticlesCount];
            }).finally(loadingSpinner.end());

    });