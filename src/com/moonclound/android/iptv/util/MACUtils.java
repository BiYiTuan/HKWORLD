package com.moonclound.android.iptv.util;

import android.annotation.SuppressLint;
import java.io.FileInputStream;
import java.io.IOException;

@SuppressLint("DefaultLocale")
public class MACUtils {

	public static String getMac() {
		FileInputStream localFileInputStream;
		String str = "";
		try{
			localFileInputStream = new FileInputStream("/sys/class/net/eth0/address");
			byte[] arrayOfByte = new byte[17];
			localFileInputStream.read(arrayOfByte, 0, 17);
			str = new String(arrayOfByte);
			localFileInputStream.close();
			if (str.contains(":"))
				str = str.replace(":", "").trim();
			if (str.contains("-"))
				str = str.replace("-", "").trim();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return str.toLowerCase();
//		return "005551ab0004";
	}
}
