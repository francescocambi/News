/**
 * Created by Francesco on 10/11/15.
 */

angular.module("NewsApp")
    .controller("PerformanceTaskCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $location, $timeout) {

        $scope.taskConfig = {
            metricName: "cosine",
            start: 0.1,
            step: 0.1,
            limit: 9,
            noiseWordsFilter: true,
            stemming: true,
            tfidf: true,
            keywordExtraction: "capitals"
        };

        function updateTaskList() {
            loadingSpinner.begin();
            $http.get(SERVER_URL + "/threshold-performance/")
                .then(function (response) {
                    $scope.taskList = response.data;

                    if ($location.path() == "/threshold-performance/") {
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
            var url = SERVER_URL+"/threshold-performance/start?";
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