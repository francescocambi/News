/**
 * Created by Francesco on 28/09/15.
 */

angular.module("NewsApp", ["ngRoute", "ngCookies", "googlechart"])
    .constant("SERVER_URL", "")
    .factory("loadingSpinner", function () {
        return {
            activeCount: 0,
            begin: function (a) {
                if (a == undefined) a = 1;
                this.activeCount += a;
                if (this.activeCount == a) {
                    document.getElementById("loading-spinner").style.display = 'block';
                }
            },
            end: function () {
                this.activeCount--;
                if (this.activeCount == 0) {
                    document.getElementById("loading-spinner").style.display = 'none';
                }
            }
        }
    })
    .factory("forbiddenRequestsObserver", function ($q, $rootScope, $location) {
        return {
            'responseError': function (errorResponse) {
                if (errorResponse.status == 403) {
                    console.log("403 - Session Expired");
                    $rootScope.sessionErrorMessage = "Session expired or you can't access this area.";
                    $location.path("/login");
                }
                return $q.reject(errorResponse);
            }
        };
    })
    .filter("to_trusted", ['$sce', function ($sce) {
        return function (text) {
            return $sce.trustAsHtml(text);
        };
    }])
    .config(function ($httpProvider, $routeProvider) {

        $routeProvider.when("/login", {
            templateUrl: "partials/login.html",
            controller: "loginCtrl"
        });

        $routeProvider.when("/logout", {
            templateUrl: "partials/logout.html",
            controller: "logoutCtrl"
        });

        $routeProvider.when("/home", {
            templateUrl: "partials/home.html",
            controller: "homeCtrl"
        });

        $routeProvider.when("/articles/download", {
            templateUrl: "partials/articles.html",
            controller: "articlesDownloadCtrl"
        });

        $routeProvider.when("/articles/list", {
            templateUrl: "partials/articlesList.html",
            controller: "articlesCtrl"
        });

        $routeProvider.when("/articles/:id", {
            templateUrl: "partials/articleDetails.html",
            controller: "articlesCtrl"
        });

        $routeProvider.when("/articles/match/:id", {
            templateUrl: "partials/newsMatchList.html",
            controller: "NewsMatchListCtrl"
        });

        $routeProvider.when("/articles/match/details/:id-:matchid", {
            templateUrl: "partials/articlesMatchDetails.html",
            controller: "articlesCtrl"
        });

        $routeProvider.when("/front-pages", {
            templateUrl: "partials/frontPagesList.html",
            controller: "FrontPagesListCtrl"
        });

        $routeProvider.when("/front-pages/:id", {
            templateUrl: "partials/frontPageDetails.html",
            controller: "FrontPageDetailsCtrl"
        });

        $routeProvider.when("/test", {
            templateUrl: "partials/test.html",
            controller: "TestCtrl"
        });

        $routeProvider.otherwise({
            templateUrl: "partials/home.html",
            controller: "homeCtrl"
        });

        $httpProvider.interceptors.push('forbiddenRequestsObserver');

    })
    .run(function ($rootScope, $location, $cookieStore, $http) {
        // retrieve sessionid from cookies
        $rootScope.serverSessionId = $cookieStore.get('newsApp_sessionId');
        //console.log("Retrieved from cookies "+$rootScope.serverSessionId);
        if ($rootScope.serverSessionId != undefined) {
            //Attach it to all http requests
            $http.defaults.headers.common['Authorization'] = $rootScope.serverSessionId;
        } else {
            if ($location.path() != "/login") {
                $location.path("/login");
            }
        }
        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            //redirect to login page if user is not logged in
            if ($rootScope.serverSessionId == undefined) {
                $location.path('/login');
            }
        });
    })
    .controller("homeCtrl", function ($scope, $http, SERVER_URL, loadingSpinner) {
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/articles/stats")
            .success(function (data) {
                $scope.serverOnline = true;
                $scope.stats = data;
                $scope.articlesChart = {
                    type: "PieChart",
                    options: {
                        "title": "Articles on DB",
                        is3D: true
                    },
                    data: {
                        "cols": [
                            {id: "d", label: "Description", type: "string"},
                            {id: "v", label: "Value", type: "number"}
                        ], "rows": [
                            {c: [{v: "Matched"}, {v: $scope.stats.matchedArticlesCount}]},
                            {c: [{v: "Not Matched"}, {v: $scope.stats.notMatchedArticlesCount}]}
                        ]
                    }
                };
            }).finally(loadingSpinner.end());

    })
    .controller("articlesDownloadCtrl", function ($scope, $http, SERVER_URL, $timeout) {
        $scope.progress = 0;
        $scope.downloadActive = false;
        $http.get(SERVER_URL+"/crawlers/status").success(function (status) {
            if (status) {
                $scope.downloadActive = true;
                $scope.updateProgress();
            }
        });

        $scope.beginDownload = function () {
            $http.get(SERVER_URL+"/crawlers/start").success(function (response) {
                console.log("Start response: "+response);
                $scope.downloadActive = response;
                if ($scope.downloadActive) {
                    $scope.updateProgress();
                }
            });
        };

        $scope.updateProgress = function () {
            $http.get(SERVER_URL+"/crawlers/progress").then(function (response) {
                var progress = response.data;
                console.log("Progress response: "+progress);
                $scope.progress = progress;
                $scope.downloadActive = ($scope.progress < 99.9);
            }, function (response) {
                console.log(response);
                $scope.downloadActive = false;
            });

            if ($scope.downloadActive)
                $timeout($scope.updateProgress, 1000);
        };

    });