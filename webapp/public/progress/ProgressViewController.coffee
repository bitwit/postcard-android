appModule.controller 'ProgressViewController',
  ['$scope', '$rootScope', 'PostcardDataService',
  ($scope, $rootScope, dataService) ->
    $scope.currentActivity = dataService.currentActivity

]

