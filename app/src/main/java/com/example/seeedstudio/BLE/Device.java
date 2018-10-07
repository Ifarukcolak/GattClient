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

import com.example.seeedstudio.BLE.R;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Device extends Activity implements OnItemClickListener {

	private ArrayList<BluetoothDevice> devices;
	private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
	private SimpleAdapter adapter;
	private Map<String, String> map = null;
	private ListView listView;
	private String DEVICE_NAME = "name";
	private String DEVICE_ADDRESS = "address";
	public static final int RESULT_CODE = 31;
	public final static String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
	public final static String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		listView = (ListView) findViewById(R.id.listView);
		devices = (ArrayList<BluetoothDevice>) MainActivity.mDevices;
		for (BluetoothDevice device : devices) {
            if(device.toString().contains("B8:27:EB:89:05:54"))
                //sendNotification();
			map = new HashMap<String, String>();
			map.put(DEVICE_NAME, device.getName());
			map.put(DEVICE_ADDRESS, device.getAddress());
			listItems.add(map);
		}
		adapter = new SimpleAdapter(getApplicationContext(), listItems,
				R.layout.list_item, new String[] { "name", "address" },
				new int[] { R.id.deviceName, R.id.deviceAddr });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		HashMap<String, String> hashMap = (HashMap<String, String>) listItems
				.get(position);
		String addr = hashMap.get(DEVICE_ADDRESS);
		String name = hashMap.get(DEVICE_NAME);

		Intent intent = new Intent(Device.this, Control.class);
		intent.putExtra(EXTRA_DEVICE_ADDRESS, addr);
		intent.putExtra(EXTRA_DEVICE_NAME, name);
		startActivity(intent);
		MainActivity.instance.finish();
		finish();
	}

}
