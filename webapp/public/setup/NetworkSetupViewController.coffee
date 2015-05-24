appModule.controller 'NetworkSetupViewController',
  ['$scope', 'PostcardDataService', 'PostcardPlatformService',
  ($scope, dataService, platformService)->

    PostcardApplication.showBackButton()

    $scope.networkIconStyles = (network) ->
      "-webkit-mask": "url(img/icon-#{network.slug}@2x.png)"
      "-webkit-mask-size": "32px 32px"

    $scope.networkSelected = (network) ->
      $scope.selectedNetwork = network

    $scope.fieldsContainerHeight = ->
      if !$scope.selectedNetwork?
        return height: 0
      fields = 1 + if $scope.selectedNetwork.setupFields? then $scope.selectedNetwork.setupFields.length else 0
      height = fields * 60
      return height: "#{height}px"

    $scope.grantAccess = ->
      dataService.grantAccess $scope.selectedNetwork.slug, null

]