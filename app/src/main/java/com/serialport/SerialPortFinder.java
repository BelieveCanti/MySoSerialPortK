/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class SerialPortFinder {

	public class Driver {
		public Driver(String name, String root) {
			mDriverName = name;
			mDeviceRoot = root;
		}
		private String mDriverName;
		private String mDeviceRoot;
		Vector<File> mDevices = null;
		public Vector<File> getDevices() {
			if (mDevices == null) {
				mDevices = new Vector<File>();
				File dev = new File("/dev");
				File[] files = dev.listFiles();
				int i;
				for (i=0; i<files.length; i++) {
					if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
						Log.d(TAG, "Found new device: " + files[i]);
						mDevices.add(files[i]);
					}
				}
			}
			return mDevices;
		}
		public String getName() {
			return mDriverName;
		}
	}

	private static final String TAG = "SerialPort";

	private Vector<Driver> mDrivers = null;

	/**
	 * Vector类实现了一个可增长的对象数组。与数组一样，它包含可以使用整数索引访问的组件。但是，Vector的大小可以根据需要增加或缩小
	 * ，以适应在创建Vector之后添加和删除项。 每个向量都试图通过维护一个容量和一个capacityIncrement来优化存储管理。容量总是至少与向量大小一样大;它通常更大，
	 * 因为随着组件被添加到vector中，vector的存储空间会按capacityIncrement的大小成块增加。
	 * 应用程序可以在插入大量组件之前增加向量的容量;这减少了增量再分配的数量。 这个类的迭代器和listIterator方法返回的迭代器是快速失败的:如果在迭代器创建后的任何时候，
	 * 以除了迭代器自己的remove或add方法以外的任何方式对vector进行了结构修改，迭代器将抛出ConcurrentModificationException异常。
	 * 因此，在面对并发修改时，迭代器会快速而干净地失败，而不是在未来不确定的时间发生任意的、不确定的行为。elements方法返回的Enumerations不会快速失败。
	 *
	 * 从Java 2平台v1.2开始，这个类进行了改进，以实现List接口，使其成为Java集合框架的成员。
	 * 与新的集合实现不同，Vector是同步的。如果不需要线程安全的实现，建议使用ArrayList来代替Vector。
	 */
	Vector<Driver> getDrivers() throws IOException {
		if (mDrivers == null) {
			mDrivers = new Vector<Driver>();
			LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
			String l;
			while((l = r.readLine()) != null) {
				// Issue 3:
				// Since driver name may contain spaces, we do not extract driver name with split()
				String drivername = l.substring(0, 0x15).trim();
				String[] w = l.split(" +");
				if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
					Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length-4]);
					mDrivers.add(new Driver(drivername, w[w.length-4]));
				}
			}
			r.close();
		}
		return mDrivers;
	}

	public String[] getAllDevices() {
		Vector<String> devices = new Vector<String>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
				Iterator<File> itdev = driver.getDevices().iterator();
				while(itdev.hasNext()) {
					String device = itdev.next().getName();
					String value = String.format("%s (%s)", device, driver.getName());
					devices.add(value);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}

	public String[] getAllDevicesPath() {
		Vector<String> devices = new Vector<String>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
				Iterator<File> itdev = driver.getDevices().iterator();
				while(itdev.hasNext()) {
					String device = itdev.next().getAbsolutePath();
					devices.add(device);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}
}
