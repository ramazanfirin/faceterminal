(function() {
    'use strict';

    angular
        .module('terminalApp')
        .controller('CompareController', CompareController);

    CompareController.$inject = ['DataUtils', 'Compare'];

    function CompareController(DataUtils, Compare) {

        var vm = this;

        vm.compares = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadAll();

        function loadAll() {
            Compare.query(function(result) {
                vm.compares = result;
                vm.searchQuery = null;
            });
        }
    }
})();
