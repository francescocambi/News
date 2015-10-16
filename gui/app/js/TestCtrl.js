/**
 * Created by Francesco on 14/10/15.
 */
angular.module("NewsApp")
.controller("TestCtrl", function ($scope, SERVER_URL, $http, loadingSpinner) {

        $scope.charts = [];

        $http.get(SERVER_URL+"/front-pages/test").success(function (data) {
            $scope.data = data;
            $scope.charts = [];
            for (var newspaper in data) {
                $scope.charts.push(generateChart(newspaper, data[newspaper]));
            }
            //console.log($scope.charts[0]);
        });

        $scope.chartCols = [
            {
                'id': 'time',
                'label': 'Time',
                'type': 'datetime'
            },
            {
                'id': 'variation',
                'label': 'Variation',
                'type': 'number'
            }
        ];

        function generateChart(name, o) {
            console.log(o);
            chart = {};
            chart.type = "AreaChart";
            chart.displayed = true;
            chart.data = {};
            chart.data.cols = $scope.chartCols;
            chart.data.rows = [];
            chart.options = {};
            chart.options.title = name;
            chart.options.displayExactValues = true;
            chart.options.vAxis = {};
            chart.options.hAxis = {};
            chart.options.vAxis.title = "Variability";
            chart.options.hAxis.title = "Timestamp";
            var keys = [];
            for (var k in o) { keys.push(k); }
            keys.sort(function (a,b) { return (new Date(a)) - (new Date(b)); });
            //console.log(keys);
            angular.forEach(keys, function (time) {
                chart.data.rows.push({
                    'c': [ {'v': new Date(time)}, {'v': o[time]} ]
                });
            });
            return chart;
        }

    });