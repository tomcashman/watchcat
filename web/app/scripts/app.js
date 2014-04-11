'use strict';

angular.module('watchcatApp',
		[ 'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ui.bootstrap', 'angular-flot', 'elasticsearch', 'pasvaz.bindonce', 'ui.bootstrap.datetimepicker']).config(
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
