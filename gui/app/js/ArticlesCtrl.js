/**
 * Created by Francesco on 08/10/15.
 */

angular.module("NewsApp")
    .controller("articlesCtrl", function ($scope, $http, SERVER_URL, $routeParams, $location, loadingSpinner) {

        if ($location.path().indexOf("/articles/list") == 0) {
            $scope.onlyNotMatched = true;

            loadingSpinner.begin();
            $http.get(SERVER_URL+"/articles/")
                .success(function (response) {
                    $scope.articles = [];
                    response.forEach(function (article) {
                        $scope.articles.push({
                            id: article[0],
                            title: article[1],
                            source: article[2],
                            news: article[3]
                        });
                    });
                })
                .finally(function () { loadingSpinner.end() });
        } else if ($location.path().indexOf("/articles/match/details/") == 0) {
            loadingSpinner.begin();
            $http.get(SERVER_URL+"/matcharticles/matching/"+$routeParams['id']+"-"+$routeParams['matchid'])
                .success(function (matchingArticle) {
                    $scope.matchingArticle = matchingArticle;
                    $http.get(SERVER_URL + "/articles/" + $routeParams['id'])
                        .success(function (article) {
                            $scope.currentArticle = article;
                            $scope.prepareText();
                        });
                }).finally(function () { loadingSpinner.end() });
        } else if ($location.path().indexOf("/articles/match/") == 0) {
            loadingSpinner.begin(2);
            $http.get(SERVER_URL+"/articles/"+$routeParams['id'])
                .success(function (article) {
                    $scope.currentArticle = article;
                }).finally(function() { loadingSpinner.end(); });
            $http.get(SERVER_URL + "/matcharticles/matching/" + $routeParams['id'])
                .success(function (data) {
                    $scope.matchingArticles = data;
                }).finally(function() { loadingSpinner.end(); });
        } else if ($location.path().indexOf("/articles/") == 0) {
            $http.get(SERVER_URL+"/articles/"+$routeParams['id'])
                .success(function (article) {
                    $scope.currentArticle = article;
                });
            $scope.sortingCol = 0;
        }

        $scope.prepareText = function () {
            var text = $scope.currentArticle.title+" "+$scope.currentArticle.description+" "+$scope.currentArticle.body;

            $scope.highlightedTitle = $scope.highlightTextSimilarities($scope.matchingArticle.article.title, text, 'green');
            $scope.highlightedDescription = $scope.highlightTextSimilarities($scope.matchingArticle.article.description, text, 'green');
            $scope.highlightedBody = $scope.highlightTextSimilarities($scope.matchingArticle.article.body, text, 'green');
        }

        /*
         Highlight with green color words that appear both in text and source.
         Highlighting will be done on text argument.
         */
        $scope.highlightTextSimilarities = function (text, source, greenClass) {
            if (!text) return;
            if (!source) return text;
            var splittedText = text.split(/[\s]+/g);
            var splittedSource = source.split(/[\s]+/g);
            var result = "";

            splittedText.forEach(function (wordA) {
                var found = false;
                var word = "";
                for (var i=0; i<splittedSource.length && !found; i++) {
                    var a = wordA.toLowerCase().replace(/[^\w\d]/ig, "");
                    var b = splittedSource[i].toLowerCase().replace(/[^\w\d]/ig, "");
                    if (a.length > 0 && a == b) {
                        word += "<span class=\"" + greenClass + "\">" + wordA + "</span>";
                        found = true;
                    }
                }
                if (word == "") {
                    word = wordA;
                }
                result += word+" ";
            });

            return result;
        }

        $scope.deleteArticle = function (article) {
            if (!confirm("You really want to delete this article?")) return;
            var i = $scope.articles.indexOf(article);
            if (i < 0) return;
            loadingSpinner.begin();
            $http({
                url: SERVER_URL+"/articles/"+article.id,
                method: "DELETE"
            }).success(function (data) {
                $scope.articles.splice(i, 1);
            }).finally(function () { loadingSpinner.end(); });
        }

        $scope.onlyNotMatchedFilter = function (article) {
            return (!$scope.onlyNotMatched || article.news == null);
        }

    });
