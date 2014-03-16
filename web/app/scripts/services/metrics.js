'use strict';

angular.module('linuxGraphApp').factory('Metrics',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				getLoadAverage : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'load',
						size : Math.round(((endTime - startTime)/ 1000) + 1),
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
						size : Math.round(((endTime - startTime)/ 1000) + 1),
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
						size : Math.round(((endTime - startTime)/ 1000) + 1),
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
				getBandwidth : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'bandwidth',
						size : Math.round(((endTime - startTime)/ 1000) + 1),
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
				getProcesses : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'processes',
						size : Math.round(((endTime - startTime)/ 1000) + 1),
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