/**
 * Created by Francesco on 28/09/15.
 */

angular.module("NewsApp", ["ngRoute", "ngCookies", "chart.js",
    "angularUtils.directives.dirPagination"])
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
            templateUrl: "partials/articlesDownload.html",
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

        $routeProvider.when("/news", {
            templateUrl: "partials/newsList.html",
            controller: "NewsListCtrl"
        });

        $routeProvider.when("/news/:id", {
            templateUrl: "partials/newsDetails.html",
            controller: "NewsDetailsCtrl"
        });

        $routeProvider.when("/threshold-performance/", {
            templateUrl: "partials/performanceTask.html",
            controller: "PerformanceTaskCtrl"
        });

        $routeProvider.when("/threshold-performance/:id", {
            templateUrl: "partials/performanceResults.html",
            controller: "PerformanceResultsCtrl"
        });

        $routeProvider.otherwise({
            templateUrl: "partials/home.html",
            controller: "homeCtrl"
        });

        $httpProvider.interceptors.push('forbiddenRequestsObserver');

    })
    .run(function ($rootScope, $location, $cookieStore, $http) {

        //Define default colours for charts
        Chart.defaults.global.colours = ["#3366CC","#DC3912","#FF9900","#109618","#4D5360","#949FB1","#990099"];

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
    });