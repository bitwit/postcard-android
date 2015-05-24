

appModule.service "PostcardAndroidMock", ["$http", "$window", ($http, $window) ->

  PostcardApplication =

    isMock: yes

    showBackButton: ->

    hideBackButton: ->

    loadNetworks: ->
      $http
        url:'/data/networks.json'
      .then (response) ->
        console.log 'network response', response
        $window.PostcardUI.networksDidChange response.data

    loadNetworkAccounts: ->
      $http
        url: '/data/accounts.json'
      .then (response) ->
        console.log 'network accounts response', response
        $window.PostcardUI.networkAccountsDidChange response.data

    deleteNetworkAccount: (networkAccountStringObj) ->

    grantAccess: (networkName) ->

    postActivity: (activityStringObj) ->

]

###
  PostcardUI.mediaAdded = (uri, type) ->
    console.log 'UI Bridge recognizes media capture'
    dataService.currentActivity.messageMedia = new MessageMedia()
    dataService.currentActivity.messageMedia.imageLocation = uri #.replace "file://", ""
    $rootScope.apply()
    return
###