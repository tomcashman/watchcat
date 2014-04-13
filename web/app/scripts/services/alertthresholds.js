'use strict';

angular.module('watchcatApp').factory('AlertThresholds',
		[ "ElasticSearch", function(ElasticSearch) {
			var esType = 'threshold';
			return {
				getLoadAverageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'loadaverage'
					});
				},
				saveLoadAverageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'loadaverage',
						body: thresholds
					});
				},
				getMemoryUsageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'memoryusage'
					});
				},
				saveMemoryUsageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'memoryusage',
						body: thresholds
					});
				},
				getDiskUsageThresholds : function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'filesystems'
					});
				},
				saveDiskUsageThresholds : function(hostname, thresholds) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'filesystems',
						body: thresholds
					});
				}
			};
		} ]);