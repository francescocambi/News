/**
 * Created by Francesco on 11/12/15.
 */

angular.module("NewsApp")
    .controller("ClusteringPerformanceTaskCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $location, $timeout) {

        $scope.taskConfig = {
            metricName: 'cosine',
            start: 0.47,
            step: 0.01,
            limit: 1,
            noiseWordsFilter: true,
            stemming: true,
            tfidf: true,
            keywordExtraction: 'capitals',
            matcherName: 'highest_mean_over_threshold'
        };

        function updateTaskList() {
            loadingSpinner.begin();
            $http.get(SERVER_URL + "/clustering-performance/")
                .then(function (response) {
                    $scope.taskList = response.data;

                    if ($location.path() == "/clustering-performance") {
                        $timeout(updateTaskList, 2000);
                    }
                }, function (error) {
                    console.log(error);
                }).finally(function () {
                loadingSpinner.end();
            });
        }
        updateTaskList();

        $scope.createAndStartTask = function (taskConfig) {
            var url = SERVER_URL+"/clustering-performance/start?";
            angular.forEach(taskConfig, function (value, key) {
                url += key+"="+value+"&";
            });

            loadingSpinner.begin();
            $http.get(url)
                .then(function (response) {
                    if (response.status != 201) {
                        console.log("Error", response.data);
                    }
                }).finally(function () { loadingSpinner.end(); });
        }

    });