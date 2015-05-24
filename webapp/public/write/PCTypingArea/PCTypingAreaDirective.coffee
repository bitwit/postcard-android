appModule.directive 'pcTypingArea', [ ->
  replace: yes
  link: (scope, element, attrs, ctrl) ->
    scope.textareaElement = element[0].children[1]
    console.log 'typing area element', scope.textareaElement

  controller: [ "$scope", "$rootScope", "$sce", "$timeout", ($scope, $rootScope, $sce, $timeout) ->
    $scope.message = ""
    $scope.styledMessage = ""
    $scope.editingWordRange = null

    $scope.currentSpecialRanges = []

    $scope.textChange = ->
      $scope.currentSpecialRanges.length = 0
      $scope.styledMessage = $scope.message
      isTagging = $scope.evaluateHashtags()
      isMentioning = $scope.evaluateMentions()
      console.log 'tagging?', isTagging, 'mentioning', isMentioning
      if !isTagging and !isMentioning
        $scope.$broadcast "enteringState", "PCMessageTextViewStateNormal"
      $scope.$emit 'textViewDidChange', $scope.message

      $scope.styledMessage = $sce.trustAsHtml $scope.styledMessage

    $scope.didFocus = ->
      $scope.$emit 'textViewDidBeginEditing'

    $scope.didBlur = ->
      #$scope.$broadcast 'textViewDidEndEditing'
      #$scope.$broadcast "enteringState", "PCMessageTextViewStateNormal"
      $scope.evaluateMessageLinks()

    $scope.textViewShouldChangeTextInRange = (text, range) ->
      if text is " "
        # or text hasSuffix:@"\n"
        $scope.state = "PCMessageTextViewStateNormal"
        $scope.$broadcast "enteringState", "PCMessageTextViewStateNormal"
        $scope.evaluateMessageLinks()
      return yes

    $scope.deleteBackward = ->
      if $scope.text.length == 0
          $scope.state = "PCMessageTextViewStateNormal";
          $scope.$broadcast "enteringState", "PCMessageTextViewStateNormal"

    $scope.evaluateMessageLinks = ->
      text = $scope.message
      urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/
      matches = []
      text.replace urlPattern, (match, p1, p2, p3, offset, string) ->
        #p1 is nondigits, p2 digits, and p3 non-alphanumerics
        matches.push match
        #console.log 'match,p1,p2,p3,offset,string', match, p1, p2, p3, offset, string
        return [p1, p2, p3].join ' - '

      if matches.length > 0
        url = matches[0]
        $scope.$broadcast 'addLinkToMessage', url
      else
        $scope.$broadcast 'noLinkInMessage'

    $scope.getMatches = (pattern, asRanges) ->
      text = $scope.message
      matches = []
      text.replace pattern, (match, p1, p2, p3, offset, string) ->
        #p1 is nondigits, p2 digits, and p3 non-alphanumerics
        console.log "(match, p1, p2, p3, offset, string) ->", match, ":", p1, ":", p2, ":", p3, ":", offset, ":", string
        if asRanges? and asRanges is yes
          matches.push [p2, match.length - 1]
        else
          matches.push match

      $scope.styledMessage = $scope.styledMessage.replace pattern, (match, p1, p2, p3, offset, string) ->
        return """<span class="highlight">#{match}</span>"""

      return matches

    $scope.evaluateHashtags = ->
      matches = $scope.getMatches /\B#([_a-z0-9]+)/ig, yes
      if matches.length > 0
        currentRange = [$scope.textareaElement.selectionStart, ($scope.textareaElement.selectionEnd - $scope.textareaElement.selectionStart)]
        $scope.currentSpecialRanges = $scope.currentSpecialRanges.concat matches
        isEditing = no
        for match in matches
          if currentRange[0] >= match[0] and currentRange[0] <= (match[0] + match[1] + 1)
            $scope.editingWordRange = match
            isEditing = yes
            break

        if isEditing
          $scope.state = "PCMessageTextViewStateHashtag"
          $scope.$broadcast "enteringState", "PCMessageTextViewStateHashtag"
          word = $scope.message.substr $scope.editingWordRange[0], $scope.editingWordRange[1] + 1
          word = word.replace "#", ""
          word = word.replace " ", ""
          $scope.$broadcast 'currentSpecialWordText', word
          return yes

      return no

    $scope.setTextForCurrentSpecialWord = (text) ->
      replacement = if $scope.state is "PCMessageTextViewStateHashtag" then "#" else "@"
      replacement += text + " "

      start = $scope.message.substring 0, $scope.editingWordRange[0]
      finish = $scope.message.substring $scope.editingWordRange[0] + $scope.editingWordRange[1] + 1

      $scope.message = start + replacement + finish
      $scope.textareaElement.selectionStart = $scope.editingWordRange[0] + replacement.length
      $scope.textareaElement.selectionEnd = $scope.textareaElement.selectionStart

    $scope.evaluateMentions = ->
      matches = $scope.getMatches /\B@([_a-z0-9]+)/ig, yes
      if matches.length > 0
        $scope.currentSpecialRanges = $scope.currentSpecialRanges.concat matches
        currentRange = [$scope.textareaElement.selectionStart, ($scope.textareaElement.selectionEnd - $scope.textareaElement.selectionStart)]
        isEditing = no
        for match in matches
          if currentRange[0] >= match[0] and currentRange[0] <= (match[0] + match[1] + 1)
            $scope.editingWordRange = match
            isEditing = yes
            break
        if isEditing
          $scope.state = "PCMessageTextViewStateMention"
          $scope.$broadcast "enteringState", "PCMessageTextViewStateMention"
          word = $scope.message.substr $scope.editingWordRange[0], $scope.editingWordRange[1] + 1
          word = word.replace "@", ""
          word = word.replace " ", ""
          $scope.$broadcast 'currentSpecialWordText', word
          return yes

      return no

    $scope.$on 'specialWordSelected', ($e, word) ->
      console.log 'specialWordSelected', word, $e
      $timeout ->
        $scope.setTextForCurrentSpecialWord word
        $scope.textareaElement.focus()
        $scope.state = "PCMessageTextViewStateNormal"
        $scope.$broadcast "enteringState", "PCMessageTextViewStateNormal"
      , 100

  ]
  template: """
      <div class="typing-area-container" style="width: {{sizes.textarea.width}}px;height: {{sizes.textarea.height}}px;">
        <div class="typing-area typing-highlighter" ng-bind-html='styledMessage'></div>
        <textarea class="typing-area" ng-model="message" ng-change="textChange()" ng-focus="didFocus()" ng-blur="didBlur()">
        </textarea>
      </div>
  """
]