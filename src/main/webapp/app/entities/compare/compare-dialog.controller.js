(function() {
    'use strict';

    angular
        .module('terminalApp')
        .controller('CompareDialogController', CompareDialogController);

    CompareDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Compare'];

    function CompareDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Compare) {
        var vm = this;

        vm.compare = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.similarity=new Object();
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.compare.id !== null) {
                Compare.update(vm.compare, onSaveSuccess, onSaveError);
            } else {
                Compare.save(vm.compare, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('terminalApp:compareUpdate', result);
            //$uibModalInstance.close(result);
            vm.isSaving = false;
            vm.similarity = result;
        }

        function onSaveError () {
            vm.isSaving = false;
            vm.similarity=new Object();
        }


        vm.setImage1 = function ($file, compare) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        compare.image1 = base64Data;
                        compare.image1ContentType = $file.type;
                    });
                });
            }
        };

        vm.setImage2 = function ($file, compare) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        compare.image2 = base64Data;
                        compare.image2ContentType = $file.type;
                    });
                });
            }
        };

    }
})();
