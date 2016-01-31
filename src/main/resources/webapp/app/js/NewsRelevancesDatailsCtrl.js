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
                $scope.relevances = mapToArrayOfObjects(response.data.relevances);
                if ($scope.relevances.length > 1)
                    $scope.chartData = prepare($scope.relevances);
            }).finally(function () { loadingSpinner.end()});

        function mapToArrayOfObjects(data) {
            var arr = [];
            angular.forEach(data, function (value, key) {
                arr.push({
                    timestamp: key,
                    relevance: value
                });
            });
            return arr;
        }

        function prepare(data) {
            //data is an object of this form
            //{ ts: relevance, ts: relevance, ...}
            var chartData = {
                labels: [],
                values: [ [] ],
                series: ["Relevance"]
            };
            var sorted = data.sort(function (a, b) {
                return ((a.timestamp < b.timestamp) ? -1 : ((a.timestamp == b.timestamp) ? 0 : 1));
            });
            //Fills both arrays
            angular.forEach(sorted, function (o) {
                chartData.labels.push(new Date(Number(o.timestamp)));
                chartData.values[0].push([o.relevance]);
            });

            //console.log(chartData);

            return chartData;
        }

    });