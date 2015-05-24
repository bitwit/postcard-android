appModule.controller 'WriteViewController',
  ['$scope', '$location', 'PostcardDataService', 'PostcardPlatformService',
  ($scope, $location, dataService, platformService) ->

    PostcardApplication.hideBackButton()

    $scope.mainTableState = "networks"
    $scope.message = ""
    $scope.characterCount = 140
    $scope.upgradeButtonTitle = ->
      "Thank you"

    $scope.socialActivity = dataService.currentActivity

    $scope.changeState = (state) ->
      $scope.mainTableState = state

    $scope.captureMedia = ->
      platformService.captureMedia()

    $scope.addNetwork = ->
      console.log 'add network'
      $location.path '/setup/'

    # Listeners
    $scope.$on 'textViewDidChange', ($e, message) ->
      $scope.message = message
      $scope.characterCount = 140 - message.length
      dataService.currentActivity.message = message
]

