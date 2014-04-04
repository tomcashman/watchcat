'use strict';

angular.module('linuxGraphApp',
		[ 'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ui.bootstrap', 'angular-flot', 'elasticsearch', 'pasvaz.bindonce']).config(
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
