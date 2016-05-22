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
            tfidfDictionary: null,
            language: null,
            keywordExtraction: 'capitals',
            matcherName: 'highest_mean_over_threshold',
            testSet: 0.3,
            newspapers: []
        };

        //Retrieve tfidf dictionaries
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/dictionaries")
            .then(function (response) {
                $scope.dictionaries = response.data;

                if ($scope.dictionaries.length > 0)
                    $scope.taskConfig.tfidfDictionary = $scope.dictionaries[0].description;
            })
            .finally(function () { loadingSpinner.end(); });

        //Retrieve languages
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/languages")
            .then(function (response) {
                $scope.languages = response.data;

                if ($scope.languages.length > 0)
                    $scope.taskConfig.language = $scope.languages[0];
            })
            .finally(function () { loadingSpinner.end(); });

        //Initialize newspapers list and filter object
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/newspapers")
            .then(function (response) {
                $scope.newspapers = response.data;

                $scope.taskConfig.newspapers = $scope.newspapers.slice();
            })
            .finally(function () {loadingSpinner.end();});

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
            //Check parameters
            if (!taskConfig.tfidfDictionary || taskConfig.tfidfDictionary == null || taskConfig.tfidfDictionary.length == 0) {
                alert("Please choose a TF-IDF Dictionary to use for clustering, or remove \"Use TF-IDF\" flag.");
                return;
            }
            if (!taskConfig.language || taskConfig.language == null || taskConfig.language.length == 0) {
                alert("Please select a language to use for stemming and noise words filtering.");
                return;
            }

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