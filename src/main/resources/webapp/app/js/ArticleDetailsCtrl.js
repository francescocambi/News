/**
 * Created by Francesco on 08/10/15.
 */

angular.module("NewsApp")
    .controller("ArticleDetailsCtrl", function ($scope, $http, SERVER_URL, $routeParams, loadingSpinner) {

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/articles/"+$routeParams['id'])
            .then(function (response) {
                $scope.currentArticle = response.data;
            }).finally(function () { loadingSpinner.end(); });

    });
