appModule.directive 'pcNetworkProgressCell', [ () ->
  replace: yes
  link: (scope, element, attrs, ctrl) ->

  controller: [ "$scope", "$rootScope", ($scope, $rootScope) ->

    console.log 'network progress cell for' + JSON.stringify $scope.networkAccount

    $scope.frontViewStyles =  ->
      "-webkit-transform": "translate3d(0, " + ($scope.networkAccount.order * 60) + 'px' + ", 0)"

    $scope.networkIconStyles = ->
      "-webkit-mask": "url(img/icon-#{$scope.networkAccount.networkId}@2x.png)"
      "-webkit-mask-size": "32px 32px"

    $scope.hostIconStyles = ->
      leftPosition = -60 + $scope.cellXOffset
      if $scope.cellXOffset >= 60
        leftPosition = 0
      return {
        "-webkit-transform": "translate3d(#{leftPosition + 'px'}, 0, 0)"
        "background-color": if $scope.cellXOffset >= 60 then "#024883" else "#888"
      }

    $scope.progressStyles = ->
      return {
        width: "#{$scope.networkAccount.progress}%"
      }

  ]
  templateUrl: 'PCNetworkProgressCell'

]