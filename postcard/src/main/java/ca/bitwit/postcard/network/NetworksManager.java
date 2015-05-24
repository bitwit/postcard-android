package ca.bitwit.postcard.network;

import android.util.Log;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.bitwit.postcard.MessageLink;
import ca.bitwit.postcard.PostcardAdaptor;

/**
 * Created by kylenewsome on 2014-10-07.
 */
public enum NetworksManager {

    INSTANCE;
    public static final String DATEFORMATNOW = "yyyy-MM-dd HH:mm:ss";
    public PostcardAdaptor adaptor;
    public SocialActivity currentSocialActivity;
    public int currentNetworkCompletionCount;

    private boolean isInHostingMode;

    public void postActivity(SocialActivity activity){
        currentNetworkCompletionCount = 0;
        currentSocialActivity = activity;
        isInHostingMode = currentSocialActivity.networks.get(0).isHost;
        if (isInHostingMode) {
            Network network = currentSocialActivity.networks.get(0);
            String beginPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex: 0}";
            adaptor.postUIMessage("socialActivityDidBeginPostingToNetwork", beginPost);
            try {
                network.postUpdate(currentSocialActivity);
            }
            catch (Exception e) {
                e.printStackTrace();
                currentNetworkCompletionCount++;
                String errorPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex: 0, error:\"Couldn't start up network\" }";
                adaptor.postUIMessage("socialActivityPostingToNetworkDidFailWithError", errorPost);
                evaluateNextOption();
            }
        } else {
            for (int i = 0; i < currentSocialActivity.networks.size(); i++) {
                Network network = currentSocialActivity.networks.get(i);
                String beginPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + i + "}";
                adaptor.postUIMessage("socialActivityDidBeginPostingToNetwork", beginPost);
                try {
                    network.postUpdate(currentSocialActivity);
                }
                catch (Exception e) {
                    String errorPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + i + ", error:\"Couldn't start up network\" }";
                    adaptor.postUIMessage("socialActivityPostingToNetworkDidFailWithError", errorPost);
                    e.printStackTrace();
                    currentNetworkCompletionCount++;
                    evaluateNextOption();
                }
            }
        }
    }

    public void networkUpdatedProgress(Network network, double progress){
        String progressPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + indexForNetwork(network) + ", progress:" + progress + "}";
        adaptor.postUIMessage("socialActivityPostingToNetworkUpdatedWithProgress", progressPost);
    }

    public void networkUpdateMessage(Network network, String message){
        String messagePost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + indexForNetwork(network) + ", message:\"" + message + "\"}";
        adaptor.postUIMessage("socialActivityPostingToNetworkUpdatedWithMessage", messagePost);
    }

    public void networkDidFailWithError(Network network, String error){
        currentNetworkCompletionCount++;
        String messagePost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + indexForNetwork(network) + ", error:\"" + error + "\"}";
        adaptor.postUIMessage("socialActivityPostingToNetworkDidFailWithError", messagePost);
        evaluateNextOption();
    }

    public void networkDidCompletePostingWithInfo(Network network, JSONObject info){
        if (isInHostingMode && currentNetworkCompletionCount == 0) {
            //host completed and has a permalink for us
            MessageLink link = new MessageLink();
            try {
                link.url = info.getString("permalink");
                currentSocialActivity.hostId = info.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            link.title = currentSocialActivity.message;
            link.description = "Posted on " + now();
            currentSocialActivity.messageLink = link;
            currentSocialActivity.isFromAHost = true;
            currentSocialActivity.hostNetwork = network.name;
        }
        currentNetworkCompletionCount++;
        String completePost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + indexForNetwork(network) + "}";
        adaptor.postUIMessage("socialActivityDidCompletePostingToNetwork", completePost);
        evaluateNextOption();
    }

    private int indexForNetwork(Network network){
        for (int i = 0; i < currentSocialActivity.networks.size(); i++) {
            if(currentSocialActivity.networks.get(i) == network){
                return i;
            }
        }
        return -1;
    }

    private void evaluateNextOption() {
        if (isInHostingMode && currentNetworkCompletionCount == 1) {
            //host just completed, prepare remaining networks
            for (int i = 1; i < currentSocialActivity.networks.size(); i++) {
                Network network = currentSocialActivity.networks.get(i);
                String beginPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + i + "}";
                adaptor.postUIMessage("socialActivityDidBeginPostingToNetwork", beginPost);
                try {
                    network.postUpdate(currentSocialActivity);
                }
                catch (Exception e) {
                    String errorPost = "{socialActivity:" + currentSocialActivity.toString() + ", networkIndex:" + i + ", error:\"Couldn't start up network\" }";
                    adaptor.postUIMessage("socialActivityPostingToNetworkDidFailWithError", errorPost);
                    e.printStackTrace();
                    currentNetworkCompletionCount++;
                    evaluateNextOption();
                }
            }
        }

        if (currentNetworkCompletionCount >= currentSocialActivity.networks.size()) {
            Log.d("Postcard", "Posting activities completed");
            adaptor.postUIMessage("socialActivityDidComplete", currentSocialActivity.toString());
        }
    }

    private static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATNOW);
        return sdf.format(cal.getTime());
    }
}
