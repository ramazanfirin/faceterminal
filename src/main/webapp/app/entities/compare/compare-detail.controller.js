(function() {
    'use strict';

    angular
        .module('terminalApp')
        .controller('CompareDetailController', CompareDetailController);

    CompareDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'Compare'];

    function CompareDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, Compare) {
        var vm = this;

        vm.compare = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('terminalApp:compareUpdate', function(event, result) {
            vm.compare = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
