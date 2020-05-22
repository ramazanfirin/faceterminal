(function() {
    'use strict';

    angular
        .module('terminalApp')
        .controller('CompareDeleteController',CompareDeleteController);

    CompareDeleteController.$inject = ['$uibModalInstance', 'entity', 'Compare'];

    function CompareDeleteController($uibModalInstance, entity, Compare) {
        var vm = this;

        vm.compare = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Compare.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
