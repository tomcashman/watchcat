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
	
	$scope.bandwidth = [ {
		key: 'RX',
		values: []
	}, {
		key: 'TX',
		values: []
	}];
	
	$scope.disks = [];
	$scope.processes = [];
	
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
			
			if(response.hits.hits.length > 0) {
				$scope.cpuCores = response.hits.hits[response.hits.hits.length - 1]._source.cpuCores;
				$scope.loadAverage = averages;
			}
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
	
	$scope.getBandwidth = function(startTime, endTime) {
		Metrics.getBandwidth($routeParams.host, startTime, endTime).then(function(response) {
			var bandwidth = [];
			bandwidth.push($scope.bandwidth[0]);
			bandwidth.push($scope.bandwidth[1]);
			
			for(var i = 0; i < response.hits.hits.length; i++) {
				bandwidth[0].values.push([
					                        response.hits.hits[i]._source.timestamp,
					                        response.hits.hits[i]._source.rx
					                        ]);
				bandwidth[1].values.push([
					                        response.hits.hits[i]._source.timestamp,
					                        response.hits.hits[i]._source.tx
					                        ]);
			}
			$scope.bandwidth = bandwidth;
		});
	};
	
	$scope.getDiskUsage = function(startTime, endTime) {
		Metrics.getDiskUsage($routeParams.host, startTime, endTime).then(function(response) {
			var disks = [];
			for(var i = 0; i < $scope.disks.length; i++) {
				disks.push($scope.disks[i]);
			}
			
			for(var i = 0; i < response.hits.hits.length; i++) {
				var timestamp = response.hits.hits[i]._source.timestamp;
				for(var j = 0; j < response.hits.hits[i]._source.filesystems.length; j++) {
					var filesystem = response.hits.hits[i]._source.filesystems[j].filesystem;
					var mountPoint = response.hits.hits[i]._source.filesystems[j].mountPoint;
					var exists = false;
					
					for(var k = 0; k < disks.length; k++) {
						if(disks[k].filesystem === filesystem) {
							var graph = [];
							for(var l = 0; l < disks[k].graph.length; l++) {
								graph.push(disks[k].graph[l]);
							}
							
							var sizeGraph = graph[0];
							sizeGraph.values.push([timestamp, response.hits.hits[i]._source.filesystems[j].size]);
							graph[0] = sizeGraph;
							
							var usedGraph = graph[1];
							usedGraph.values.push([timestamp, response.hits.hits[i]._source.filesystems[j].used]);
							graph[1] = usedGraph;
							
							disks[k].graph = graph;
							
							exists = true;
							break;
						}
					}
					
					if(!exists) {
						disks.push({
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
			
			$scope.disks = disks;
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
	
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
		$scope.intervalId = setInterval(function() {
			$scope.endTime = moment().valueOf();
		    $scope.getLoadAverage($scope.startTime, $scope.endTime);
		    $scope.getMemoryUsage($scope.startTime, $scope.endTime);
		    $scope.getDiskUsage($scope.startTime, $scope.endTime);
		    $scope.getBandwidth($scope.startTime, $scope.endTime);
		    $scope.getProcesses($scope.startTime, $scope.endTime);
			$scope.startTime = $scope.endTime;
		}, 1000);
	});
	$scope.$on("$routeChangeStart", function(event, current, previous, rejection) {
		clearInterval($scope.intervalId);
	});
});