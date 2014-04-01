'use strict';

angular.module('linuxGraphApp').controller('HostCtrl', function($scope, $routeParams, $interval, Metrics) {
	$scope.resetGraphs = function() {
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
		
		$scope.bandwidth = [ {
			key: 'RX',
			values: []
		}, {
			key: 'TX',
			values: []
		}];
		
		$scope.networkConnections = [];
		$scope.disks = [];
		$scope.processes = [];
	};
	$scope.resetGraphs();
	
	$scope.timeTickFormat = function() {
		return function(d) {
			return d3.time.format('%X')(new Date(d));
		};
	};
	
	$scope.startTime = moment().subtract('minutes', 1).valueOf();
	
	$scope.getLoadAverage = function(startTime, endTime) {
		Metrics.getLoadAverage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				$scope.loadAverage[0].values.push([
				                        response.hits.hits[i]._source.timestamp,
				                        response.hits.hits[i]._source.oneMinuteAverage
				                        ]);
				$scope.loadAverage[1].values.push([
				                         response.hits.hits[i]._source.timestamp,
				                         response.hits.hits[i]._source.fiveMinuteAverage
				                         ]);
				$scope.loadAverage[2].values.push([
				                            response.hits.hits[i]._source.timestamp,
				                            response.hits.hits[i]._source.fifteenMinuteAverage
				                            ]);
			}
			
			if(response.hits.hits.length > 0) {
				$scope.cpuCores = response.hits.hits[response.hits.hits.length - 1]._source.cpuCores;
			}
		});
	};
	
	$scope.getMemoryUsage = function(startTime, endTime) {
		Metrics.getMemoryUsage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				$scope.ram[0].values.push([
				                        response.hits.hits[i]._source.timestamp,
				                        response.hits.hits[i]._source.totalMemory
				                        ]);
				$scope.ram[1].values.push([
				                         response.hits.hits[i]._source.timestamp,
				                         response.hits.hits[i]._source.usedMemory
				                         ]);
				$scope.swap[0].values.push([
			                        response.hits.hits[i]._source.timestamp,
			                        response.hits.hits[i]._source.totalSwap
			                        ]);
				$scope.swap[1].values.push([
			                         response.hits.hits[i]._source.timestamp,
			                         response.hits.hits[i]._source.usedSwap
			                         ]);
			}
		});
	};
	
	$scope.getBandwidth = function(startTime, endTime) {
		Metrics.getBandwidth($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				$scope.bandwidth[0].values.push([
					                        response.hits.hits[i]._source.timestamp,
					                        response.hits.hits[i]._source.rx
					                        ]);
				$scope.bandwidth[1].values.push([
					                        response.hits.hits[i]._source.timestamp,
					                        response.hits.hits[i]._source.tx
					                        ]);
			}
		});
	};
	
	$scope.getDiskUsage = function(startTime, endTime) {
		Metrics.getDiskUsage($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				var timestamp = response.hits.hits[i]._source.timestamp;
				for(var j = 0; j < response.hits.hits[i]._source.filesystems.length; j++) {
					var filesystem = response.hits.hits[i]._source.filesystems[j].filesystem;
					var mountPoint = response.hits.hits[i]._source.filesystems[j].mountPoint;
					var exists = false;
					
					for(var k = 0; k < $scope.disks.length; k++) {
						if($scope.disks[k].filesystem === filesystem) {
							$scope.disks[k].graph[0].values.push([timestamp, response.hits.hits[i]._source.filesystems[j].size]);
							$scope.disks[k].graph[1].values.push([timestamp, response.hits.hits[i]._source.filesystems[j].used]);
							exists = true;
							break;
						}
					}
					
					if(!exists) {
						$scope.disks.push({
							filesystem: filesystem,
							mountPoint: mountPoint,
							graph: [{
								key: 'Total',
								values: [[timestamp, response.hits.hits[i]._source.filesystems[j].size]]
							}, {
								key: 'Used',
								values: [[timestamp, response.hits.hits[i]._source.filesystems[j].used]]
							}]
						});
					}
				}
			}
		});
	};
	
	$scope.getProcesses = function(startTime, endTime) {
		Metrics.getProcesses($routeParams.host, startTime, endTime).then(function(response) {
			if(response.hits.hits.length > 0) {
				var processes = [];
				
				for(var i = 0; i < response.hits.hits[response.hits.hits.length - 1]._source.processes.length && i < 9; i++) {
					processes.push(response.hits.hits[response.hits.hits.length - 1]._source.processes[i]);
				}
				
				$scope.processes = processes;
			}
		});
	};
	
	$scope.getNetworkConnections = function(startTime, endTime) {
		Metrics.getNetworkConnections($routeParams.host, startTime, endTime).then(function(response) {
			for(var i = 0; i < response.hits.hits.length; i++) {
				var timestamp = response.hits.hits[i]._source.timestamp;
				for(var j = 0; j < response.hits.hits[i]._source.connections.length; j++) {
					var address = response.hits.hits[i]._source.connections[j].address;
					var totalConnections = response.hits.hits[i]._source.connections[j].total;
					var exists = false;
					
					for(var k = 0; k < $scope.networkConnections.length; k++) {
						if($scope.networkConnections[k].key === address) {
							$scope.networkConnections[k].values.push([timestamp, totalConnections]);
							exists = true;
							break;
						}
					}
					
					if(!exists) {
						$scope.networkConnections.push({
							key: address,
							values: [[timestamp, totalConnections]]
						});
					}
				}
			}
		});
	};
	
	$scope.poll = function() {
		$scope.endTime = moment().valueOf();
	    $scope.getLoadAverage($scope.startTime, $scope.endTime);
	    $scope.getMemoryUsage($scope.startTime, $scope.endTime);
	    $scope.getDiskUsage($scope.startTime, $scope.endTime);
	    $scope.getBandwidth($scope.startTime, $scope.endTime);
	    $scope.getProcesses($scope.startTime, $scope.endTime);
	    $scope.getNetworkConnections($scope.startTime, $scope.endTime);
		$scope.startTime = $scope.endTime;
	};
	
	$scope.beginPolling = function() {
		$scope.intervalId = $interval(function() {
			$scope.poll();
		}, 2500);
	};
	$scope.cancelPolling = function() {
		$interval.cancel($scope.intervalId);
	};
	
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
	});
	$scope.$on("$routeChangeStart", function(event, current, previous, rejection) {
		$scope.cancelPolling();
	});
	$scope.$on("UPDATE_TIME_PERIOD", function(event, timePeriodData){
		$scope.cancelPolling();
		
		$scope.startTime = timePeriodData.timePeriod[0];
		$scope.endTime = timePeriodData.timePeriod[1];
		$scope.resetGraphs();
		
		if(timePeriodData.live) {
			$scope.beginPolling();
		} else {
			$scope.poll();
		}
	});
});
