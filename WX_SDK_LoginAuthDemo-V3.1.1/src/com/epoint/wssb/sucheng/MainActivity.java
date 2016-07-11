package com.epoint.wssb.sucheng;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class MainActivity extends Activity implements OnClickListener{
	// IWXAPI：第三方APP和微信通信的接口
	private IWXAPI api;
	// 登录授权
	private Button authButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);
		// 向微信终端注册你的id
		api.registerApp(Constants.APP_ID);
		
		authButton = (Button)findViewById(R.id.authButton);
		authButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 登录授权
		case R.id.authButton:
			loginAuth();
			break;

		default:
			break;
		}

	}
	
	// 登录授权
	private void loginAuth() {
		Toast.makeText(this, "Auth", Toast.LENGTH_SHORT).show();
		final SendAuth.Req req 	= new SendAuth.Req();
		req.scope 				= "snsapi_userinfo";
		req.state 				= "wechat_sdk_demo_test";
		api.sendReq(req);
	}

}
