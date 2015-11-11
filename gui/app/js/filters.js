/**
 * Created by Francesco on 19/10/15.
 */
angular.module("NewsApp")
    .filter("toProgressColor", function () {
        return function (val) {
            if (val < 0.25) return 'progress-bar-danger';
            if (val < 0.47) return 'progress-bar-warning';
            else return 'progress-bar-success';
        }
    })
    .filter("NewspaperName", function () {
        return function (name) {
            if (name) {
                return name.split("_").map(function (item) {
                    return item.charAt(0).toUpperCase() + item.slice(1).toLowerCase();
                }).join(" ");
            } else {
                return name;
            }
        }
    })
    .filter("taskStatus", function () {
        return function (status) {
            if (status) {
                return status.charAt(0) + status.slice(1).toLowerCase();
            } else
                return status;
        }
    });

