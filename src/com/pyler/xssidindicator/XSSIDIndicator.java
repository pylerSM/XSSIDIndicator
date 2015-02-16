package com.pyler.xssidindicator;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XSSIDIndicator implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		if (!"com.android.systemui".equals(lpparam.packageName)) {
			return;
		}
		findAndHookMethod("com.android.systemui.statusbar.policy.Clock",
				lpparam.classLoader, "updateClock", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						final Context context = (Context) XposedHelpers
								.getObjectField(param.thisObject, "mContext");
						WifiManager wifiManager = (WifiManager) context
								.getSystemService(Context.WIFI_SERVICE);
						WifiInfo wifiInfo = wifiManager.getConnectionInfo();
						String wifiSSID = wifiInfo.getSSID().replace("\"", "");
						TelephonyManager manager = (TelephonyManager) context
								.getSystemService(Context.TELEPHONY_SERVICE);
						String carrier = manager.getNetworkOperatorName();
						TextView tv = (TextView) param.thisObject;
						String text = tv.getText().toString();
						String customtext = tv.getText().toString();
						boolean hasGSM = (manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE);
						XSharedPreferences settings = new XSharedPreferences(
								XSSIDIndicator.class.getPackage().getName());
						int maxLength = Integer.valueOf(settings.getString(
								"max_length", "7"));
						boolean showCarrier = settings.getBoolean(
								"show_carrier", true);
						if (showCarrier && hasGSM && !(carrier.isEmpty())) {
							if (carrier.length() > maxLength) {
								carrier = carrier.substring(0, maxLength);
							}
							customtext = carrier + " " + text;
						}
						if (!(wifiSSID.contains("unknown ssid")
								|| wifiSSID.contains("0x") || wifiSSID
								.isEmpty())) {
							if (wifiSSID.length() > maxLength) {
								wifiSSID = wifiSSID.substring(0, maxLength);
							}
							customtext = wifiSSID + " " + text;
						}
						tv.setText(customtext);
					}
				});
	}
}
