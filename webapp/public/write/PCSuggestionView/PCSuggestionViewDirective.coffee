appModule.directive 'pcSuggestionView', [ ->
  replace: true
  link: (scope, element, attrs, ctrl) ->
    console.log 'suggestion view link'

  controller: [ "$scope", "$rootScope", ($scope, $rootScope) ->
    console.log 'suggestion view controller'
    $scope.isHidden = yes
    $scope.textFilter = ""
    $scope.suggestions = [
      "canada"
      "coffee",
      "crazy",
      "ff",
      "follow",
      "followfriday",
      "interesting",
      "love",
      "mlb",
      "mondays",
      "music",
      "news",
      "nofilter",
      "nfl",
      "nhl",
      "nba",
      "ontario",
      "postcard",
      "quote",
      "swag",
      "TGIF",
      "throwbackthursday",
      "toronto",
      "useful"
    ]

    $scope.$on 'currentSpecialWordText', ($e, text) ->
      console.log 'current special word', text
      $scope.textFilter = text

    $scope.suggestionSelected = ($e, suggestion) ->
      $e.stopPropagation()
      $e.stopImmediatePropagation()
      $e.preventDefault()
      console.log 'suggestionSelected', suggestion
      $rootScope.$broadcast 'specialWordSelected', suggestion

  ]
  template: """
      <ul class="suggestion-list hidden-{{isSuggestionViewHidden}}">
        <li class="suggestion" ng-click="suggestionSelected($event, suggestion)" ng-repeat="suggestion in suggestions | filter:textFilter">
          <span class="value">{{suggestion}}</span>
        </li>
      </ul>
  """
]