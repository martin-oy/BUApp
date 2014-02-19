package martin.app.bitunion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.util.BUForum;
import martin.app.bitunion.util.PostMethod;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static int SCREENWIDTH;  
	public static int SCREENHEIGHT; 
	public static float PIXDENSITY;
	

	// ��̳�б���ͼ
	ExpandableListView listView;
	// ������̳�б�����
	ArrayList<BUForum> forumList = new ArrayList<BUForum>();
	// ������̳�б�����
	ArrayList<ArrayList<BUForum>> fArrayList = new ArrayList<ArrayList<BUForum>>();
	// ��������
	ArrayList<String> groupList;
	ForumListAdapter adapter;

	// ��̬����������Ӧ���д����������Ӳ���������session, username, password��Ϣ
	public static BUAppUtils network = new BUAppUtils();
	// �ϴΰ����ؼ���ʱ��
	long touchTime = 0;

	private UserLoginTask mLoginTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_main);
		
		getActionBar().setTitle("����FTP����");
		 
		Point size = new Point();
		WindowManager w = getWindowManager();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
		    w.getDefaultDisplay().getSize(size);
		    SCREENWIDTH = size.x;
		    SCREENHEIGHT = size.y; 
		}else{
		    Display d = w.getDefaultDisplay(); 
		    SCREENWIDTH = d.getWidth(); 
		    SCREENHEIGHT = d.getHeight(); 
		}
		PIXDENSITY = getResources().getDisplayMetrics().densityDpi;
		

		listView = (ExpandableListView) this.findViewById(R.id.listview);

		// ExpandableListView�ķ�����Ϣ
		groupList = new ArrayList<String>();
		groupList.add(0, "ϵͳ������");
		groupList.add(1, "ֱͨ����");
		groupList.add(2, "����������");
		groupList.add(3, "����������");
		groupList.add(4, "ʱ��������");

		// ��ȡ��̳�б���Ϣ
		String[] forumNames = getResources().getStringArray(R.array.forums);
		int[] forumFids = getResources().getIntArray(R.array.fids);
		int[] forumTypes = getResources().getIntArray(R.array.types);
		for (int i = 0; i < forumNames.length; i++) {
			forumList.add(new BUForum(forumNames[i], forumFids[i],
					forumTypes[i]));
		}
		// ת����̳�б���ϢΪ��ά���飬����ListViewAdapter����
		for (int i = 0; i < groupList.size(); i++) {
			ArrayList<BUForum> forums = new ArrayList<BUForum>();
			for (BUForum forum : forumList) {
				if (i == forum.getType()) {
					forums.add(forum);
				}
			}
			fArrayList.add(forums);
		}
		// Log.v("martin", fArrayList.get(0).get(0).getName());
		listView.setAdapter(new ForumListAdapter());

		readConfig();
		if (network.mUsername != null && !network.mUsername.isEmpty()) {
			mLoginTask = new UserLoginTask();
			mLoginTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	private class UserLoginTask extends AsyncTask<Void, Void, Result> {

		PostMethod postMethod = new PostMethod();

		@Override
		protected Result doInBackground(Void... params) {

			JSONObject postReq = new JSONObject();
			try {
				postReq.put("action", "login");
				postReq.put("username",
						URLEncoder.encode(network.mUsername, "utf-8"));
				postReq.put("password", network.mPassword);
				postMethod.setNetType(network.mNetType);
				return postMethod.sendPost(postMethod.REQ_LOGGING, postReq);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

		// �����¼���������toast��ʾ
		@Override
		protected void onPostExecute(final Result result) {
			mLoginTask = null;

			switch (result) {
			default:
				return;
			case FAILURE:
				showToast(BUAppUtils.LOGINFAIL);
				return;
			case NETWRONG:
				showToast(BUAppUtils.NETWRONG);
				return;
			case UNKNOWN:
				return;
			case SUCCESS:
			}
			showToast(BUAppUtils.USERNAME + " " + network.mUsername + " "
					+ BUAppUtils.LOGINSUCCESS);
			try {
				network.mSession = postMethod.jsonResponse.getString("session");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// finish();
		}

		@Override
		protected void onCancelled() {
			mLoginTask = null;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// ��ҪΪlogin_activity���������������
		Log.v("MainActivity", "Cookie>>" + network.mSession);
	}

	private void readConfig() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		network.mNetType = config.getInt("nettype", BUAppUtils.OUTNET);
		network.mUsername = config.getString("username", null);
		network.mPassword = config.getString("password", null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// ��¼��ť��ת��login_activity
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_login:
			if (network.mUsername == null || network.mUsername.isEmpty()) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivityForResult(intent, BUAppUtils.MAIN_REQ);
			} else {
				Intent intent = new Intent(this, MyinfoActivity.class);
				startActivity(intent);
			}
			break;
		case R.id.action_settings:
			//TODO
			showToast("���ܻ�δ���");
			break;
		default:}
		return true;
		// return super.onOptionsItemSelected(item);
	}

	// �õ�login_activity���ص�cookies
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == BUAppUtils.MAIN_RESULT
				&& requestCode == BUAppUtils.MAIN_REQ) {
			readConfig();
			network.mSession = data.getStringExtra("session");
			showToast(BUAppUtils.USERNAME + " " + network.mUsername + " "
					+ BUAppUtils.LOGINSUCCESS);
		}
	}

	// ExpandableListView�����ݽӿ�
	private class ForumListAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return fArrayList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = new TextView(MainActivity.this);
			textView.setBackgroundColor(getResources().getColor(
					R.color.blue_light));
			if (fArrayList.get(groupPosition).get(childPosition).getName().contains("--"))
				textView.setTextSize(16);
			else
				textView.setTextSize(20);
			textView.setPadding(60, 10, 0, 10);
			textView.setText(fArrayList.get(groupPosition).get(childPosition)
					.getName());
			textView.setTag(fArrayList.get(groupPosition).get(childPosition));
			// Log.v("martin", Integer.toString(textView.getId()));
			// �����������������ʱ��ɫ�����»������ɿ���ָ�
			textView.setOnTouchListener(new OnTouchListener() {

				double y;

				@Override
				public boolean onTouch(View v, MotionEvent motion) {
					if (motion.getAction() == MotionEvent.ACTION_DOWN) {
						v.setBackgroundColor(getResources().getColor(
								R.color.blue_view_selected));
						y = motion.getY();
					} else if (motion.getAction() == MotionEvent.ACTION_UP
							|| motion.getAction() == MotionEvent.ACTION_CANCEL) {
						v.setBackgroundColor(getResources().getColor(
								R.color.blue_light));
						return false;
					} else if (motion.getAction() == MotionEvent.ACTION_MOVE) {
						double disMoved;
						disMoved = Math.abs(y - motion.getY());
						if (disMoved > 5) {
							v.setBackgroundColor(getResources().getColor(
									R.color.blue_light));
						}
					}
					return false;
				}
			});
			// ע��OnClick�¼����������ת��DisplayActivity
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (network.mUsername != null && network.mPassword != null) {
						Intent intent = new Intent(MainActivity.this,
								DisplayActivity.class);
						intent.putExtra("fid", ((BUForum) v.getTag()).getFid());
						intent.putExtra("name",
								((BUForum) v.getTag()).getName());
						startActivityForResult(intent, BUAppUtils.MAIN_REQ);
					} else showToast("���ȵ�¼");
				}
			});
			return textView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return fArrayList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groupList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			final TextView textView = new TextView(MainActivity.this);
			textView.setBackgroundColor(getResources().getColor(
					R.color.blue_dark));
			textView.setTextSize(25);
			textView.setPadding(60, 10, 0, 10);
			textView.setText(groupList.get(groupPosition));
			return textView;
		}
		@Override
		public boolean hasStableIds() {
			return false;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}
	
	@Override
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();  
	    if((currentTime-touchTime)>= BUAppUtils.EXIT_WAIT_TIME) {  
	        showToast("�ٰ�һ���˳�����");
	        touchTime = currentTime;  
	    }else {  
	    	super.onBackPressed();  
	    }  
	}

	private void showToast(String text) {
		Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
	}

}
