'use strict';

angular.module('watchcatApp').factory('AlertsLog',
		[ "ElasticSearch", function(ElasticSearch) {
			var esType = 'threshold';
			return {
				getLogs : function(hostname) {
					return ElasticSearch.search({
						index : 'watchcat-alerts',
						size : 15,
						body : {
							sort : [ {
								"timestamp" : {
									"order" : "desc"
								}
							} ],
							query : {
								match : {
									hostname : hostname
								}
							}
						}
					});
				}
			};
		} ]);