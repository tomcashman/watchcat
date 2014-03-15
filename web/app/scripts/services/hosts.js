'use strict';

angular.module('linuxGraphApp').factory('Hosts',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				list : function() {
					return ElasticSearch.search({
						index : 'linux-graph',
						type : 'host',
						size : 500,
						body : {
							sort : [ {
								"host" : "asc"
							}, "_score" ],
							query : {
								"match_all" : { }
							}
						}
					});
				}
			};
		} ]);