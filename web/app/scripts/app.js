'use strict';

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

angular.module('watchcatApp',
		[ 'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ui.bootstrap', 'angular-flot', 'elasticsearch', 'pasvaz.bindonce', 'ui.bootstrap.datetimepicker']).config(
		function($routeProvider) {
			$routeProvider.when('/:host/dashboard', {
				templateUrl : 'views/dashboard.html',
				controller : 'DashboardCtrl'
			}).when('/:host/alerts/log', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host/alerts/emailaddresses', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host/alerts/loadaverage', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host/alerts/memoryusage', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host/alerts/diskusage', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host/alerts', {
				templateUrl : 'views/alerts.html',
				controller : 'AlertsCtrl'
			}).when('/:host', {
				templateUrl : 'views/main.html',
				controller : 'MainCtrl'
			}).when('/', {
				templateUrl : 'views/main.html',
				controller : 'MainCtrl'
			}).otherwise({
				redirectTo : '/'
			});
		});
