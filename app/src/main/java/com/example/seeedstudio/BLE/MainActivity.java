/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.seeedstudio.BLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.example.seeedstudio.BLE.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;//10s
	private Dialog mDialog;
	public static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
	public static MainActivity instance = null;
	private Map<String, String> map = null;
	private ListView listView;
	private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
	SearchDevicesView search_device_view;
	public String[] DEVICE_NAME =new String[100];
	public String[] DEVICE_ADDRESS = new String[100];
	public List<String> Device_Name=new ArrayList<>();
	public List<String> Device_Adress=new ArrayList<>();
	ListViewAdapter adapter;
	Timer mTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		listView = (ListView) findViewById(R.id.listView);
		adapter = new ListViewAdapter();
		listView.setAdapter(adapter);
		//search_device_view = (SearchDevicesView) findViewById(R.id.search_device_view);
		//search_device_view.setWillNotDraw(false);
		mTimer = new Timer();
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		 
		
	/*	search_device_view.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if(search_device_view.getValidTouchEvent(event)){
					Log.d(null, "==== lawliet test onTouch======");
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						if(search_device_view.isSearching()){
							if(mTimer != null){
								Log.d(null, "====cancel scan ====");
								mTimer.cancel();
							}
						}else{
							scanLeDevice();
							mTimer = new Timer();
							mTimer.schedule(new TimerTask() {
								@Override
								public void run() {
									Intent deviceListIntent = new Intent(getApplicationContext(),Device.class);
									startActivity(deviceListIntent);
									if(search_device_view.isSearching()){
										search_device_view.setSearching(false);
									}
								}
							}, SCAN_PERIOD);
						}
					}
				}
				return false;
			}
		});*/

		instance = this;
	}

	public void showRoundProcessDialog(Context mContext, int layout) {
		OnKeyListener keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
								 KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME
						|| keyCode == KeyEvent.KEYCODE_SEARCH) {
					return true;
				}
				return false;
			}
		};

		mDialog = new AlertDialog.Builder(mContext).create();
		mDialog.setOnKeyListener(keyListener);
		mDialog.show();

		mDialog.setContentView(layout);
	}

	public void startService(View view) {
		scanLeDevice();
	}

	public void scanLeDevice() {
		mDevices.clear();
		new Thread() {

			@Override
			public void run() {
				mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				searchDevices();
			}
		}.start();
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
							 byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (device != null) {
						if (mDevices.indexOf(device) == -1)
							mDevices.add(device);
					}
				}
			});
		}
	};

	public void searchDevices() {
		Device_Name.clear();
		Device_Adress.clear();
		for (BluetoothDevice device : mDevices) {
			if (device.toString().contains("B8:27:EB:89:05:54"))
				sendNotification();
			Device_Adress.add(device.getAddress());
			Device_Name.add(device.getName());
		}
		Runnable mRunnable = new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
			}
		};;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listView.invalidateViews();
			}
		});

		//listView.setOnItemClickListener(this);
		scanLeDevice();
	}

	public void sendNotification() {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("Raspberry Notification")
						.setContentText("Hello :)");
		// Gets an instance of the NotificationManager service//
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(001, mBuilder.build());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		System.exit(0);
	}




		/*
		Button btn = (Button)findViewById(R.id.btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				scanLeDevice();

				showRoundProcessDialog(MainActivity.this, R.layout.loading_process_dialog_anim);

				Timer mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						Intent deviceListIntent = new Intent(getApplicationContext(),
								Device.class);
						startActivity(deviceListIntent);
						mDialog.dismiss();
					}
				}, SCAN_PERIOD);
			}
		});*/
/*
		scanLeDevice();

		showRoundProcessDialog(MainActivity.this, R.layout.loading_process_dialog_anim);

		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Intent deviceListIntent = new Intent(getApplicationContext(),
						Device.class);
				startActivity(deviceListIntent);
				mDialog.dismiss();
			}
		}, SCAN_PERIOD);
*/


	class ListViewAdapter extends BaseAdapter {
		Context context;
		LayoutInflater inflater;

		@Override
		public int getCount() { return Device_Name.size(); }

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView deviceAdress;
			TextView deviceName;
			convertView =getLayoutInflater().inflate(R.layout.list_item,null);//list_item_row dan yeni bir view oluşturuyoruz

			deviceAdress = (TextView) convertView.findViewById(R.id.deviceAddr);
			deviceName = (TextView) convertView.findViewById(R.id.deviceName);
			if(!Device_Adress.isEmpty())
				deviceAdress.setText("Device Adress:" + Device_Adress.get(position));
				//deviceAdress.setText("Device Adress:" + DEVICE_ADDRESS[position]);
			if(!Device_Name.isEmpty())
				deviceName.setText("Device Name:" + Device_Name.get(position));
			//deviceName.setText("Device Name:" + DEVICE_NAME[position]);

			return convertView;
		}
		private  boolean isNullOrBlank(String s)
		{
			return (s==null || s.trim().equals(""));
		}
		@Override
		public boolean isEmpty() {
			return false;
		}

	}
}