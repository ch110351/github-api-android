package code.diegohdez.githubapijava.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import code.diegohdez.githubapijava.Activity.GetTokenActivity;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Utils.Request.API;
import okhttp3.Response;

public class GetToken extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = GetToken.class.getSimpleName();
    private GetTokenActivity context;
    private String token;

    public GetToken(GetTokenActivity context, String token) {
        this.context = context;
        this.token = token;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ANResponse doInBackground(String... strings) {
        ANRequest request = API.getToken(strings[0], token);
        return (ANResponse) request.executeForJSONObject();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        String message = "";
        int code = 0;
        if (response.isSuccess()) {
            Log.d(TAG, response.toString());
            Response anResponse = response.getOkHttpResponse();
            code = anResponse.code();
            if (code == 200) {
                message = "Auth successfully";
            }
            AppManager.getOurInstance().setToken(token);
        } else {
            ANError error = response.getError();
            message = "Error: " + error.getErrorDetail() + "\n" +
                    "Body: " + error.getErrorBody() + "\n" +
                    "Message: " + error.getMessage() + "\n" +
                    "Code: " + error.getErrorCode();
            Log.e(TAG, message);
            code = error.getErrorCode();
        }
        GetTokenActivity.responseMessage(context, message, code);
    }
}
