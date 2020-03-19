package pt.ulisboa.tecnico.cmov.foodist.async;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.net.URL;

public class GuessCampusTask extends AsyncTask<String, Integer, JSONObject> {


    @Override
    protected JSONObject doInBackground(String... strings) {
        String url = strings[0];

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                                    new ResponseListener(), new ErrorListener());

        return null;
    }
}

class ResponseListener implements Response.Listener<JSONObject> {

    @Override
    public void onResponse(JSONObject response) {

    }
}

class ErrorListener implements Response.ErrorListener {

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO: Handle error

    }
}