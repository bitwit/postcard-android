package ca.bitwit.postcard.network;

import ca.bitwit.postcard.MessageAttachment;
import ca.bitwit.postcard.MessageLink;
import ca.bitwit.postcard.MessageMedia;

import java.lang.Boolean;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.io.File;
import java.util.List;

public class SocialActivity {

    public List<Network> networks;
    public String date;
    public String message;
    public String tags;
    public MessageLink messageLink;
    public MessageMedia messageMedia = null;
    //public Dictionary<> postIds;

    public Boolean isFromAHost;
    public String hostId;
    public String hostNetwork; //the host name i.e. 'twitter'

    public SocialActivity() {
        //init
        networks = new ArrayList<Network>();
    }

    public void addNetwork(Network network) {
        //this.networks[] = network;
    }

    public String messageWithLink() {
        if (this.messageLink != null) {   //there is a link
            return this.message + " " + this.messageLink.url;
        }
        return this.message;
    }

    public String messageWithLinkFittingCharacterLimit(Integer charLimit) {
        String message = this.message;
        if (this.messageLink != null) {   //there is a link
            if (message.length() > (charLimit - 23)) { //message alone is too long to accompany link
                message = message.substring(0, charLimit - 26) + "...";
            }
            return message + " " + this.messageLink.url;
        } else if (message.length() > charLimit) { //message is text only but too long (this shouldn't ever occur due to other rules in Postcard, but still here for thoroughness)
            message = message.substring(0, charLimit);
        }
        return message;
    }

}