package activemq.xmg.com.activemq_mqtt.moudle.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;


import java.text.SimpleDateFormat;

import activemq.xmg.com.activemq_mqtt.Constants;
import activemq.xmg.com.activemq_mqtt.R;
import activemq.xmg.com.activemq_mqtt.moudle.util.SharePreferenceUtils;


public class AlarmManager {

	private static AlarmManager alarmManager;
	private final NotificationManager notificationManager;
	private int alarmState;
	private Context context;
	private Vibrator vibrator;
	Handler handler;
	long vibratorEndTime = 0l;
	int vibratorTime = 15000;
	private String TAG = "AlarmManager";
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	int id = 100;

	private AlarmManager(Context context) {
		this.context = context;
		notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("1","Alarm", NotificationManager.IMPORTANCE_HIGH);
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
			notificationManager.createNotificationChannel(channel);
		}
		handler = new Handler();
		alarmState = SharePreferenceUtils.getPrefInt(Constants.KEY_ALARM_STATE, Constants.ALARM_STATE_NORMAL);
		vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
	}
	public static void init(Context context){
		if(alarmManager == null) {
			alarmManager = new AlarmManager(context);
		}
	}
	public static AlarmManager getDefault(){
		return alarmManager;
	}

	public int getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(int alarmState) {
		this.alarmState = alarmState;
	}

	public  void destroy() {
		context = null;
		handler = null;
		alarmManager = null;
	}

	public void startAlarm(AlarmBean alarmBean){
		Log.e(TAG, "startAlarm: "+alarmBean.msg);
		switch (alarmState){
			case Constants.ALARM_STATE_NORMAL:

			case Constants.ALARM_STATE_VIB:
				if (System.currentTimeMillis() < vibratorEndTime) {
					stopAlarm();
				}
				long[] patter = {1000, 1000};
				vibrator.vibrate(patter, 0);
				handler.postDelayed(() -> AlarmManager.this.stopAlarm(), vibratorTime);
				vibratorEndTime = System.currentTimeMillis() + vibratorTime;
			case Constants.ALARM_STATE_MUTE:
				sendCustomNofitication(alarmBean,id++);
				break;
		}
	}
	public void startAlarm(int notificationId,AlarmBean alarmBean){
		Log.e(TAG, "startAlarm: "+alarmBean.msg);
		switch (alarmState){
			case Constants.ALARM_STATE_NORMAL:

			case Constants.ALARM_STATE_VIB:
				if (System.currentTimeMillis() < vibratorEndTime) {
					stopAlarm();
				}
				long[] patter = {1000, 1000};
				vibrator.vibrate(patter, 0);
				handler.postDelayed(() -> AlarmManager.this.stopAlarm(), vibratorTime);
				vibratorEndTime = System.currentTimeMillis() + vibratorTime;
			case Constants.ALARM_STATE_MUTE:
				sendCustomNofitication(alarmBean,notificationId);
				break;
		}
	}
	public void sendNofitication(AlarmBean alarmBean){
		sendCustomNofitication(alarmBean,id++);
	}
	public void sendCustomNofitication(AlarmBean alarmBean,int notificationId){
		RemoteViews contentview = new RemoteViews(context.getPackageName(), R.layout.layout_alarm_notification);
		contentview.setTextViewText(R.id.title,"预警等级："+alarmBean.level);
		contentview.setImageViewResource(R.id.icon,R.mipmap.ic_launcher);
		contentview.setTextViewText(R.id.time,alarmBean.time);
		contentview.setTextViewText(R.id.content,alarmBean.msg);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"1");
		Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher)
				.setAutoCancel(true).build();
		notification.bigContentView = contentview;
		notification.contentView = contentview;

		notificationManager.notify(notificationId,notification);

	}
	private void stopAlarm(){
		if(vibrator!=null){
			vibrator.cancel();
		}
	}
}
