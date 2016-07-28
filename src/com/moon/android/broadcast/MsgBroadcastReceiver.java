package com.moon.android.broadcast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.moon.android.iptv.arb.film.Configs;
import com.moon.android.iptv.arb.film.MyApplication;
import com.mooncloud.android.looktvb.R;
import com.moonclound.android.iptv.util.Logger;
import com.moonclound.android.iptv.util.UpdateData;
import com.moonclound.android.view.GGTextView;

public class MsgBroadcastReceiver extends BroadcastReceiver {

	private Context mContext;
	private Logger logger = Logger.getInstance();
	
	private Activity mActivity;
	
	private GGTextView mScrollTextview;
	
	public MsgBroadcastReceiver(Activity activity){
		mActivity=activity;
		mScrollTextview=(GGTextView)mActivity.findViewById(R.id.marquee_text);
		mScrollTextview.init(mActivity.getWindowManager());
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext=context;
		if(Configs.BroadCast.APP_GET_MSG.equals(intent.getAction())){
			showMsg(MyApplication.appMsg);
		} 
		if(Configs.BroadCast.UPDATE_MSG.equals(intent.getAction())){
 			UpdateData updata = MyApplication.updateData;
			String appUrl = updata.getUrl();
//			String msg = updata.getMsg();
			initPopWindow(appUrl, updata); 
		} 
	}

	/*private MyToast mToast;
	private void showMsg(String paramString) {
		if(null == mToast)
			mToast = new MyToast(mContext);
		if ((paramString != null) && (!paramString.equals(""))) {
			mToast.remove();
			mToast.setFocusable(false);
			mToast.setGravity(53, 1275, 10, 20);
			HorizontalMessageView localHorizontalMessageView = new HorizontalMessageView(mContext, paramString);
			localHorizontalMessageView.setTextColor(-1);
			localHorizontalMessageView.setTextSize(28);
			localHorizontalMessageView.startScroll();
			mToast.setView(localHorizontalMessageView);
			mToast.show();
			return;
		}
		mToast.remove();
	}*/
	
	private void showMsg(String msg) {
		mScrollTextview.setText(msg);
		mScrollTextview.init(mActivity.getWindowManager());
		mScrollTextview.startScroll();
	}
	
	private String mUpdateUrl;
	private String mUpdateMsg;
	private Button mBtnSubmit;
	private Button mBtnCancel;
	private PopupWindow mUpdatePopupWindow;
	public static final int START_DOWNLOAD = 0x11111;
	
	@SuppressLint("NewApi")
	private void initPopWindow(final String appUrl,
			UpdateData updata) {
		try{
			mUpdateUrl = appUrl;
			String updateMsg = updata.getMsg();
			logger.i("updata.getType() = " + updata.getType());
			boolean isMustUpdate = "0".equals(updata.getType()) ? true : false;
			mUpdateMsg = null == updateMsg ? "" : updateMsg;
			View view = LayoutInflater.from(mContext).inflate(R.layout.update_dialog, null);
			mBtnSubmit = (Button) view.findViewById(R.id.dialog_submit);
			mBtnCancel = (Button) view.findViewById(R.id.dialog_cancel);
			mBtnSubmit.setOnClickListener(mDialogClickListener);
			if(!isMustUpdate){
				logger.i("Not must update");
				mBtnCancel.setOnClickListener(mDialogClickListener);
			} else{
				logger.i("Must update");
				mBtnCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						System.exit(0);
					}
				});
			}
			TextView textContent = (TextView) view.findViewById(R.id.text_content);
			Spanned text = Html.fromHtml(mUpdateMsg); 
			textContent.setText(text);
			
			Display display=mActivity.getWindowManager().getDefaultDisplay();
			Point point=new Point();
			display.getSize(point);
			int width = point.x;
			int height = point.y;
			mUpdatePopupWindow = new PopupWindow(view,width,height,true);
			mUpdatePopupWindow.showAsDropDown(view,0,0);
			mUpdatePopupWindow.setOutsideTouchable(false);
		}catch(Exception e){
			logger.e(e.toString());
		}
	}
	
	private OnClickListener mDialogClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mUpdatePopupWindow.dismiss();
			if(v == mBtnSubmit){
				Toast.makeText(mContext, R.string.start_download, Toast.LENGTH_LONG).show();
				downFile(mUpdateUrl);
			}
		}
	};
	
	private void downFile(final String paramString) {
		new Thread() {
			public void run() {
				try {
					DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
					HttpGet localHttpGet = new HttpGet(paramString.trim());
					HttpEntity localHttpEntity = localDefaultHttpClient
							.execute(localHttpGet).getEntity();
					localHttpEntity.getContentLength();
					InputStream localInputStream = localHttpEntity.getContent();
					FileOutputStream localFileOutputStream = null;
					byte[] arrayOfByte;
					if (localInputStream != null) {
						localFileOutputStream = mContext.openFileOutput(Configs.APK_NAME,1);
						arrayOfByte = new byte[1024];
						int j = 0;
						while ((j = localInputStream.read(arrayOfByte)) != -1) {
							localFileOutputStream.write(arrayOfByte, 0, j);
						}
						localFileOutputStream.flush();
					}
					if (localFileOutputStream != null)
						localFileOutputStream.close();
//					mHandler.sendEmptyMessage(START_DOWNLOAD);
//					down();
					update();
					return;
				} catch (ClientProtocolException localClientProtocolException) {
					localClientProtocolException.printStackTrace();
					return;
				} catch (IOException localIOException) {
					localIOException.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void update() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Configs.File.UPDATE_PATH + "/files/",
				Configs.APK_NAME)), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
	}
}
