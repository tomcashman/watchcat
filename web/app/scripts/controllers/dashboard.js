'use strict';

angular.module('watchcatApp').controller('DashboardCtrl', function($scope, $route, $routeParams, $interval, $window, Metrics) {
	$scope.flotOptions = {
			series: {
				shadowSize: 0,	// Drawing is faster without shadows
				lines: { show: true }
			},
			xaxis: { 
				mode: "time",
				tickSize: [20, "second"],
			},
			yaxis: {
				min: 0
			},
			selection: {
				mode: "x"
			},
			legend: {
				show: true,
				noColumns: 5
			}
	};
	
	$scope.resetGraphs = function() {
		$scope.loadAverage = [
		                      {
		                    	  label: '1 min.',
		                    	  data: []
		                      },
		                      {
		                    	  label: '5 min.',
		                    	  data: []
		                      },
		                      {
		                    	  label: '15 min.',
		                    	  data: []
		                      }
		                      ];
		
		$scope.ram = [ {
			label: 'Total',
			data: []
		}, {
			label: 'Used',
			data: []
		}];
		
		$scope.swap = [ {
			label: 'Total',
			data: []
		}, {
			label: 'Used',
			data: []
		}];
		
		$scope.bandwidth = [ {
			label: 'RX',
			data: []
		}, {
			label: 'TX',
			data: []
		}];
		
		$scope.networkConnections = [{
			label: 'Total',
			data: []
		}];
		$scope.disks = [];
		$scope.processes = [];
	};
	$scope.resetGraphs();
	
	$scope.startTime = moment().subtract('minutes', 1).valueOf();
	$scope.lastPollTime = moment().subtract('minutes', 2).valueOf();
	
	$scope.getLoadAverage = function(startTime, endTime, lastPollTime) {
		Metrics.getLoadAverage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.aggregations.timestamps.buckets.length; i++) {
				var bucket = response.aggregations.timestamps.buckets[i];
				var timestamp = bucket.from;
				if(i == response.aggregations.timestamps.buckets.length - 1)
					timestamp = bucket.to;
				
				if(!bucket.oneMinuteAverage.value)
					continue;
				
				$scope.loadAverage[0].data.push([
					                timestamp,
					                bucket.oneMinuteAverage.value
					                ]);
				$scope.loadAverage[1].data.push([
					                timestamp,
					                bucket.fiveMinuteAverage.value
					                 ]);
				$scope.loadAverage[2].data.push([
					                     timestamp,
					                     bucket.fifteenMinuteAverage.value
					                    ]);
				$scope.cpuCores = bucket.cpuCores.value;
			}
		});
	};
	
	$scope.getMemoryUsage = function(startTime, endTime, lastPollTime) {
		Metrics.getMemoryUsage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.aggregations.timestamps.buckets.length; i++) {
				var bucket = response.aggregations.timestamps.buckets[i];
				var timestamp = bucket.from;
				if(i == response.aggregations.timestamps.buckets.length - 1)
					timestamp = bucket.to;
				if(!bucket.totalMemory.value)
					continue;
				
				$scope.ram[0].data.push([
					timestamp,
					bucket.totalMemory.value
				]);
				$scope.ram[1].data.push([
					timestamp,
					bucket.usedMemory.value
				]);
				$scope.swap[0].data.push([
					timestamp,
					bucket.totalSwap.value
				]);
				$scope.swap[1].data.push([
					timestamp,
					bucket.usedSwap.value
				]);
			}
		});
	};
	
	$scope.getBandwidth = function(startTime, endTime, lastPollTime) {
		Metrics.getBandwidth($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.aggregations.timestamps.buckets.length; i++) {
				var bucket = response.aggregations.timestamps.buckets[i];
				var timestamp = bucket.from;
				if(i == response.aggregations.timestamps.buckets.length - 1)
					timestamp = bucket.to;
				if(bucket.rx.value == null)
					continue;
				
				$scope.bandwidth[0].data.push([
						                timestamp,
						                bucket.rx.value
						                ]);
				$scope.bandwidth[1].data.push([
						                timestamp,
						                bucket.tx.value
						                ]);
			}
		});
	};
	
	$scope.getDiskUsage = function(startTime, endTime, lastPollTime) {
		Metrics.getDiskUsage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				var timestamp = response.hits.hits[i]._source.timestamp;
				for(var j = 0; j < response.hits.hits[i]._source.filesystems.length; j++) {
					var filesystem = response.hits.hits[i]._source.filesystems[j].filesystem;
					var mountPoint = response.hits.hits[i]._source.filesystems[j].mountPoint;
					var diskIndex = -1;
					
					for(var k = 0; k < $scope.disks.length; k++) {
						if($scope.disks[k].filesystem === filesystem) {

							diskIndex = k;
							break;
						}
					}
					
					if(diskIndex < 0) {
						$scope.disks.push({
							filesystem: filesystem,
							mountPoint: mountPoint,
							graph: [{
								label: 'Total',
								data: [[timestamp, response.hits.hits[i]._source.filesystems[j].size]]
							}, {
								label: 'Used',
								data: [[timestamp, response.hits.hits[i]._source.filesystems[j].used]]
							}]
						});
					} else {
						if(startTime === lastPollTime) {
							/* Push existing data to the left, dropping the item at index 0 */
							for(var l = 1; l < $scope.disks[diskIndex].graph[0].data.length; l++) {
								$scope.disks[diskIndex].graph[0].data[l - 1] = $scope.disks[diskIndex].graph[0].data[l];
								$scope.disks[diskIndex].graph[1].data[l - 1] = $scope.disks[diskIndex].graph[1].data[l];
							}
							
							/* Insert new data */
							var index = $scope.disks[diskIndex].graph[0].data.length - 1;

							$scope.disks[diskIndex].graph[0].data[index] = [timestamp, response.hits.hits[i]._source.filesystems[j].size];
							$scope.disks[diskIndex].graph[1].data[index] = [timestamp, response.hits.hits[i]._source.filesystems[j].used];
						} else {
							$scope.disks[diskIndex].graph[0].data.push([timestamp, response.hits.hits[i]._source.filesystems[j].size]);
							$scope.disks[diskIndex].graph[1].data.push([timestamp, response.hits.hits[i]._source.filesystems[j].used]);
						}
					}
				}
			}
		});
	};
	
	$scope.getProcesses = function(startTime, endTime, lastPollTime) {
		Metrics.getProcesses($routeParams.host, startTime, endTime).then(function(response) {
			if(response.hits.hits.length > 0) {
				var processes = response.hits.hits[response.hits.hits.length - 1]._source.processes;
				processes = processes.slice(0, 10);
				$scope.processes = processes;
			}
		});
	};
	
	$scope.getNetworkConnections = function(startTime, endTime, lastPollTime) {
		Metrics.getNetworkConnections($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.aggregations.timestamps.buckets.length; i++) {
				var bucket = response.aggregations.timestamps.buckets[i];
				var timestamp = bucket.from;
				if(i == response.aggregations.timestamps.buckets.length - 1)
					timestamp = bucket.to;
				
				$scope.networkConnections[0].data.push([timestamp,  bucket.total.value]);
			}
		});
	};
	
	$scope.poll = function() {
		$scope.endTime = moment().valueOf();
		$scope.getLoadAverage($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.getMemoryUsage($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.getDiskUsage($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.getBandwidth($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.getProcesses($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.getNetworkConnections($scope.startTime, $scope.endTime, $scope.lastPollTime);
		$scope.startTime = $scope.endTime;
		$scope.lastPollTime = $scope.endTime;
	};
	
	$scope.beginPolling = function() {
		$scope.intervalId = $interval(function() {
			$scope.poll();
		}, 1000);
	};
	$scope.cancelPolling = function() {
		$interval.cancel($scope.intervalId);
	};
	$scope.live = true;
	
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
	});
	$scope.$on("$routeChangeStart", function(event, current, previous, rejection) {
		$scope.cancelPolling();
	});
	$scope.$on("UPDATE_TIME_PERIOD", function(event, timePeriodData){
		$scope.cancelPolling();
		$scope.live = timePeriodData.live;
		$scope.flotOptions.xaxis.tickSize = timePeriodData.tickSize;
		
		$scope.lastPollTime = moment().subtract('minutes', 2).valueOf();
		$scope.startTime = timePeriodData.timePeriod[0];
		$scope.endTime = timePeriodData.timePeriod[1];
		$scope.resetGraphs();
		
		if($scope.live) {
			$scope.beginPolling();
		} else {
			$scope.poll();
		}
	});
	
	$window.addEventListener('focus', function() {
		$route.reload();
	});
	$window.addEventListener('blur', $scope.cancelPolling());
});
