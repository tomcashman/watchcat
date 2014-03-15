'use strict';

angular.module('linuxGraphApp').service('ElasticSearch', function(esFactory) {
	return esFactory({
		hosts : Configuration.elasticsearchnodes,
		maxKeepAliveTime : 3600000,
		maxRetries : 10,
		deadTimeout : 1000,
		apiVersion : "1.0"
	});
});