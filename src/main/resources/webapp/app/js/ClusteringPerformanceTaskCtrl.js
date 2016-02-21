/**
 * Created by Francesco on 11/12/15.
 */

angular.module("NewsApp")
    .controller("ClusteringPerformanceTaskCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $location, $timeout) {

        $scope.taskConfig = {
            metricName: 'cosine',
            threshold: 0.47,
            noiseWordsFilter: true,
            stemming: true,
            tfidf: true,
            keywordExtraction: 'capitals',
            matcherName: 'highest_mean_over_threshold',
            testSet: 0.3,
            newspapers: ["LA_REPUBBLICA", "LA_STAMPA", "CORRIERE_DELLA_SERA", "ANSA", "ADNKRONOS", "IL_GIORNALE"]
        };

        $scope.toggleNewspaper = function (newspaper) {
            var index = $scope.taskConfig.newspapers.indexOf(newspaper);
            if (index > -1)
                $scope.taskConfig.newspapers.splice(index, 1);
            else
                $scope.taskConfig.newspapers.push(newspaper);
            //console.log($scope.taskConfig.newspapers);
        }

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