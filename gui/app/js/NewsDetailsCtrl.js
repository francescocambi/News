/**
 * Created by Francesco on 06/11/15.
 */
angular.module("NewsApp")
    .controller("NewsDetailsCtrl", function ($scope, $http, $routeParams, loadingSpinner, SERVER_URL) {

        loadingSpinner.begin(2);
        $http.get(SERVER_URL+"/news/"+$routeParams['id'])
            .then(function (response) {
                $scope.news = response.data
            }).finally( function () {loadingSpinner.end();});
        $http.get(SERVER_URL+"/articles/?newsId="+$routeParams['id'])
            .then(function (response) {
                $scope.articles = response.data;
            }).finally( function () { loadingSpinner.end(); });

    });