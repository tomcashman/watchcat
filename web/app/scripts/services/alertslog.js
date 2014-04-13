'use strict';

angular.module('watchcatApp').factory('AlertsLog',
		[ "ElasticSearch", function(ElasticSearch) {
			var esType = 'threshold';
			return {
				getLogs : function(hostname, pageNumber) {
					return ElasticSearch.search({
						index : 'watchcat-alerts',
						size : 15,
						from: (pageNumber - 1) * 15,
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
				},
				count : function(hostname) {
					return ElasticSearch.count({
						index : 'watchcat-alerts',
						body : {
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