appModule.service "PostcardDataService", ["$rootScope", "$window", "PostcardPlatformService", ($rootScope, $window, platform) ->

  dataService = {}

  dataService.currentActivity = null

  dataService.initializeData = ->
    dataService.newSocialActivity()
    if $window.PostcardApplication?
      $window.PostcardApplication.loadNetworks();
      $window.PostcardApplication.loadNetworkAccounts();
    else
      console.log 'PostcardApplication not available'

  dataService.grantAccess = (networkName) ->
    $window.PostcardApplication.grantAccess networkName

  dataService.updateNetworkAccount = (networkAccount) ->
    $window.PostcardApplication.updateNetworkAccount JSON.stringify networkAccount

  dataService.deleteNetworkAccount = (networkAccount) ->
    $rootScope.deleteNetworkAccount networkAccount
    $window.PostcardApplication.deleteNetworkAccount JSON.stringify networkAccount

  dataService.newSocialActivity = ->
    dataService.currentActivity = new SocialActivity()

  dataService.postActivity = ->
    ###
    Post a new activity
    ###
    date = new Date()
    dataService.currentActivity.date = date.toString()

    for networkAccount in $rootScope.networkAccounts
      if networkAccount.isEnabled
        dataService.currentActivity.networks.push angular.copy networkAccount

    console.log JSON.stringify(dataService.currentActivity)
    $window.PostcardApplication.postActivity JSON.stringify dataService.currentActivity

  ###
  $rootScope
  ###
  $rootScope.networkAccounts = [] #initialize original array

  $rootScope.reorderNetworkAccounts = ->
    newArray = $rootScope.networkAccounts.slice 0
    newArray.sort(
      firstBy (a, b) ->
        b.isHost - a.isHost
      .thenBy (a, b) ->
        b.isEnabled - a.isEnabled
      .thenBy (a, b) ->
        Date.parse(a.lastActivated) - Date.parse(b.lastActivated)
      .thenBy (a, b) ->
        Date.parse(b.lastDeactivated) - Date.parse(a.lastDeactivated)
    )
    for account in $rootScope.networkAccounts
      for sortedNetwork, index in newArray
        if sortedNetwork is account
          account.order = index
          break

  $rootScope.toggleNetworkAccount = (networkAccount) ->
    networkAccount.isEnabled = !networkAccount.isEnabled
    if networkAccount.isEnabled
      networkAccount.lastActivated = new Date()
    else
      networkAccount.isHost = no
      networkAccount.lastDeactivated = new Date()
    $rootScope.reorderNetworkAccounts()
    dataService.updateNetworkAccount networkAccount

  $rootScope.deleteNetworkAccount = (theNetworkAccount) ->
    for networkAccount, i in $rootScope.networkAccounts
      if networkAccount.id is theNetworkAccount.id
        $rootScope.networkAccounts.splice i, 1
        break
    $rootScope.reorderNetworkAccounts()

  $rootScope.setNetworkAccountAsHost = (networkAccount) ->
    console.log 'set networkAccount', networkAccount, 'as host'
    networkAccount.canHostContent = yes  # TODO: Remove this and check properly
    if networkAccount.canHostContent
      for aNetworkAccount in $rootScope.networkAccounts
        if aNetworkAccount is networkAccount
          aNetworkAccount.isHost = !aNetworkAccount.isHost
        else if aNetworkAccount.isHost
          aNetworkAccount.isHost = no
          dataService.updateNetworkAccount aNetworkAccount

      if !networkAccount.isEnabled
        networkAccount.isEnabled = yes
        networkAccount.lastActivated = new Date()

      $rootScope.reorderNetworkAccounts()
      dataService.updateNetworkAccount networkAccount
    else
      alert "This network can't host content"

  dataService.initializeData()

  ###
    Listeners
  ###

  $rootScope.$on 'postActivity', ->
    dataService.postActivity()

  return dataService
]

