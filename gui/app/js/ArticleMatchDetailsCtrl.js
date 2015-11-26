/**
 * Created by Francesco on 14/11/15.
 */
angular.module("NewsApp")
    .controller("ArticleMatchDetailsCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $routeParams) {

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/matcharticles/matching/"+$routeParams['id']+"-"+$routeParams['matchid'])
            .then(function (response) {
                $scope.matchingArticle = response.data;
                $scope.similarityKey = Object.keys(response.data.similarities)[0];
                $http.get(SERVER_URL + "/articles/" + $routeParams['id'])
                    .then(function (response) {
                        $scope.currentArticle = response.data;
                        $scope.prepareText();
                    });
            }).finally(function () { loadingSpinner.end() });

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

            var stemmer = new Snowball('Italian');

            splittedText.forEach(function (wordA) {
                var found = false;
                var word = "";
                for (var i=0; i<splittedSource.length && !found; i++) {
                    var a = wordA.toLowerCase().replace(/[^\w\d]/ig, "");
                    stemmer.setCurrent(a);
                    stemmer.stem();
                    a = stemmer.getCurrent();
                    var b = splittedSource[i].toLowerCase().replace(/[^\w\d]/ig, "");
                    stemmer.setCurrent(b);
                    stemmer.stem();
                    b = stemmer.getCurrent();
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

    });