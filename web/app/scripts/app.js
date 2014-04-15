/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Cashman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
