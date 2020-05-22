(function() {
    'use strict';
    angular
        .module('terminalApp')
        .factory('Compare', Compare);

    Compare.$inject = ['$resource'];

    function Compare ($resource) {
        var resourceUrl =  'api/compares/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
