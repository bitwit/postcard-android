appModule.factory "PostcardPlatformService", ["$rootScope", "$window", "PostcardAndroidMock", ($rootScope, $window, PostcardAndroidMock) ->
  service = {}

  # check if we are connected to the android application and add a mock if necessary
  if !$window.PostcardApplication?
    console.log 'setting up android mock', PostcardAndroidMock
    $window.PostcardApplication = PostcardAndroidMock

  service.captureMedia = ->
    $window.PostcardApplication.captureMedia()

  service.openLibrary = ->

  return service

]