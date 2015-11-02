/**
 * Created by Francesco on 19/10/15.
 */
angular.module("NewsApp")
    .controller("NewsMatchListCtrl", function ($scope, $http, loadingSpinner, SERVER_URL, $routeParams, $location) {

        loadingSpinner.begin(2);
        $http.get(SERVER_URL+"/articles/"+$routeParams['id'])
            .then(function (response) {
                $scope.currentArticle = response.data;
            }).finally(function () { loadingSpinner.end(); });
        $http.get(SERVER_URL+"/news/match-article/"+$routeParams['id'])
            .then(function (response) {
                $scope.newsList = response.data;
                $scope.similarityKey = Object.keys(response.data[0].similarities)[0];
            }).finally(function () { loadingSpinner.end(); });


        $scope.matchArticleWithNews = function (articleId, newsId) {
            loadingSpinner.begin();
            $http({
                url: SERVER_URL+"/matcharticles/match",
                method: "POST",
                data: {'articleId': articleId, 'newsId': newsId},
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(function (response) {
                $location.path('/articles/list');
            }).finally(function () {loadingSpinner.end() });
        };

        $scope.noMatch = function (articleId) {
            $scope.matchArticleWithNews(articleId, 0);
        };

    });