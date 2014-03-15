'use strict';

angular.module('linuxGraphApp',
		[ 'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ui.bootstrap', 'elasticsearch']).config(
		function($routeProvider) {
			$routeProvider.when('/:host', {
				templateUrl : 'views/host.html',
				controller : 'HostCtrl'
			}).when('/', {
				templateUrl : 'views/main.html',
				controller : 'MainCtrl'
			}).otherwise({
				redirectTo : '/'
			});
		});