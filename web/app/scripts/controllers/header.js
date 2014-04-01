'use strict';

angular.module('linuxGraphApp').controller('HeaderCtrl', function($rootScope, $scope, $routeParams, Hosts) {
	$scope.currentHost = "";
	if($routeParams.host) {
		$scope.currentHost = $routeParams.host;
	}
	$scope.hosts = [];
	
	$scope.selectedTimePeriod = 0;
	$scope.timePeriods = ["1 minute ago to a few seconds ago", "5 minutes ago to a few seconds ago", "Last 15 minutes", "Last 30 minutes", "Last hour"];
	
	Hosts.list().then(function(response) {
		var result = [];
		for (var i = 0; i < response.hits.hits.length; i++) {
			result.push(response.hits.hits[i]._id);
		}
		$scope.hosts = result;
		
		if(!$routeParams.host) {
			$scope.currentHost = $scope.hosts[0];
		} else {
			$scope.currentHost = $routeParams.host;
		}
	}, function(error) {
		
	});
	
	$scope.timePeriodClicked = function(timePeriod) {
		switch(timePeriod) {
		case 0:
			$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
				live: true,
				timePeriod: [moment().subtract('minutes', 1).valueOf(), moment().valueOf()]
			});
			break;
		case 1:
			$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
				live: true,
				timePeriod: [moment().subtract('minutes', 5).valueOf(), moment().valueOf()]
			});
			break;
		case 2:
			$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
				live: false,
				timePeriod: [moment().subtract('minutes', 15).valueOf(), moment().valueOf()]
			});
			break;
		case 3:
			$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
				live: false,
				timePeriod: [moment().subtract('minutes', 30).valueOf(), moment().valueOf()]
			});
			break;
		case 4:
			$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
				live: false,
				timePeriod: [moment().subtract('hours', 1).valueOf(), moment().valueOf()]
			});
			break;
		}
		$scope.selectedTimePeriod = timePeriod;
	};
	
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
		if($routeParams.host) {
			$scope.currentHost = $routeParams.host;
			$scope.timePeriodClicked($scope.selectedTimePeriod);
		}
	});
});
