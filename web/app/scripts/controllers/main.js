'use strict';

angular.module('watchcatApp').controller('MainCtrl', function($scope, $location, Hosts) {
	$scope.online = true;
	
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
});
