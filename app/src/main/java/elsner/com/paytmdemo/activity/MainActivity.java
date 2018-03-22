package elsner.com.paytmdemo.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import elsner.com.paytmdemo.AppController;
import elsner.com.paytmdemo.R;
import elsner.com.paytmdemo.network.API;
import elsner.com.paytmdemo.network.RestAdapter;
import elsner.com.paytmdemo.network.callback.CallbackChecksum;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {
    private static final String TAG = "MainActivity";

    private API api;
    private CompositeDisposable disposable;
    private ProgressDialog progressDialog;

    private String merchantId, orderId, customerId, channelId, website,
            callbackUrl, taxAmount, industryType, checksumHash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing Views
        initViews();

    }

    private void initViews() {
        Button btnStart = findViewById(R.id.btnStart);

        disposable = new CompositeDisposable();
        api = RestAdapter.createAPI();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        merchantId = "XXXXXXXXXXXXXXXXXXXX";
        customerId = generateStringID();
        orderId = generateStringID();
        channelId = "XXX";
        website = "elsner.com";
        taxAmount = "1";
        callbackUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        industryType = "XXXXX";

        btnStart.setOnClickListener(v -> {
            // generate checksum if there is internet connection
            if (AppController.hasNetwork())
                generateChecksum();
            else
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        });
    }

    private String generateStringID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    private void generateChecksum() {
        disposable.add(api.generateChecksum(merchantId, orderId, customerId, channelId,
                taxAmount, website, callbackUrl, industryType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable1 -> showProgress())
                .doFinally(this::hideProgress)
                .subscribe(this::handleChecksumResponse, this::handleError));
    }

    private void handleChecksumResponse(CallbackChecksum data) {
        if (data != null) {
            if (data.getChecksum() != null) {
                checksumHash = data.getChecksum();
                initializePaytmPayment();
            }
        } else {
            Toast.makeText(this, "Error generating checksum", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePaytmPayment() {
        PaytmPGService paytmPGService = PaytmPGService.getStagingService();

        //creating a hashmap and adding all the required values
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", merchantId);
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", customerId);
        paramMap.put("CHANNEL_ID", channelId);
        paramMap.put("TXN_AMOUNT", taxAmount);
        paramMap.put("WEBSITE", website);
        paramMap.put("CALLBACK_URL", callbackUrl);
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", industryType);

        //creating a paytmOrder instance using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //initializing a PaytmPGService
        paytmPGService.initialize(order, null);

        //finally starting the paytm payment transaction using PaytmPGService
        paytmPGService.startPaymentTransaction(this, true, true, this);
    }

    private void handleError(Throwable throwable) {
        Log.d(TAG, "handleError: " + throwable.getLocalizedMessage());
    }

    public void showProgress() {
        progressDialog.show();
    }

    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (disposable != null && !disposable.isDisposed())
            disposable.clear();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        Log.d(TAG, "onTransactionResponse: " + inResponse.toString());
    }

    @Override
    public void networkNotAvailable() {
        Log.d(TAG, "networkNotAvailable: ");
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Log.d(TAG, "clientAuthenticationFailed: " + inErrorMessage);
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Log.d(TAG, "someUIErrorOccurred: " + inErrorMessage);
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Log.d(TAG, "onErrorLoadingWebPage: " + String.valueOf(iniErrorCode));
        Log.d(TAG, "onErrorLoadingWebPage: " + inErrorMessage);
        Log.d(TAG, "onErrorLoadingWebPage: " + inFailingUrl);
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Log.d(TAG, "onBackPressedCancelTransaction: ");
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Log.d(TAG, "onTransactionCancel: " + inErrorMessage);
        Log.d(TAG, "onTransactionCancel: " + inResponse);
    }
}
