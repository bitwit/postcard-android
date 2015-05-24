###
  Device setup
###
user_agent = navigator.userAgent;
Device =
  isAndroid: user_agent.toLowerCase().indexOf("android") >= 0
  isIOS: (user_agent.match(/iPhone/i) || user_agent.match(/iPod/i) || user_agent.match(/iPad/i))?
Device.isMobile = Device.isAndroid or Device.isIOS

###
  Helper JS functions
###
replaceAt = (string, start, end, substitute) ->
  string.substring(0, start) + substitute + string.substring(end)

initializeApp = ->
  angular.element(document).ready ->
    angular.bootstrap(document, ['appModule'])

firstBy = (->
    # mixin for the `thenBy` property */
    extend = (f) ->
      f.thenBy = tb
      return f

    # adds a secondary compare function to the target function (`this` context)
    #  which is applied in case the first one returns 0 (equal)
    #  returns a new compare function, which has a `thenBy` method as well */
    tb = (y)->
      x = this
      return extend (a, b)->
        return x(a, b) or y(a, b)

    return extend
)()

###
  Begin Angular
###
appModule = angular.module 'appModule', ['postcard-templates','ngRoute', 'ngTouch', 'ngAnimate']

