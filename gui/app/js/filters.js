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
    .filter("to_trusted", ['$sce', function ($sce) {
        return function (text) {
            return $sce.trustAsHtml(text);
        };
    }])
    .filter("taskStatus", function () {
        return function (status) {
            if (status) {
                return status.charAt(0) + status.slice(1).toLowerCase();
            } else
                return status;
        }
    })
    .filter('duration', function() {
        return function(millseconds) {
            var seconds = Math.round(millseconds / 1000);
            var days = Math.round(seconds / 86400);
            var hours = Math.round((seconds % 86400) / 3600);
            var minutes = Math.round(((seconds % 86400) % 3600) / 60);
            if (minutes == 60) {
                minutes = 0; hours++;
            }
            var timeString = '';
            if(days > 0) timeString += (days > 1) ? (days + " days ") : (days + " day ");
            if(hours > 0) timeString += (hours > 1) ? (hours + " hours ") : (hours + " hour ");
            if(minutes > 0) timeString += (minutes > 1) ? (minutes + " minutes ") : (minutes + " minute ");
            return timeString;
        }
    });

