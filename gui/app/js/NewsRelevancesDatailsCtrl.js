angular.module("NewsApp")
    .controller("NewsRelevancesDetailsCtrl", function ($scope, $http, SERVER_URL, loadingSpinner, $routeParams) {

        $scope.chartOptions = {
            scaleShowGridLines: false,
            pointDot: false,
            pointDotRadius: 3,
            pointHitDetectionRadius: 5,
            scaleShowLabels: true,
            scaleFontSize: 0
        };

        loadingSpinner.begin();
        $http.get(SERVER_URL+"/news-relevances/"+$routeParams['id']+"?clustering="+$routeParams['clustering'])
            .then(function (response) {
                $scope.news = response.data.news;
                $scope.chartData = prepare(response.data.relevances);
            }).finally(function () { loadingSpinner.end()});

        function prepare(data) {
            //data is an object of this form
            //{ ts: relevance, ts: relevance, ...}
            var chartData = {
                labels: [],
                values: [ [] ],
                series: ["Relevance"]
            };
            var arr = [];
            angular.forEach(data, function (value, key) {
                arr.push({
                    timestamp: key,
                    relevance: value
                });
            });
            var sorted = arr.sort(function (a,b) {
                return ((a.timestamp < b.timestamp) ? -1 : ((a.timestamp == b.timestamp) ? 0 : 1));
            });
            //Fills both arrays
            angular.forEach(sorted, function (o) {
                chartData.labels.push(new Date(Number(o.timestamp)));
                chartData.values[0].push([o.relevance]);
            });

            console.log(chartData);

            return chartData;
        }

    });