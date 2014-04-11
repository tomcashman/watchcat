'use strict';

angular.module('watchcatApp').directive('flotLegend', function() {
	return {
		restrict : 'E',
		template : '<div></div>',
		scope : {
			dataset: '='
		},
		link : function(scope, element, attributes) {
			return scope.$watch('dataset', function(dataset) {
				var targetDiv = $(element);
				var flotElement = targetDiv.closest('.panel-body').find('.legend');
				if(flotElement == null)
					return;

				var html = flotElement.html();
				flotElement.remove();
				targetDiv.html(html);
			}, true);
		}
	};
});