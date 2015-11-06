/**
 * Created by Francesco on 05/11/15.
 */
angular.module("NewsApp")
    .controller("NewsListCtrl", function ($scope, $http, loadingSpinner) {

        loadingSpinner.begin();
        $http.get("/news").then(function (response) {
            $scope.news = response.data;
        }).finally(function () {loadingSpinner.end()});

    });