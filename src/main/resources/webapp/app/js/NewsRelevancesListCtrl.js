angular.module("NewsApp")
    .controller("NewsRelevancesListCtrl", function ($rootScope, $scope, $http, loadingSpinner, SERVER_URL) {

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
            $http.get(SERVER_URL+"/news-relevances?clustering="+$scope.selectedClustering)
                .then(function (response) {
                    $scope.news = response.data;
                }).finally(function () {
                loadingSpinner.end()
            });
        };

    });