package elsner.com.paytmdemo.network.callback;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by elsner on 22/3/18.
 */

public class CallbackChecksum {

    @SerializedName("CHECKSUMHASH")
    private String checksum;
    @SerializedName("ORDER_ID")
    private String orderId;
    @SerializedName("payt_STATUS")
    private String paytmStatus;

    public String getChecksum() {
        return checksum;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaytmStatus() {
        return paytmStatus;
    }
}
