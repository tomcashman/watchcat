'use strict';

angular.module('linuxGraphApp').controller('HeaderCtrl', function($scope, $routeParams, Hosts) {
	$scope.currentHost = "";
	if($routeParams.host) {
		$scope.currentHost = $routeParams.host;
	}
	$scope.hosts = [];
	
	Hosts.list().then(function(response) {
		var result = [];
		for (var i = 0; i < response.hits.hits.length; i++) {
			result.push(response.hits.hits[i]._id);
		}
		$scope.hosts = result;
		
		if($scope.currentHost === "") {
			$scope.currentHost = $scope.hosts[0];
		}
	}, function(error) {
		
	});
});