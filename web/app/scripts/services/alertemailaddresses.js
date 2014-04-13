'use strict';

angular.module('watchcatApp').factory('AlertEmailAddresses',
		[ "ElasticSearch", function(ElasticSearch) {
			var esType = 'alert-destination';
			return {
				getEmailAddresses: function(hostname) {
					return ElasticSearch.getSource({
						index: hostname,
						type: esType,
						id: 'emailaddresses'
					});
				},
				saveEmailAddresses : function(hostname, addresses) {
					return ElasticSearch.index({
						index: hostname,
						type: esType,
						id: 'emailaddresses',
						body: addresses
					});
				}
			};
		} ]);