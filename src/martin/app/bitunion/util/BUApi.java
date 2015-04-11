package martin.app.bitunion.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.R;
import martin.app.bitunion.model.BUUser;

/**
 * Api methods communicating with server, using {@link Volley} as network
 * @see <a href="http://out.bitunion.org/viewthread.php?tid=10471436">Bitunion Api Documentation</a>
 */
public class BUApi {

    private static final String TAG = BUApi.class.getSimpleName();

    private static BUUser sLoggedinUser;

    private static String mUsername;
    private static String mPassword;
    private static String mSession;
    private static String rooturl;
    private static String baseurl;

    private static RequestQueue mApiQueue;

    public static Response.ErrorListener sErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Volley default error", error);
        }
    };

    public static boolean hasValidUser() {
        return mUsername != null && mPassword != null;
    }

    public static boolean isUserLoggedin() {
        return mSession != null && !mSession.isEmpty();
    }

    /**
     * Login current user with response listener
     */
    public static void tryLogin(final String username, final String password,
                                @NonNull final Response.Listener<JSONObject> responseListener,
                                @NonNull Response.ErrorListener errorListener) {
        if (username == null || password == null)
            return;
        String path = baseurl + "/bu_logging.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "login");
        params.put("username", username);
        params.put("password", password);
        httpPost(path, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                switch (BUApi.getResult(response)) {
                    case FAILURE:
                        Toast.makeText(BUApplication.getInstance(), R.string.login_fail, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        mSession = response.optString("session");
                        mUsername = username;
                        mPassword = password;
                        updateUser();
                        break;
                    case UNKNOWN:
                        Toast.makeText(BUApplication.getInstance(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
                        break;
                }
                responseListener.onResponse(response);
            }
        }, errorListener);
    }

    public static void tryLogin(@NonNull Response.Listener<JSONObject> responseListener,
                                @NonNull Response.ErrorListener errorListener) {
        tryLogin(mUsername, mPassword, responseListener, errorListener);
    }

    public static void logoutUser(@NonNull final Response.Listener<JSONObject> responseListener,
                                  @NonNull Response.ErrorListener errorListener) {
        if (mUsername == null || mPassword == null)
            return;
        String path = baseurl + "/bu_logging.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "logout");
        params.put("password", mPassword);
        appendUserCookie(params);
        httpPost(path, params, 1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (getResult(response) == Utils.Result.SUCCESS) {
                    mUsername = null;
                    mPassword = null;
                    mSession = null;
                    saveUser(BUApplication.getInstance());
                }
                responseListener.onResponse(response);
            }
        }, errorListener);
    }

    /**
     * Send post without attachment
     * @param tid thread id
     * @param message message to be sent
     */
    public static void postNewPost(int tid, String message,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        if (tid <= 0 || message == null || message.isEmpty())
            return;
        String path = baseurl + "/bu_newpost.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "newreply");
        params.put("tid", Integer.toString(tid));
        params.put("message", message);
        params.put("attachment", "0");
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    /**
     * Post a new thread on specified forum
     * @param fid Forum id
     * @param title New thread subject
     * @param message New thread message
     */
    public static void postNewThread(int fid, String title, String message,
                                     Response.Listener<JSONObject> responseListener,
                                     Response.ErrorListener errorListener) {
        if (fid < 0 || title == null || title.isEmpty() || message == null || message.isEmpty())
            return;
        String path = baseurl + "/bu_newpost.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "newthread");
        params.put("fid", Integer.toString(fid));
        params.put("subject", title);
        params.put("message", message);
        params.put("attachment", "0");
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    /**
     * Get user profile
     * @param uid user id
     */
    public static void getUserProfile(int uid,
                                      Response.Listener<JSONObject> responseListener,
                                      Response.ErrorListener errorListener) {
        if (uid <= 0)
            return;
        String path = baseurl + "/bu_profile.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "profile");
        params.put("uid", Integer.toString(uid));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    /**
     * Get user profile
     * @param userName user name, passing null will read profile of user himself
     */
    public static void getUserProfile(String userName,
                                      Response.Listener<JSONObject> responseListener,
                                      Response.ErrorListener errorListener) {
        if (userName == null)
            userName = mUsername;
        String path = baseurl + "/bu_profile.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "profile");
        params.put("queryusername", userName);
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    /**
     * Get threads of forum
     * @param fid Forum id
     * @param from from index
     * @param to to index
     */
    public static void readThreads(int fid, int from, int to,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_thread.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "thread");
        params.put("fid", Integer.toString(fid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void readPostList(int tid, int from, int to,
                                    Response.Listener<JSONObject> responseListener,
                                    Response.ErrorListener errorListener) {
        if (from < 0 || to < 0 || from > to)
            return;
        String path = baseurl + "/bu_post.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "post");
        params.put("tid", Integer.toString(tid));
        params.put("from", Integer.toString(from));
        params.put("to", Integer.toString(to));
        appendUserCookie(params);
        httpPost(path, params, 1, responseListener, errorListener);
    }

    public static void init(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        mUsername = config.getString("username", null);
        mPassword = config.getString("password", null);
        setNetType(BUApplication.settings.netType);

        mApiQueue = Volley.newRequestQueue(context);
        // Need to read database
        sLoggedinUser = null;
    }

    public static void saveUser(Context context) {
        SharedPreferences config = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putString("username", mUsername);
        editor.putString("password", mPassword);
        editor.apply();
    }

    private static void updateUser() {
        getUserProfile(null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (getResult(response) == Utils.Result.SUCCESS)
                    sLoggedinUser = new BUUser(response.optJSONObject("memberinfo"));
            }
        }, sErrorListener);
    }

    public static BUUser getLoggedinUser() {
        return sLoggedinUser;
    }

    public static void clearUser() {
        SharedPreferences config = BUApplication.getInstance()
                .getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        BUApplication.settings.netType = Constants.OUTNET;
        editor.putInt("nettype", Constants.OUTNET);
        editor.putString("username", null);
        editor.putString("password", null);
        editor.apply();
    }

    public static void setNetType(int net) {
        if (net == Constants.BITNET)
            rooturl = "http://www.bitunion.org";
        else if (net == Constants.OUTNET)
            rooturl = "http://out.bitunion.org";
        baseurl = rooturl + "/open_api";
    }

    private static void appendUserCookie(Map<String, String> params) {
        params.put("username", mUsername);
        params.put("session", mSession);
    }

    /**
     * A hacky wrapper method of {@link BUApi#httpPost(String, Map, int, Response.Listener, Response.ErrorListener)},
     * makes it recursively retry login action with an upper limit.
     * Api session will expire if try login on web, login again will fix this. This should be an api bug
     * @param retryLimit The limit of how many times should we retry login
     */
    private static void httpPost(final String path, final Map<String, String> params,
                                 final int retryLimit,
                                 final Response.Listener<JSONObject> responseListener,
                                 final Response.ErrorListener errorListener) {
        if (retryLimit <= 0)
            httpPost(path, params, responseListener, errorListener);
        else
            httpPost(path, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    switch (getResult(response)) {
                        case SUCCESS:
                            responseListener.onResponse(response);
                            break;
                        case FAILURE:
                            tryLogin(new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (getResult(response) == Utils.Result.SUCCESS) {
                                        appendUserCookie(params);
                                        httpPost(path, params, retryLimit - 1, responseListener, errorListener);
                                    }
                                }
                            }, errorListener);
                            break;
                        default:
                    }
                }
            }, errorListener);
    }

    private static void httpPost(final String path, final Map<String, String> params,
                                 final Response.Listener<JSONObject> responseListener,
                                 final Response.ErrorListener errorListener) {
        JSONObject postReq = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : params.entrySet())
                postReq.put(entry.getKey(), URLEncoder.encode(entry.getValue(), HTTP.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "BUILD " + path + " >> " + postReq.toString());
        mApiQueue.add(new JsonObjectRequest(Request.Method.POST, path, postReq, responseListener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.d(TAG, path + " >> " + new String(response.data));
                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                Log.d(TAG, path + " >> " + new String(volleyError.networkResponse.data), volleyError);
                return super.parseNetworkError(volleyError);
            }
        });
    }

    public static Utils.Result getResult(JSONObject response) {
        if ("fail".equals(response.optString("result")))
            return Utils.Result.FAILURE;
        if ("success".equals(response.optString("result")))
            return Utils.Result.SUCCESS;
        return Utils.Result.UNKNOWN;
    }

    public static String getRootUrl() {
        return rooturl;
    }

    public static String getImageAbsoluteUrl(String shortUrl) {
        String path;
        path = shortUrl;
        path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org", rooturl);
        path = path.replaceAll("^images/", rooturl + "/images/");
        path = path.replaceAll("^attachments/", rooturl + "/attachments/");
        return path;
    }

}