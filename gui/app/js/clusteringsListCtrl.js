/**
 * Created by Francesco on 25/01/16.
 */

angular.module("NewsApp")
    .controller('ClusteringsListCtrl', function ($scope, $http, SERVER_URL, loadingSpinner) {

        loadingSpinner.begin();
        $scope.clusterings = $http.get(SERVER_URL+"/clusterings")
            .then(function (response) {
                $scope.clusterings = response.data;
            }).finally(function () { loadingSpinner.end() });

        $scope.deleteClustering = function (clustering) {
            if (!confirm("You really want to delete this clustering?")) return;
            var i = $scope.clusterings.indexOf(clustering);
            if (i < 0) return;
            loadingSpinner.begin();
            $http({
                url: SERVER_URL+"/clusterings/"+clustering.name,
                method: "DELETE"
            }).then(function (response) {
                $scope.clusterings.splice(i, 1);
            }).finally(function () { loadingSpinner.end(); });
        }

    });