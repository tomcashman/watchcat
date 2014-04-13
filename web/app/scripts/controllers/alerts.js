'use strict';

angular.module('watchcatApp').controller(
		'AlertsCtrl',
		function($scope, $route, $routeParams, $location, AlertThresholds, AlertEmailAddresses, AlertsLog) {
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
				$scope.page = 1;
				$scope.totalPages = 1;
				$scope.pages = [];
				
				$scope.fetchPage = function() {
					AlertsLog.getLogs($routeParams.host, $scope.page).then(function(response) {
						for(var i = 0; i < response.hits.hits.length; i++) {
							var datetime = moment(response.hits.hits[i]._source.timestamp);
							datetime.local();
							response.hits.hits[i]._source.datetime = datetime.format("MMMM Do YYYY, h:mm:ss a");
						}
						
						$scope.alerts = response.hits.hits;
					}, function() {
						
					});
				};
				$scope.goToPage = function(pageNumber) {
					$scope.page = pageNumber;
					$scope.fetchPage();
				};
				
				$scope.goBack = function() {
					if($scope.page > 1) {
						$scope.page = $scope.page - 1;
						$scope.fetchPage();
					}
				};
				$scope.goForward = function() {
					if($scope.page < $scope.totalPages) {
						$scope.page = $scope.page + 1;
						$scope.fetchPage();
					}
				};
				
				AlertsLog.count($routeParams.host).then(function(response) {
					$scope.totalPages = response.count / 15;
					if($scope.totalPages < 1) {
						$scope.totalPages = 1;
					}
					var pages = [];
					for(var i = 0; i < $scope.totalPages; i++) {
						pages.push(i + 1);
					}
					$scope.pages = pages;
				}, function() {
					
				});
				$scope.fetchPage();
				
			} else if($scope.section === 'emailaddresses') {
				$scope.EMAIL_REGEXP = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$/;
				$scope.addresses = {
					list: []
				};
				AlertEmailAddresses.getEmailAddresses($routeParams.host).then(function(response) {
					$scope.addresses = response;
				}, function() {
					
				});
				
				$scope.addEmailAddress = function() {
					$scope.addresses.list.push('');
				};
				
				$scope.removeEmailAddress = function(index) {
					$scope.addresses.list.splice(index, 1);
				};
				
				$scope.save = function(addresses) {
					AlertEmailAddresses.saveEmailAddresses($routeParams.host, addresses).then(function(response) {
						$scope.alerts.push({ type: 'success', msg: 'Saved successfully'});
					}, function() {
						$scope.alerts.push({ type: 'danger', msg: 'Could not save settings to ElasticSearch'});
					});
				};
			} else if($scope.section === 'loadaverage') {
				AlertThresholds.getLoadAverageThresholds($routeParams.host).then(function(response) {
					$scope.threshold = response;
				}, function() {
					
				});
				
				$scope.isValid = function(thresholds) {
					this.loadAverageThresholdForm.oneMinuteAverageCriticalThreshold.$invalid = false;
					this.loadAverageThresholdForm.oneMinuteAverageMajorThreshold.$invalid = false;
					this.loadAverageThresholdForm.oneMinuteAverageMinorThreshold.$invalid = false;
					
					this.loadAverageThresholdForm.fiveMinuteAverageCriticalThreshold.$invalid = false;
					this.loadAverageThresholdForm.fiveMinuteAverageMajorThreshold.$invalid = false;
					this.loadAverageThresholdForm.fiveMinuteAverageMinorThreshold.$invalid = false;
					
					this.loadAverageThresholdForm.fifteenMinuteAverageCriticalThreshold.$invalid = false;
					this.loadAverageThresholdForm.fifteenMinuteAverageMajorThreshold.$invalid = false;
					this.loadAverageThresholdForm.fifteenMinuteAverageMinorThreshold.$invalid = false;
					
					if(thresholds.oneMinuteAverageMinorThreshold < 0) {
						this.loadAverageThresholdForm.oneMinuteAverageMinorThreshold.$invalid = true;
						this.loadAverageThresholdForm.oneMinuteAverageMinorThreshold.errorMessage = 'Value cannot be less than 0';
						return false;
					}
					if(thresholds.oneMinuteAverageMajorThreshold <= thresholds.oneMinuteAverageMinorThreshold) {
						this.loadAverageThresholdForm.oneMinuteAverageMajorThreshold.$invalid = true;
						this.loadAverageThresholdForm.oneMinuteAverageMajorThreshold.errorMessage = 'Value must be greater than minor threshold';
						return false;
					}
					if(thresholds.oneMinuteAverageCriticalThreshold <= thresholds.oneMinuteAverageMajorThreshold) {
						this.loadAverageThresholdForm.oneMinuteAverageCriticalThreshold.$invalid = true;
						this.loadAverageThresholdForm.oneMinuteAverageCriticalThreshold.errorMessage = 'Value must be greater than major threshold';
						return false;
					}
					
					if(thresholds.fiveMinuteAverageMinorThreshold < 0) {
						this.loadAverageThresholdForm.fiveMinuteAverageMinorThreshold.$invalid = true;
						this.loadAverageThresholdForm.fiveMinuteAverageMinorThreshold.errorMessage = 'Value cannot be less than 0';
						return false;
					}
					if(thresholds.fiveMinuteAverageMajorThreshold <= thresholds.fiveMinuteAverageMinorThreshold) {
						this.loadAverageThresholdForm.fiveMinuteAverageMajorThreshold.$invalid = true;
						this.loadAverageThresholdForm.fiveMinuteAverageMajorThreshold.errorMessage = 'Value must be greater than minor threshold';
						return false;
					}
					if(thresholds.fiveMinuteAverageCriticalThreshold <= thresholds.fiveMinuteAverageMajorThreshold) {
						this.loadAverageThresholdForm.fiveMinuteAverageCriticalThreshold.$invalid = true;
						this.loadAverageThresholdForm.fiveMinuteAverageCriticalThreshold.errorMessage = 'Value must be greater than major threshold';
						return false;
					}
					
					if(thresholds.fifteenMinuteAverageMinorThreshold < 0) {
						this.loadAverageThresholdForm.fifteenMinuteAverageMinorThreshold.$invalid = true;
						this.loadAverageThresholdForm.fifteenMinuteAverageMinorThreshold.errorMessage = 'Value cannot be less than 0';
						return false;
					}
					if(thresholds.fifteenMinuteAverageMajorThreshold <= thresholds.fifteenMinuteAverageMinorThreshold) {
						this.loadAverageThresholdForm.fifteenMinuteAverageMajorThreshold.$invalid = true;
						this.loadAverageThresholdForm.fifteenMinuteAverageMajorThreshold.errorMessage = 'Value must be greater than minor threshold';
						return false;
					}
					if(thresholds.fifteenMinuteAverageCriticalThreshold <= thresholds.fifteenMinuteAverageMajorThreshold) {
						this.loadAverageThresholdForm.fifteenMinuteAverageCriticalThreshold.$invalid = true;
						this.loadAverageThresholdForm.fifteenMinuteAverageCriticalThreshold.errorMessage = 'Value must be greater than major threshold';
						return false;
					}
					
					return true;
				};
				
				$scope.save = function(thresholds) {
					AlertThresholds.saveLoadAverageThresholds($routeParams.host, thresholds).then(function() {
						$scope.alerts.push({ type: 'success', msg: 'Saved successfully'});
					}, function() {
						$scope.alerts.push({ type: 'danger', msg: 'Could not save settings to ElasticSearch'});
					});
				};
			} else if($scope.section === 'memoryusage') {
				AlertThresholds.getMemoryUsageThresholds($routeParams.host).then(function(response) {
					$scope.threshold = response;
				}, function() {
					
				});
				
				$scope.isValid = function(thresholds) {
					this.memoryUsageThresholdForm.usedSwapMinorThreshold.$invalid = false;
					this.memoryUsageThresholdForm.usedSwapMajorThreshold.$invalid = false;
					this.memoryUsageThresholdForm.usedSwapCriticalThreshold.$invalid = false;
					
					this.memoryUsageThresholdForm.usedMemoryMinorThreshold.$invalid = false;
					this.memoryUsageThresholdForm.usedMemoryMajorThreshold.$invalid = false;
					this.memoryUsageThresholdForm.usedMemoryCriticalThreshold.$invalid = false;
					
					if(thresholds.usedMemoryMinorThreshold < 1) {
						this.memoryUsageThresholdForm.usedMemoryMinorThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedMemoryMinorThreshold.errorMessage = 'Value must be greater than 0';
						return false;
					}
					if(thresholds.usedMemoryMajorThreshold <= thresholds.usedMemoryMinorThreshold) {
						this.memoryUsageThresholdForm.usedMemoryMajorThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedMemoryMajorThreshold.errorMessage = 'Value must be greater than minor threshold';
						return false;
					}
					if(thresholds.usedMemoryCriticalThreshold <= thresholds.usedMemoryMajorThreshold) {
						this.memoryUsageThresholdForm.usedMemoryCriticalThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedMemoryCriticalThreshold.errorMessage = 'Value must be greater than major threshold';
						return false;
					}
					if(thresholds.usedMemoryCriticalThreshold >= 100) {
						this.memoryUsageThresholdForm.usedMemoryCriticalThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedSwapMinorThreshold.errorMessage = 'Value must be less than 100';
						return false;
					}
					
					if(thresholds.usedSwapMinorThreshold < 1) {
						this.memoryUsageThresholdForm.usedSwapMinorThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedSwapMinorThreshold.errorMessage = 'Value must be greater than 0';
						return false;
					}
					if(thresholds.usedSwapMajorThreshold <= thresholds.usedSwapMinorThreshold) {
						this.memoryUsageThresholdForm.usedSwapMajorThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedSwapMajorThreshold.errorMessage = 'Value must be greater than minor threshold';
						return false;
					}
					if(thresholds.usedSwapCriticalThreshold <= thresholds.usedSwapMajorThreshold) {
						this.memoryUsageThresholdForm.usedSwapCriticalThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedSwapCriticalThreshold.errorMessage = 'Value must be greater than major threshold';
						return false;
					}
					if(thresholds.usedSwapCriticalThreshold >= 100) {
						this.memoryUsageThresholdForm.usedSwapCriticalThreshold.$invalid = true;
						this.memoryUsageThresholdForm.usedSwapMinorThreshold.errorMessage = 'Value must be less than 100';
						return false;
					}
					
					return true;
				};
				
				$scope.save = function(thresholds) {
					AlertThresholds.saveMemoryUsageThresholds($routeParams.host, thresholds).then(function() {
						$scope.alerts.push({ type: 'success', msg: 'Saved successfully'});
					}, function() {
						$scope.alerts.push({ type: 'danger', msg: 'Could not save settings to ElasticSearch'});
					});
				};
			}
		});