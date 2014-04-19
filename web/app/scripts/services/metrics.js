/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Cashman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
						            		"max" : {
						            			"field": "oneMinuteAverage"
						            		}
						            	},
						            	"fiveMinuteAverage" : {
						            		"max" : {
						            			"field": "fiveMinuteAverage"
						            		}
						            	},
						            	"fifteenMinuteAverage" : {
						            		"max" : {
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
						            		"max" : {
						            			"field": "totalMemory"
						            		}
						            	},
						            	"usedMemory" : {
						            		"max" : {
						            			"field": "usedMemory"
						            		}
						            	},
						            	"totalSwap" : {
						            		"max" : {
						            			"field": "totalSwap"
						            		}
						            	},
						            	"usedSwap" : {
						            		"max" : {
						            			"field": "usedSwap"
						            		}
						            	}
						            }
						        }
						    }
						}
					});
				},
				getDisks : function(host, startTime, endTime) {
					return ElasticSearch.search({
						index : host,
						type : 'disks',
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
								"disks" : {
									"terms" : { "script" : "doc['disk'].value" }
								}
							}
						}
					});
				},
				getDiskUsage : function(host, startTime, endTime, disk) {
					var ranges = calculateRanges(startTime, endTime);
					return ElasticSearch.search({
						index : host,
						type : 'disks',
						size : 0,
						body : {
							sort : [ {
								"timestamp" : "asc"
							}, "_score" ],
							query : {
								"filtered" : {
									query: {
										"range" : {
									        "timestamp" : {
									            "gte" : startTime,
									            "lte" : endTime,
									            "boost" : 2.0
									        }
									    }
									},
									filter: {
										"query" : {
											"match" : {
												"disk" : disk
											}
										}
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
						            	"size" : {
						            		"max" : {
						            			"field": "size"
						            		}
						            	},
						            	"used" : {
						            		"max" : {
						            			"field": "used"
						            		}
						            	}
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
						            		"max" : {
						            			"field": "rx"
						            		}
						            	},
						            	"tx" : {
						            		"max" : {
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
