'use strict';

angular.module('linuxGraphApp').controller('HostCtrl', function($scope, $routeParams, Metrics) {
	$scope.loadAverage = [
	                      {
	                    	  key: '1 min.',
	                    	  values: []
	                      },
	                      {
	                    	  key: '5 min.',
	                    	  values: []
	                      },
	                      {
	                    	  key: '15 min.',
	                    	  values: []
	                      }
	                      ];
	$scope.ram = [ {
		key: 'Total',
		values: []
	}, {
		key: 'Used',
		values: []
	}];
	
	$scope.swap = [ {
		key: 'Total',
		values: []
	}, {
		key: 'Used',
		values: []
	}];
	
	$scope.timeTickFormat = function() {
		return function(d) {
			return d3.time.format('%X')(new Date(d));
		};
	};
	
	$scope.startTime = moment().subtract('minutes', 1).valueOf();
	
	$scope.getLoadAverage = function(startTime, endTime) {
		Metrics.getLoadAverage($routeParams.host, startTime, endTime).then(function(response) {
			var averages = [];
			averages.push($scope.loadAverage[0]);
			averages.push($scope.loadAverage[1]);
			averages.push($scope.loadAverage[2]);
			
			for(var i = 0; i < response.hits.hits.length; i++) {
				averages[0].values.push([
				                        response.hits.hits[i]._source.timestamp,
				                        response.hits.hits[i]._source.oneMinuteAverage
				                        ]);
				averages[1].values.push([
				                         response.hits.hits[i]._source.timestamp,
				                         response.hits.hits[i]._source.fiveMinuteAverage
				                         ]);
				averages[2].values.push([
				                            response.hits.hits[i]._source.timestamp,
				                            response.hits.hits[i]._source.fifteenMinuteAverage
				                            ]);
			}
			$scope.cpuCores = response.hits.hits[response.hits.hits.length - 1]._source.cpuCores;
			$scope.loadAverage = averages;
		});
	};
	
	$scope.getMemoryUsage = function(startTime, endTime) {
		Metrics.getMemoryUsage($routeParams.host, startTime, endTime).then(function(response) {
			var ram = [];
			ram.push($scope.ram[0]);
			ram.push($scope.ram[1]);
			
			var swap = [];
			swap.push($scope.swap[0]);
			swap.push($scope.swap[1]);
			
			for(var i = 0; i < response.hits.hits.length; i++) {
				ram[0].values.push([
				                        response.hits.hits[i]._source.timestamp,
				                        response.hits.hits[i]._source.totalMemory
				                        ]);
				ram[1].values.push([
				                         response.hits.hits[i]._source.timestamp,
				                         response.hits.hits[i]._source.usedMemory
				                         ]);
				swap[0].values.push([
			                        response.hits.hits[i]._source.timestamp,
			                        response.hits.hits[i]._source.totalSwap
			                        ]);
				swap[1].values.push([
			                         response.hits.hits[i]._source.timestamp,
			                         response.hits.hits[i]._source.usedSwap
			                         ]);
			}
			$scope.ram = ram;
			$scope.swap = swap;
		});
	};
	
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
		$scope.intervalId = setInterval(function() {
			$scope.endTime = moment().valueOf();
		    $scope.getLoadAverage($scope.startTime, $scope.endTime);
		    $scope.getMemoryUsage($scope.startTime, $scope.endTime);
			$scope.startTime = $scope.endTime;
		}, 1000);
	});
	$scope.$on("$routeChangeStart", function(event, current, previous, rejection) {
		clearInterval($scope.intervalId);
	});
});