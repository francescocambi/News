/**
 * Created by Francesco on 11/11/15.
 */

angular.module("NewsApp")
    .controller("ClusteringTaskCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $location, $timeout) {

        $scope.taskConfig = {
            metricName: "cosine",
            noiseWordsFilter: true,
            stemming: true,
            tfidf: true,
            keywordExtraction: "capitals",
            threshold: 0.47,
            matcherName: "highest_mean_over_threshold",
            clusteringName: undefined,
            articlesFrom: undefined,
            articlesTo: undefined
        };

        function updateTaskList() {
            loadingSpinner.begin();
            $http.get(SERVER_URL + "/clustering/")
                .then(function (response) {
                    $scope.taskList = response.data;

                    if ($location.path() == "/clustering") {
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
            var url = SERVER_URL+"/clustering/start?";
            angular.forEach(taskConfig, function (value, key) {
                if (value != undefined && value != null && value != "" )
                    url += key+"="+value+"&";
            });

            loadingSpinner.begin();
            $http.get(url)
                .then(function (response) {
                    if (response.status != 201) {
                        console.log("Error", response.data);
                    }
                }, function (error) { console.log(error); })
                .finally(function () { loadingSpinner.end(); });
        };

        $scope.cancelTask = function (task) {

            loadingSpinner.begin();
            $http.get(SERVER_URL+"/clustering/cancel/"+task.taskId)
                .then(function (response) {
                    $scope.taskList.splice($scope.taskList.indexOf(task), 1);
                }, function (error) {
                    console.log(error);
                }).finally(function () { loadingSpinner.end(); });

        }

    });
