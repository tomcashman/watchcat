'use strict';

var calculateRanges = function(startTime, endTime) {	
	var diff = endTime - startTime;
	var interval = 1000;
	
	if(diff <= 120000) {
		interval = 1000;
	} else if(diff <= 600001) {
		interval = 10000;
	}else if(diff <= 1800001) {
		interval = 30000;
	} else if(diff <= 3600001) {
		interval = 60000;
	}
	
	var result = [];
	for(var i = startTime; i <= endTime; i += interval) {
		if(i === startTime) {
			result.push({
				"from": i
			});
		} else if(i === endTime) {
			result.push({
				"to": i - 1
			});
		} else {
			result.push({
				"from": i,
				"to": i + interval - 1
			});
		}
	}
	return result;
};

angular.module('linuxGraphApp').factory('Metrics',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				getLoadAverage : function(host, startTime, endTime) {
					var ranges = calculateRanges(startTime, endTime);
					return ElasticSearch.search({
						index : host,
						type : 'load',
						size: 0,
						body : {
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							},
							aggregations : {
						        "timestamps" : {
						            "range" : {
						                "field" : "timestamp",
						                "ranges" : ranges
						            },
						            "aggregations" : {
						            	"oneMinuteAverage" : {
						            		"avg" : {
						            			"field": "oneMinuteAverage"
						            		}
						            	},
						            	"fiveMinuteAverage" : {
						            		"avg" : {
						            			"field": "fiveMinuteAverage"
						            		}
						            	},
						            	"fifteenMinuteAverage" : {
						            		"avg" : {
						            			"field": "fifteenMinuteAverage"
						            		}
						            	},
						            	"cpuCores" : {
						            		"max" : {
						            			"field": "cpuCores"
						            		}
						            	}
						            }
						        }
						    }
						}
					});
				},
				getMemoryUsage : function(host, startTime, endTime) {
					var ranges = calculateRanges(startTime, endTime);
					return ElasticSearch.search({
						index : host,
						type : 'memory',
						size : 0,
						body : {
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							},
							aggregations : {
						        "timestamps" : {
						            "range" : {
						                "field" : "timestamp",
						                "ranges" : ranges
						            },
						            "aggregations" : {
						            	"totalMemory" : {
						            		"avg" : {
						            			"field": "totalMemory"
						            		}
						            	},
						            	"usedMemory" : {
						            		"avg" : {
						            			"field": "usedMemory"
						            		}
						            	},
						            	"totalSwap" : {
						            		"avg" : {
						            			"field": "totalSwap"
						            		}
						            	},
						            	"usedSwap" : {
						            		"avg" : {
						            			"field": "usedSwap"
						            		}
						            	}
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
					var ranges = calculateRanges(startTime, endTime);
					return ElasticSearch.search({
						index : host,
						type : 'bandwidth',
						size : 0,
						body : {
							query : {
								"range" : {
							        "timestamp" : {
							            "gte" : startTime,
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							},
							aggregations : {
						        "timestamps" : {
						            "range" : {
						                "field" : "timestamp",
						                "ranges" : ranges
						            },
						            "aggregations" : {
						            	"rx" : {
						            		"avg" : {
						            			"field": "rx"
						            		}
						            	},
						            	"tx" : {
						            		"avg" : {
						            			"field": "tx"
						            		}
						            	}
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
				},
				getNetworkConnections : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'connections',
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
