/**
 * Created by Francesco on 05/11/15.
 */
angular.module("NewsApp")
    .controller("NewsListCtrl", function ($rootScope, $scope, $http, loadingSpinner, SERVER_URL) {

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
            $http.get(SERVER_URL+"/news?clustering="+$scope.selectedClustering)
                .then(function (response) {
                $scope.news = response.data;
            }).finally(function () {
                loadingSpinner.end()
            });
        };

        $scope.mergeNews = function () {

            //Find all selected news
            var url = SERVER_URL+"/news/merge?";
            var found = 0;
            angular.forEach($scope.news, function (value) {
                if (value.selected) {
                    found += 1;
                    url += "news=" + value.id + "&";
                }
            });

            if (found < 2) return;

            loadingSpinner.begin();
            $http.get(url).then(function (response) {
                $scope.clusteringChange();
            }, function (error) {
                console.log(error);
            }).finally(function () { loadingSpinner.end(); });

        }

    });