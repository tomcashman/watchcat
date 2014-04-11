'use strict';

angular.module('watchcatApp').factory('Hosts',
		[ "ElasticSearch", function(ElasticSearch) {
			return {
				list : function() {
					return ElasticSearch.search({
						index : 'watchcat',
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