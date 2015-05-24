appModule.directive 'pcNetworkAccountCell', [ "$swipe", ($swipe) ->
  replace: yes
  link: (scope, element) ->
    scope.cellXOffset = 0
    scope.lastTouch = null
    scope.didTouchMove = no
    scope.shouldHighlight = no

    $swipe.bind element,
      start: (touch, event) ->
        if Device.isMobile and event.type is 'mousedown'
          return #for mobile, no mousedown event needed
        scope.lastTouch = touch
        scope.didTouchMove = no
        scope.shouldHighlight = yes
        scope.$apply()
      move: (touch, event) ->
        if Device.isMobile and event.type is 'mousemove'
          return #for mobile, no mousemove event needed
        scope.didTouchMove = yes
        scope.cellXOffset = touch.x - scope.lastTouch.x
        scope.shouldHighlight = no
        scope.$apply()

      end: (touch, event) ->
        if Device.isMobile and event.type is 'mouseup'
          return #for mobile, no mouseup event needed
        scope.lastTouch = null
        scope.shouldHighlight = no
        if !scope.didTouchMove
          scope.toggle()
        else
          scope.evaluateOffset()
        scope.cellXOffset = 0
        scope.didTouchMove = no
        scope.$apply()
      cancel: ->
        scope.lastTouch = null
        scope.cellXOffset = 0
        scope.didTouchMove = no
        scope.shouldHighlight = no
        scope.$apply()

  controller: [ "$scope", "$rootScope", "$location", ($scope, $rootScope, $location) ->

    console.log 'network account' + JSON.stringify $scope.networkAccount

    $scope.toggle = ->
      console.log 'network toggle'
      #$scope.shouldHighlight = no
      $scope.toggleNetworkAccount $scope.networkAccount

    $scope.evaluateOffset = ->
      if $scope.cellXOffset >= 60
        $scope.setNetworkAccountAsHost $scope.networkAccount
      else if $scope.cellXOffset <= -60
        console.log "go to settings #{$scope.networkAccount.id}"
        $location.path "/settings/#{$scope.networkAccount.id}"

    $scope.cellStyles =  ->
      "-webkit-transform": "translate3d(0, " + ($scope.networkAccount.order * 60) + 'px' + ", 0)"

    $scope.frontViewStyles = ->
      "-webkit-transform": "translate3d(#{$scope.cellXOffset + 'px'}, 0, 0)"

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

    $scope.settingsIconStyles = ->
      leftPosition = 60 + $scope.cellXOffset
      if $scope.cellXOffset <= -60
        leftPosition = 0
      return {
        "-webkit-transform": "translate3d(#{leftPosition + 'px'}, 0, 0)"
        "background-color": if $scope.cellXOffset <= -60 then "#024883" else "#888"
      }
  ]
  templateUrl: "PCNetworkAccountCell"

]