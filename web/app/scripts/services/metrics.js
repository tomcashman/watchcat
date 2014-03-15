'use strict';

angular.module('linuxGraphApp').factory('Metrics',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				getLoadAverage : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'load',
						size : 500,
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
						size : 500,
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