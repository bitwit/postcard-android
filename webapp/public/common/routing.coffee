appModule.config ['$routeProvider', ($routeProvider) ->
  $routeProvider

    .when '/write/',
      controller: 'WriteViewController',
      templateUrl: 'WriteView'
    .when '/progress/',
      controller: 'ProgressViewController',
      templateUrl: 'ProgressView'
    .when '/setup/',
      controller: 'NetworkSetupViewController',
      templateUrl: 'NetworkSetupView'
    .when '/settings/:networkAccountId/',
      controller: 'SettingsViewController',
      templateUrl: 'SettingsView'
      resolve:
        networkAccount: ["$q", "$route", "$rootScope", ($q, $route, $rootScope) ->
          console.log "go to settings #{$route.current.params.networkAccountId}"
          deferred = $q.defer()

          for networkAccount in $rootScope.networkAccounts
            if String(networkAccount.id) is $route.current.params.networkAccountId
              deferred.resolve networkAccount
              break

          deferred.reject "Not Found"
          return deferred.promise
        ]

  $routeProvider
    .otherwise
      redirectTo: '/write/'
]