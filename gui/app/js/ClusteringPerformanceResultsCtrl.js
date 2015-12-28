/**
 * Created by Francesco on 11/12/15.
 */
angular.module("NewsApp")
    .controller("ClusteringPerformanceResultsCtrl", function ($scope, $http, SERVER_URL,
                                                                loadingSpinner, $routeParams, $filter) {

        $scope.taskId = $routeParams['id'];


        loadingSpinner.begin();
        $http.get(SERVER_URL+"/clustering-performance/results/"+$routeParams['id'])
            .then(function (response) {

                $scope.trs = response.data.trainingSet;
                $scope.tss = response.data.testSet;

            }).finally(function () {loadingSpinner.end();});

    });