'use strict';

angular.module('linuxGraphApp').controller('MainCtrl', function($scope, $location, Hosts) {
	Hosts.list().then(function(response) {
		var result = [];
		for (var i = 0; i < response.hits.hits.length; i++) {
			result.push(response.hits.hits[i]._id);
		}
		if(result.length > 0) {
			$location.path("/" + result[0]);
		}
	}, function(error) {
		
	});
});
