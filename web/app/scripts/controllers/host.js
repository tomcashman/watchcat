'use strict';

angular.module('linuxGraphApp').controller('HostCtrl', function($scope, $route, $routeParams, $interval, $window, Metrics) {
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
	$scope.lastPollTime = moment().subtract('minutes', 2).valueOf();
	
	$scope.getLoadAverage = function(startTime, endTime, lastPollTime) {
		Metrics.getLoadAverage($routeParams.host, startTime, endTime).then(function(response) {
			if(lastPollTime === startTime) {
				/* Push existing data to the left, dropping the item at index 0 */
				for(var i = 1; i < $scope.loadAverage[0].values.length; i++) {
					$scope.loadAverage[0].values[i - 1] = $scope.loadAverage[0].values[i];
					$scope.loadAverage[1].values[i - 1] = $scope.loadAverage[1].values[i];
					$scope.loadAverage[2].values[i - 1] = $scope.loadAverage[2].values[i];
				}
				
				/* Insert new data */
				var index = $scope.loadAverage[0].values.length - 1;
				$scope.loadAverage[0].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.oneMinuteAverage
				];
				$scope.loadAverage[1].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.fiveMinuteAverage
				];
				$scope.loadAverage[2].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.fifteenMinuteAverage
				];
			} else {
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
			}			
			
			if(response.hits.hits.length > 0) {
				$scope.cpuCores = response.hits.hits[response.hits.hits.length - 1]._source.cpuCores;
			}
		});
	};
	
	$scope.getMemoryUsage = function(startTime, endTime, lastPollTime) {
		Metrics.getMemoryUsage($routeParams.host, startTime, endTime).then(function(response) {
			if(lastPollTime === startTime) {
				/* Push existing data to the left, dropping the item at index 0 */
				for(var i = 1; i < $scope.ram[0].values.length; i++) {
					$scope.ram[0].values[i - 1] = $scope.ram[0].values[i];
					$scope.ram[1].values[i - 1] = $scope.ram[1].values[i];
					$scope.swap[0].values[i - 1] = $scope.swap[0].values[i];
					$scope.swap[1].values[i - 1] = $scope.swap[1].values[i];
				}
				
				/* Insert new data */
				var index = $scope.ram[0].values.length - 1;

				$scope.ram[0].values[index]= [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.totalMemory
				];
				$scope.ram[1].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.usedMemory
				];
				$scope.swap[0].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.totalSwap
				];
				$scope.swap[1].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.usedSwap
				];
			} else {
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
			}
		});
	};
	
	$scope.getBandwidth = function(startTime, endTime, lastPollTime) {
		Metrics.getBandwidth($routeParams.host, startTime, endTime).then(function(response) {
			if(lastPollTime === startTime) {
				/* Push existing data to the left, dropping the item at index 0 */
				for(var i = 1; i < $scope.bandwidth[0].values.length; i++) {
					$scope.bandwidth[0].values[i - 1] = $scope.bandwidth[0].values[i];
					$scope.bandwidth[1].values[i - 1] = $scope.bandwidth[1].values[i];
				}

				/* Insert new data */
				var index = $scope.bandwidth[0].values.length - 1;

				$scope.bandwidth[0].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.rx
				];
				$scope.bandwidth[1].values[index] = [
					response.hits.hits[0]._source.timestamp,
					response.hits.hits[0]._source.tx
				];
			} else {
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
								key: 'Total',
								values: [[timestamp, response.hits.hits[i]._source.filesystems[j].size]]
							}, {
								key: 'Used',
								values: [[timestamp, response.hits.hits[i]._source.filesystems[j].used]]
							}]
						});
					} else {
						if(startTime === lastPollTime) {
							/* Push existing data to the left, dropping the item at index 0 */
							for(var l = 1; l < $scope.disks[diskIndex].graph[0].values.length; l++) {
								$scope.disks[diskIndex].graph[0].values[l - 1] = $scope.disks[diskIndex].graph[0].values[l];
								$scope.disks[diskIndex].graph[1].values[l - 1] = $scope.disks[diskIndex].graph[1].values[l];
							}
							
							/* Insert new data */
							var index = $scope.disks[diskIndex].graph[0].values.length - 1;

							$scope.disks[diskIndex].graph[0].values[index] = [timestamp, response.hits.hits[i]._source.filesystems[j].size];
							$scope.disks[diskIndex].graph[1].values[index] = [timestamp, response.hits.hits[i]._source.filesystems[j].used];
						} else {
							$scope.disks[diskIndex].graph[0].values.push([timestamp, response.hits.hits[i]._source.filesystems[j].size]);
							$scope.disks[diskIndex].graph[1].values.push([timestamp, response.hits.hits[i]._source.filesystems[j].used]);
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
			if(startTime === lastPollTime) {
				for(var i = $scope.networkConnections.length - 1; i >= 0; i--) {
					var stillExists = false;
					for(var j = 0; j < response.hits.hits[0]._source.connections.length; j++) {
						if(response.hits.hits[0]._source.connections[j].address === $scope.networkConnections[i].key) {
							stillExists = true;
							break;
						}
					}

					if(stillExists) {
						for(var l = 1; l < $scope.networkConnections[i].values.length; l++) {
							$scope.networkConnections[i].values[l - 1] = $scope.networkConnections[i].values[l];
						}
					} else {
						$scope.networkConnections.splice(i, 1);
					}
				}
			}

			for(var i = 0; i < response.hits.hits.length; i++) {
				var timestamp = response.hits.hits[i]._source.timestamp;
				for(var j = 0; j < response.hits.hits[i]._source.connections.length; j++) {
					var address = response.hits.hits[i]._source.connections[j].address;
					var totalConnections = response.hits.hits[i]._source.connections[j].total;
					var addressIndex = -1;
					
					for(var k = 0; k < $scope.networkConnections.length; k++) {
						if($scope.networkConnections[k].key === address) {
							addressIndex = k;
							break;
						}
					}
					
					if(addressIndex < 0) {
						$scope.networkConnections.push({
							key: address,
							values: [[timestamp, totalConnections]]
						});
					} else {
						if(startTime === lastPollTime) {
							var index = $scope.networkConnections[addressIndex].values.length - 1;
							$scope.networkConnections[addressIndex].values[index] = [timestamp, totalConnections];
						} else {
							$scope.networkConnections[addressIndex].values.push([timestamp, totalConnections]);
						}
					}
				}
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
