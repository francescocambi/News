/**
 * Created by Francesco on 08/10/15.
 */
angular.module("NewsApp")
.controller("logoutCtrl", function ($cookieStore, $http, SERVER_URL, loadingSpinner) {
        loadingSpinner.begin();
        //Request Logout
        $http.post(SERVER_URL+"/security/logout", {
            sessionId: $cookieStore.get("newsApp_sessionId")
        }).finally(
            function () {loadingSpinner.end();}
        );

        $cookieStore.remove("newsApp_sessionId");
    });