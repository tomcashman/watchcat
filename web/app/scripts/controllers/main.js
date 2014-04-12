'use strict';

angular.module('watchcatApp').controller('MainCtrl', function($scope, $location, $routeParams, Hosts) {
	$scope.online = true;
	
	if($routeParams.host) {
		$location.path("/" + $routeParams.host + "/dashboard");
	} else {
		Hosts.list().then(function(response) {
			var result = [];
			for (var i = 0; i < response.hits.hits.length; i++) {
				result.push(response.hits.hits[i]._id);
			}
			if(result.length > 0) {
				$location.path("/" + result[0]);
			} else {
				$scope.online = false;
			}
		}, function(error) {
			$scope.online = false;
		});
	}
});
