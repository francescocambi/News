/**
 * Created by Francesco on 07/10/15.
 */
angular.module("NewsApp")
.controller("loginCtrl", function ($rootScope, $scope, $http, $cookieStore, $location, SERVER_URL, loadingSpinner) {

        $scope.errorMessage = $rootScope.sessionErrorMessage;
        $rootScope.sessionErrorMessage = undefined;

        $scope.doLogin = function (username, password) {

            if (!username || !password) {
                $scope.errorMessage = "Please provide credentials.";
                return;
            }

            loadingSpinner.begin();
            $http.post(SERVER_URL+"/security", {
                username: username,
                password: password
            }).then(function (response) {
                console.log(response.data);
                $cookieStore.put("newsApp_sessionId", response.data.sessionId);
                $rootScope.serverSessionId = response.data.sessionId;
                $http.defaults.headers.common['Authorization'] = $rootScope.serverSessionId;
                $location.path("/home");
            }, function (response) {
                console.log(response);
                $scope.errorMessage = "Server error: "+response.status+" "+response.statusText;
            }).finally(function () {
                loadingSpinner.end();
            });

        };

    });