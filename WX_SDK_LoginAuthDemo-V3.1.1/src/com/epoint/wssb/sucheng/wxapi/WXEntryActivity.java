package com.epoint.wssb.sucheng.wxapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.epoint.wssb.sucheng.Constants;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth.Resp;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * @author andli
 * @date 2016年6月15日 下午2:33:00
 * @annotation
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// IWXAPI：第三方APP和微信通信的接口
	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
		// 如果分享的时候，该界面没有开启，那么微信开始这个activity时，会调用onCreate，所以这里要处理微信的返回结果
		api.handleIntent(getIntent(), this);
	}

	// 如果分享的时候，该已经开启，那么微信开始这个activity时，会调用onNewIntent，所以这里要处理微信的返回结果
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq arg0) {
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	// 比如：在微信完成文本分享操作后，回调第三方APP
	@Override
	public void onResp(BaseResp resp) {
		String result;
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			// 处理登录授权成功回调,获取token（注意：新版的SDK，可以直接获取Token）
			// 第一种
			Bundle bundle = new Bundle();
			resp.toBundle(bundle);
			Resp sp = new Resp(bundle);
			String token = sp.token;
			
			// 第二种
			// String token = ((SendAuth.Resp) resp).token;  
			Toast.makeText(WXEntryActivity.this, token,Toast.LENGTH_SHORT).show();
			
			// 特别说明：新版SDK貌似不支持获取OpenId，官方文档也没有更新，也没有获取OpenID的具体说明
			
			result = "授权成功!";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "发送取消";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "发送拒绝";
			break;
		default:
			result = "发送返回";
			break;
		}

		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
		
		this.finish();
	}

	private void requestOpenId(String token) {
		// 开启线程来发起网络请求
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					String urlstr = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="+Constants.APP_ID+"+&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
					// 获得URL对象
					URL url = new URL(urlstr);
					// 获得HttpURLConnection对象
					connection = (HttpURLConnection) url.openConnection();
					// 默认为GET请求
					connection.setRequestMethod("GET");
					// 设置 链接 超时时间
					connection.setConnectTimeout(8000);
					// 设置 读取 超时时间
					connection.setReadTimeout(8000);
					// 设置是否从HttpURLConnection读入，默认为true
					connection.setDoInput(true);
					connection.setDoOutput(true);

					// 请求相应码是否为200（OK）
					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						// 下面对获取到的输入流进行读取
						InputStream in 			= connection.getInputStream();
						BufferedReader reader 	= new BufferedReader(new InputStreamReader(in));
						StringBuilder response 	= new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						Message message = new Message();
						message.what = 1;
						message.obj  = response.toString();
						handler.sendMessage(message);
					} 

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// 关闭连接
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();

	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == 1){
				Toast.makeText(WXEntryActivity.this, "返回值:"+msg.obj.toString(), Toast.LENGTH_LONG).show();
			}
		};
	};
}
