package martin.app.bitunion.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import martin.app.bitunion.R;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

public class BUAppUtils {

	public static final int BITNET = 0;
	public static final int OUTNET = 1;
	public static final int MAIN_REQ = 1;
	public static final int MAIN_RESULT = 2;
	public static final int DISPLAY_REQ = 3;
	public static final int DISPLAY_RESULT = 4;

	public static final int EXIT_WAIT_TIME = 2000;

	public static final int POSTS_PER_PAGE = 40; // ÿҳ��ʾ��������
	public static final int THREADS_PER_PAGE = 40; // ÿҳ��ʾ��������

	public static final String QUOTE_HEAD = "<br><br><center><table border='0' width='90%' cellspacing='0' cellpadding='0'><tr><td>&nbsp;&nbsp;����(?:\\[<a href='[\\w\\.&\\?=]+?'>�鿴ԭ��</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
	public static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
	public static final String QUOTE_REGEX = QUOTE_HEAD
			+ "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

	public static final String NETWRONG = "�������";
	public static final String LOGINFAIL = "��¼ʧ��";
	public static final String POSTFAILURE = "����ʧ��";
	public static final String POSTSUCCESS = "���ͳɹ���ˢ�²鿴�ظ�";
	public static final String POSTEXECUTING = "��Ϣ������...";
	public static final String USERNAME = "�û�";
	public static final String LOGINSUCCESS = "��¼�ɹ�";

	public String ROOTURL, BASEURL;
	public String REQ_LOGGING, REQ_FORUM, REQ_THREAD, REQ_PROFILE, REQ_POST,
			NEWPOST, NEWTHREAD, REQ_FID_TID_SUM;

	public String mUsername;
	public String mPassword;
	public String mSession;
	public int mNetType;

	public enum Result {
		SUCCESS, // �������ݳɹ���result�ֶ�Ϊsuccess
		FAILURE, // ��������ʧ�ܣ�result�ֶ�Ϊfailure
		SUCCESS_EMPTY, // �������ݳɹ������ֶ�û������
		SESSIONLOGIN, // obsolete
		NETWRONG, // û�з�������
		NOTLOGIN, // api��δ��¼
		UNKNOWN;
	};

	public BUAppUtils() {
		// TODO Auto-generated constructor stub
	}

	public void setNetType(int net) {
		mNetType = net;
		if (net == BITNET) {
			ROOTURL = "http://www.bitunion.org";
		} else if (net == OUTNET) {
			ROOTURL = "http://out.bitunion.org";
		}
		BASEURL = ROOTURL + "/open_api";
		REQ_LOGGING = BASEURL + "/bu_logging.php";
		REQ_FORUM = BASEURL + "/bu_forum.php";
		REQ_THREAD = BASEURL + "/bu_thread.php";
		REQ_PROFILE = BASEURL + "/bu_profile.php";
		REQ_POST = BASEURL + "/bu_post.php";
		REQ_FID_TID_SUM = BASEURL + "/bu_fid_tid.php";
		NEWPOST = BASEURL + "/bu_newpost.php";
		NEWTHREAD = BASEURL + "/bu_newpost.php";
	}

	public static JSONArray mergeJSONArray(JSONArray array1, JSONArray array2)
			throws JSONException {
		JSONArray array = new JSONArray(array1.toString());
		if (array2.length() > 0 && array2 != null)
			for (int i = 0; i < array2.length(); i++) {
				array.put(array2.getJSONObject(i));
			}
		return array;
	}

	public static ArrayList<BUThread> jsonToThreadlist(JSONArray array) {
		ArrayList<BUThread> list = new ArrayList<BUThread>();
		for (int i = 0; i < array.length(); i++)
			try {
				list.add(new BUThread(array.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// Log.v("page", "array parsed");
		return list;
	}

	public static ArrayList<BUPost> jsonToPostlist(JSONArray array) {
		ArrayList<BUPost> list = new ArrayList<BUPost>();
		for (int i = 0; i < array.length(); i++)
			try {
				list.add(new BUPost(array.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// Log.v("page", "array parsed");
		return list;
	}

	public static InputStream getImageVewInputStream(String imagepath)
			throws IOException {
		InputStream inputStream = null;
		URL url = new URL(imagepath);
		if (url != null) {
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setConnectTimeout(10000); // �������ӳ�ʱ
			httpConnection.setRequestMethod("GET");
			if (httpConnection.getResponseCode() == 200) {
				inputStream = httpConnection.getInputStream();
			}
		}
		return inputStream;
	}

	/**
	 * ��dipת��Ϊpx
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * ��pxת��Ϊdip
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static String replaceHtmlChar(String str) {
		String htmlstring = str;
		htmlstring = htmlstring.replace("&amp;", "&");
		htmlstring = htmlstring.replace("&nbsp;", " ");
		htmlstring = htmlstring.replace("&lt;", "<");
		htmlstring = htmlstring.replace("&gt;", ">");
		return htmlstring;
	}

}
