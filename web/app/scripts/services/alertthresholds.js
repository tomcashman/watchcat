'use strict';

angular.module('watchcatApp').factory('AlertThresholds',
		[ "ElasticSearch", function(ElasticSearch) {
			var esType = 'threshold';
			return {
				getLoadAverageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'load'
					});
				},
				saveLoadAverageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'load',
						body: thresholds
					});
				},
				getMemoryUsageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'memory'
					});
				},
				saveMemoryUsageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'memory',
						body: thresholds
					});
				},
				getDiskUsageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'disks'
					});
				},
				saveDiskUsageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'disks',
						body: thresholds
					});
				}
			};
		} ]);