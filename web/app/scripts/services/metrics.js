'use strict';

var getRangeInterval = function(startTime, endTime) {
	var diff = endTime - startTime;
	var interval = 1000;
	
	if(diff >= 120000) {
		interval = (diff / 60000) * interval;
	}
	return interval;
};

var calculateRanges = function(startTime, endTime, intervalValue) {
	var interval = null;
	if(!intervalValue) {
		interval = getRangeInterval(startTime, endTime);
	} else {
		interval = intervalValue;
	}
	
	var result = [];
	for(var i = startTime; i <= endTime; i += interval) {
		result.push({
			"from": i,
			"to": i + interval - 1
		});
	}
	return result;
};

angular.module('watchcatApp').factory('Metrics',
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
							            "gte" : endTime - getRangeInterval(startTime, endTime),
							            "lte" : endTime,
							            "boost" : 2.0
							        }
							    }
							}
						}
					});
				},
				getNetworkConnections : function(host, startTime, endTime) {
					var ranges = calculateRanges(startTime, endTime);
					return ElasticSearch.search({
						index : host,
						type : 'connections',
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
						            	"total" : {
				                    		"sum" : {
					                            "field" : "totalConnections"
					                        }
				                    	}
						            }
						        }
						    }
						}
					});
				}
			};
		} ]);
