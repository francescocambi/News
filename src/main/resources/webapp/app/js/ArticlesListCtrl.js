/**
 * Created by Francesco on 14/11/15.
 */
angular.module("NewsApp")
    .controller("ArticlesListCtrl", function ($scope, $rootScope, $http, SERVER_URL, loadingSpinner, $location) {

        $scope.npfilter = {
            repubblica: true,
            lastampa: true,
            corriere: true,
            ansa: true,
            adnkronos: true,
            giornale: true
        };

        $scope.onlyNotMatched = $rootScope.articleFilterOnlyNotMatched;

        $scope.sortingCol = 0;

        $scope.refreshArticlesList = function () {
            loadingSpinner.begin();
            $http.get(SERVER_URL+"/articles/?clustering=manual")
                .then(function (response) {
                    $scope.articles = response.data;
                    $rootScope.cachedArticlesList = response.data;
                })
                .finally(function () { loadingSpinner.end() });
        }

        $scope.onlyNotMatchedFilter = function (article) {
            return (!$scope.onlyNotMatched || article.news.manual == undefined);
        }

        $scope.onlyNotMatchedFilterChanged = function () {
            $rootScope.articleFilterOnlyNotMatched = $scope.onlyNotMatched;
        }

        $scope.matchingNewsFor = function (article) {
            $rootScope.articleList_selectedArticle = article;
            $location.path('/articles/match/'+article.id);
        }

        //Loads cached articles list if present
        if ($rootScope.cachedArticlesList) {
            $scope.articles = $rootScope.cachedArticlesList;
        } else {
            //Otherwise ask server
            $scope.refreshArticlesList();
        }

        //$scope.deleteArticle = function (article) {
        //    if (!confirm("You really want to delete this article?")) return;
        //    var i = $scope.articles.indexOf(article);
        //    if (i < 0) return;
        //    loadingSpinner.begin();
        //    $http({
        //        url: SERVER_URL+"/articles/"+article.id,
        //        method: "DELETE"
        //    }).then(function (response) {
        //        $scope.articles.splice(i, 1);
        //    }).finally(function () { loadingSpinner.end(); });
        //}


    });