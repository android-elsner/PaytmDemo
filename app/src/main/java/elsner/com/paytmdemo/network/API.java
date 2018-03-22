package elsner.com.paytmdemo.network;

import elsner.com.paytmdemo.network.callback.CallbackChecksum;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by elsner on 22/3/18.
 */

public interface API {

    @FormUrlEncoded()
    @POST("generateChecksum.php")
    Single<CallbackChecksum> generateChecksum(@Field("MID") String merchantId,
                                              @Field("ORDER_ID") String orderId,
                                              @Field("CUST_ID") String customerId,
                                              @Field("CHANNEL_ID") String channelId,
                                              @Field("TXN_AMOUNT") String taxAmount,
                                              @Field("WEBSITE") String website,
                                              @Field("CALLBACK_URL") String callbackUrl,
                                              @Field("INDUSTRY_TYPE_ID") String industryType);
}
