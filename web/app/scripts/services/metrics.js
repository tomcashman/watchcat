'use strict';

angular.module('linuxGraphApp').factory('Metrics',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				getLoadAverage : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'load',
						size : ((endTime - startTime) + 1),
						body : {
							sort : [ {
								"timestamp" : "asc"
							}, "_score" ],
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							}
						}
					});
				},
				getMemoryUsage : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'memory',
						size : ((endTime - startTime) + 1),
						body : {
							sort : [ {
								"timestamp" : "asc"
							}, "_score" ],
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							}
						}
					});
				},
				getDiskUsage : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'disks',
						size : ((endTime - startTime) + 1),
						body : {
							sort : [ {
								"timestamp" : "asc"
							}, "_score" ],
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							}
						}
					});
				}
			};
		} ]);