'use strict';

angular.module('watchcatApp').controller(
		'AlertsCtrl',
		function($scope, $route, $routeParams, $location, AlertThresholds, AlertsLog) {
			if ($location.path().endsWith('alerts')) {
				$location.path($location.path() + '/log');
				return;
			}

			$scope.urlPrefix = $location.path().substring(0,
					$location.path().lastIndexOf('/'));
			$scope.section = $location.path().substring(
					$location.path().lastIndexOf('/') + 1);
			$scope.alerts = [];
			
			$scope.closeAlert = function(index) {
			    $scope.alerts.splice(index, 1);
			};
			
			if($scope.section === 'log') {
				AlertsLog.getLogs($routeParams.host).then(function(response) {
					for(var i = 0; i < response.hits.hits.length; i++) {
						var datetime = moment(response.hits.hits[i]._source.timestamp);
						datetime.local();
						response.hits.hits[i]._source.datetime = datetime.format("MMMM Do YYYY, h:mm:ss a");
					}
					
					$scope.alerts = response.hits.hits;
				}, function() {
					
				});
			} else if($scope.section === 'loadaverage') {
				AlertThresholds.getLoadAverageThresholds($routeParams.host).then(function(response) {
					$scope.threshold = response;
				}, function() {
					
				});
				
				$scope.save = function(thresholds) {
					AlertThresholds.saveLoadAverageThresholds($routeParams.host, thresholds).then(function() {
						$scope.alerts.push({ type: 'success', msg: 'Saved successfully'});
					}, function() {
						$scope.alerts.push({ type: 'danger', msg: 'Could not save settings to ElasticSearch'});
					});
				};
			}
		});
