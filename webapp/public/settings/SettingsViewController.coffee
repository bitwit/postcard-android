appModule.controller 'SettingsViewController',
  ['$scope', '$rootScope', '$route', '$location', 'PostcardDataService', 'networkAccount',
  ($scope, $rootScope, $route, $location, dataService, networkAccount) ->

    PostcardApplication.showBackButton()

    $scope.networkAccount = networkAccount

    $scope.deleteNetworkAccount = ->
      dataService.deleteNetworkAccount $scope.networkAccount
      $location.path "/write/"

    # Listeners
    $rootScope.$on '$routeChangeStart', ->
      console.log 'settings view route change start'
      dataService.updateNetworkAccount networkAccount

]