appModule.controller 'PCNavigationController', ['$scope', '$rootScope', '$route', '$location', ($scope, $rootScope, $route, $location) ->

  ###
    Properties
  ###
  $scope.sizes =
    window:
      height: null
      width: null
    header:
      height: 0
    textarea:
      height: null
    toolbar:
      height: 64
    table:
      height: null
    setupTable:
      height: null
  $scope.rootSection = {title:"Postcard", slug: "postcard"}
  $scope.stack = [$scope.rootSection]
  $scope.section = $scope.rootSection
  $scope.isSending = no

  ###
    Methods
  ###
  #Dimensions
  $scope.getWidth = ->
    return window.innerWidth

  $scope.getHeight = ->
    return window.innerHeight

  $scope.$watch $scope.getWidth, (newValue, oldValue) ->
    $scope.sizes.window.width = newValue

  $scope.$watch $scope.getHeight, (newValue, oldValue) ->
    if !$scope.sizes.window.height?
      $scope.sizes.window.height = newValue
      remainingHeight = newValue - $scope.sizes.header.height - $scope.sizes.toolbar.height
      $scope.sizes.textarea.width = if $scope.sizes.window.width >= 568 then 568 else $scope.sizes.window.width
      $scope.sizes.textarea.height = remainingHeight * 0.50
      $scope.sizes.table.height = remainingHeight * 0.50
      $scope.sizes.setupTable.height = remainingHeight - 64 # horizontal table considered

  window.onresize = ->
    $scope.$apply()

  #Navigation Controls
  $scope.pushSection = (section) ->
    $scope.stack.push section
    $scope.section = section

  $scope.popSection = ->
    if $scope.stack.length == 1
      return console.warn 'WARNING: attempting to pop navigation to empty stack'
    $scope.stack.pop()
    $scope.section = $scope.stack[$scope.stack.length - 1]

  $scope.back = ->
    $location.path '/write/'

  $scope.send = ->
    console.log 'send the message'
    $rootScope.$broadcast 'postActivity'
    $location.path '/progress/'

  ###
    Listeners
  ###
  $scope.$on 'pushSection', ($e, section) ->
    $scope.pushSection section

  $scope.$on 'popSection', ->
    $scope.popSection()

  $rootScope.$on "$routeChangeSuccess", ($currentRoute, $previousRoute) ->
    console.log 'route change success'

]
