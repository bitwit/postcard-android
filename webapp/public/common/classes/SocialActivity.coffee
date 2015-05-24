class SocialActivity

  constructor: ->
    @networks = []
    @date = ""
    @message = ""
    @tags = ""
    @messageLink = null # MessageLink
    @messageMedia = null # MessageMedia
    @postIds = {}

    @hostId = ""
    @hostNetwork = null #the host name i.e. 'twitter'

  addNetwork: (network) ->
    @networks.push network

  messageWithLink: ->
    if (@messageLink != null) # there is a link
      return @message + " " + @messageLink.url
    return @message

  messageWithLinkFittingCharacterLimit: (charLimit) ->
    message = @message
    if (this.messageLink != null)  #there is a link
      if (message.length() > (charLimit - 23)) #message alone is too long to accompany link
        message = message.substring(0, charLimit - 26) + "..."
      return message + " " + @messageLink.url
    else if (message.length() > charLimit)  #message is text only but too long (this shouldn't ever occur due to other rules in Postcard, but still here for thoroughness)
      message = message.substring(0, charLimit)

    return message