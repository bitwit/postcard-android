appModule.run ["$rootScope", "$window", "$location", "$timeout", "PostcardDataService", ($rootScope, $window, $location, $timeout, dataService) ->
  $window.PostcardUI = PostcardUI = {}

  PostcardUI.home = ->
    $location.path '/write/'
    $rootScope.$apply()

  PostcardUI.send = ->
    $rootScope.$broadcast 'postActivity'
    $location.path '/progress/'
    $rootScope.$apply()

  PostcardUI.networkAccountsDidChange = (value) ->
    console.log 'network accounts change ', value
    if Array.isArray value
      #all networks
      #todo: check old and new for diff
      $rootScope.networkAccounts.length = 0
      for networkAccount in value
        $rootScope.networkAccounts.push networkAccount
    else if value?
      #new network
      $rootScope.networkAccounts.push value

    $rootScope.reorderNetworkAccounts()
    if !$window.PostcardApplication.isMock
      $rootScope.$apply()
    return

  PostcardUI.networksDidChange = (value) ->
    if Array.isArray value
      #all networks
      #todo: check old and new for diff
      console.log 'new networks', value
      $rootScope.networks = value
      if !$window.PostcardApplication.isMock
        $rootScope.$apply()
    else if value?
      #new network
      if !$window.PostcardApplication.isMock
        $rootScope.$apply()
    return

  PostcardUI.mediaAdded = (uri, type) ->
    console.log 'UI Bridge recognizes media capture'
    dataService.currentActivity.messageMedia = new MessageMedia()
    dataService.currentActivity.messageMedia.imageLocation = uri #.replace "file://", ""
    if !$window.PostcardApplication.isMock
      $rootScope.$apply()
    return

  PostcardUI.networkProgress = (data) ->
    console.log JSON.stringify data
    for networkAccount in dataService.currentActivity.networks
      if data.id is networkAccount.id
        networkAccount.progress = data.progress
        break;
    $rootScope.$apply()
    return

  PostcardUI.networkPostComplete = (data) ->
    console.log 'network post complete'
    for networkAccount, i in dataService.currentActivity.networks
      if data.id is networkAccount.id
        dataService.currentActivity.networks.splice i, 1
        break;

    # reassing orders of remaining networks for visual acknowledgement
    for networkAccount, i in dataService.currentActivity.networks
      networkAccount.order = i

    if !dataService.currentActivity.networks.length
      console.log 'we are done posting'
      $timeout ->
        dataService.newSocialActivity()
        $location.path '/write/'
        return
      , 1000
    else
      console.log 'still more networks...'

    $rootScope.$apply()
    return

]