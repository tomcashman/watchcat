'use strict';

angular.module('linuxGraphApp').controller('HostCtrl', function($scope, $routeParams) {
	$scope.$on("$routeChangeSuccess", function(event, current, previous, rejection) {
		$scope.intervalId = setInterval(function() {
		    //TODO: Poll metrics and update graphs
		}, 1000);
	});
	$scope.$on("$routeChangeStart", function(event, current, previous, rejection) {
		clearInterval($scope.intervalId);
	});
});