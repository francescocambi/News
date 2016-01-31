/**
 * Created by Francesco on 24/11/15.
 */
angular.module("NewsApp")
    .controller("IndexCtrl", function ($scope, $rootScope) {

        $scope.displayMessage = false;
        $scope.message = "";

        $rootScope.$watch(function () {
            return $rootScope.httpErrorMessage
        }, function (newVal) {
            $scope.displayMessage = newVal != undefined;
            $scope.message = newVal;
        });

    });