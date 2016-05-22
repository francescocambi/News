/**
 * Created by Francesco on 12/10/15.
 */
angular.module("NewsApp")
    .controller("FrontPagesListCtrl", function ($scope, $http, SERVER_URL, loadingSpinner) {

        //Initialize newspapers list and filter object
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/newspapers")
            .then(function (response) {
                $scope.newspapers = response.data;

                $scope.npfilter = {};
                angular.forEach($scope.newspapers, function (object, index) {
                    $scope.npfilter[object] = true;
                });
            })
            .finally(function () {loadingSpinner.end();});

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/front-pages/").success(function (data) {
            $scope.frontpages = [];
            data.forEach(function (item) {
                $scope.frontpages.push({
                    id: item[0],
                    timestamp: item[1],
                    newspaper: item[2]
                });
            })
        }).finally(function () { loadingSpinner.end(); });

    })
    .controller("FrontPageDetailsCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $routeParams) {
        loadingSpinner.begin();
        $http.get(SERVER_URL+"/front-pages/"+$routeParams['id'])
            .success(function (fp) {
                $scope.frontpage = fp;
            }).finally(function () {loadingSpinner.end();});
    });