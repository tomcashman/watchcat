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
				}
			};
		} ]);