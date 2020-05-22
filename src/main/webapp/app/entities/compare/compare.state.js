(function() {
    'use strict';

    angular
        .module('terminalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('compare', {
            parent: 'entity',
            url: '/compare',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'terminalApp.compare.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/compare/compares.html',
                    controller: 'CompareController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('compare');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('compare-detail', {
            parent: 'compare',
            url: '/compare/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'terminalApp.compare.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/compare/compare-detail.html',
                    controller: 'CompareDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('compare');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Compare', function($stateParams, Compare) {
                    return Compare.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'compare',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('compare-detail.edit', {
            parent: 'compare-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compare/compare-dialog.html',
                    controller: 'CompareDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Compare', function(Compare) {
                            return Compare.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compare.new', {
            parent: 'compare',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compare/compare-dialog.html',
                    controller: 'CompareDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                url1: null,
                                url2: null,
                                image1: null,
                                image1ContentType: null,
                                image2: null,
                                image2ContentType: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('compare', null, { reload: 'compare' });
                }, function() {
                    $state.go('compare');
                });
            }]
        })
        .state('compare.edit', {
            parent: 'compare',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compare/compare-dialog.html',
                    controller: 'CompareDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Compare', function(Compare) {
                            return Compare.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compare', null, { reload: 'compare' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compare.delete', {
            parent: 'compare',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compare/compare-delete-dialog.html',
                    controller: 'CompareDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Compare', function(Compare) {
                            return Compare.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compare', null, { reload: 'compare' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
