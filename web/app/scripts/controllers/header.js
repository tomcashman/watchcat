'use strict';

angular.module('linuxGraphApp').controller(
		'HeaderCtrl',
		function($rootScope, $scope, $routeParams, $modal, Hosts) {
			$scope.currentHost = "";
			if ($routeParams.host) {
				$scope.currentHost = $routeParams.host;
			}
			$scope.hosts = [];
			$scope.dateFormat = 'YYYY-MM-DDTHH:mm:ssZ';

			$scope.selectedTimePeriod = 0;
			$scope.timePeriods = [ "1 minute ago to now",
					"5 minutes ago to now", "Last 15 minutes",
					"Last 30 minutes", "Last hour", "" ];

			Hosts.list().then(function(response) {
				var result = [];
				for (var i = 0; i < response.hits.hits.length; i++) {
					result.push(response.hits.hits[i]._id);
				}
				$scope.hosts = result;

				if (!$routeParams.host) {
					$scope.currentHost = $scope.hosts[0];
				} else {
					$scope.currentHost = $routeParams.host;
				}
			}, function(error) {

			});

			$scope.timePeriodClicked = function(timePeriod) {
				switch (timePeriod) {
				case 0:
					$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
						live : true,
						tickSize : [ 20, "second" ],
						timePeriod : [
								moment().subtract('minutes', 1).valueOf(),
								moment().valueOf() ]
					});
					break;
				case 1:
					$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
						live : true,
						tickSize : [ 1, "minute" ],
						timePeriod : [
								moment().subtract('minutes', 5).valueOf(),
								moment().valueOf() ]
					});
					break;
				case 2:
					$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
						live : false,
						tickSize : [ 5, "minute" ],
						timePeriod : [
								moment().subtract('minutes', 15).valueOf(),
								moment().valueOf() ]
					});
					break;
				case 3:
					$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
						live : false,
						tickSize : [ 10, "minute" ],
						timePeriod : [
								moment().subtract('minutes', 30).valueOf(),
								moment().valueOf() ]
					});
					break;
				case 4:
					$rootScope.$broadcast("UPDATE_TIME_PERIOD", {
						live : false,
						tickSize : [ 20, "minute" ],
						timePeriod : [ moment().subtract('hours', 1).valueOf(),
								moment().valueOf() ]
					});
					break;
				}
				$scope.selectedTimePeriod = timePeriod;
			};

			$scope.$on("$routeChangeSuccess", function(event, current,
					previous, rejection) {
				if ($routeParams.host) {
					$scope.currentHost = $routeParams.host;
					$scope.timePeriodClicked($scope.selectedTimePeriod);
				}
			});

			$scope.openCustomDateDialog = function() {
				var modalInstance = $modal.open({
					templateUrl : 'customdate.html',
					controller : ModalInstanceCtrl
				});

				modalInstance.result.then(function(customTimePeriod) {
					$rootScope.$broadcast("UPDATE_TIME_PERIOD",
							customTimePeriod);
					$scope.timePeriods[$scope.timePeriods.length - 1] = 'From '
							+ moment(customTimePeriod.timePeriod[0]).format(
									'MMMM Do YYYY, h:mm:ss a')
							+ ' to '
							+ moment(customTimePeriod.timePeriod[1]).format(
									'MMMM Do YYYY, h:mm:ss a');
					$scope.selectedTimePeriod = $scope.timePeriods.length - 1;
				}, function() {
				});
			};
		});

var ModalInstanceCtrl = function($scope, $modalInstance) {
	$scope.onStartDateSet = function (newDate, oldDate) {
		$scope.startDate = newDate;
	};
	$scope.onEndDateSet = function (newDate, oldDate) {
		$scope.endDate = newDate;
	};
	
	$scope.invalidDates = function() {
		if(!$scope.startDate)
			return true;
		if(!$scope.endDate)
			return true;
		if(moment($scope.startDate).isSame(moment($scope.endDate)))
			return true;
		if(moment($scope.startDate).isAfter(moment($scope.endDate)))
			return true;
		
		return false;
	};
	
	$scope.ok = function() {
		var tickSize = (moment($scope.endDate).valueOf() - moment(
				$scope.startDate).valueOf()) / 1000;
		var tickUnit = "second";
		if(tickSize > 86400) {
			tickSize = 1;
			tickUnit = "day";
		} else if(tickSize > 21600) {
			tickSize = 6;
			tickUnit = "hour";
		} else if(tickSize > 7200) {
			tickSize = 2;
			tickUnit = "hour";
		} else if(tickSize > 1800) {
			tickSize = 30;
			tickUnit = "minute";
		} else if(tickSize > 900) {
			tickSize = 10;
			tickUnit = "minute";
		} else if(tickSize > 300) {
			tickSize = 5;
			tickUnit = "minute";
		} else if(tickSize > 90) {
			tickSize = 1;
			tickUnit = "minute";
		}  else if(tickSize > 30) {
			tickSize = 20;
			tickUnit = "second";
		}

		$modalInstance.close({
			live : false,
			tickSize : [ tickSize, tickUnit ],
			timePeriod : [ moment($scope.startDate).valueOf(),
					moment($scope.endDate).valueOf() ],
		});
	};

	$scope.cancel = function() {
		$modalInstance.dismiss('cancel');
	};
};