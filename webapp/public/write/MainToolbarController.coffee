appModule.controller 'MainToolbarController', ['$scope', ($scope) ->
  $scope.isSuggestionViewHidden = yes

  $scope.$on 'enteringState', ($e, state) ->
    console.log 'entering state', state
    $scope.isSuggestionViewHidden = (state is "PCMessageTextViewStateNormal")
]