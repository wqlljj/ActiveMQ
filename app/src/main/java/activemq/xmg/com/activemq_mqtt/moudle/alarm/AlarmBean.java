package activemq.xmg.com.activemq_mqtt.moudle.alarm;



import java.text.SimpleDateFormat;
import java.util.Date;


public class AlarmBean {
	public static final int LEVEL_CARE = 1;
	public static final int LEVEL_SERIOUS = 2;
	public static final int LEVEL_FATAL = 3;
	public static final int TYPE_VOICE = 0;
	public static final int TYPE_VIBRATOR = 1;
	public static final int TYPE_NOTIFY = 2;
	public static final int TYPE_MSG = 3;
	public String time;
	//预警等级 1关注 2严重  3异常严重
	int level;
	//告警提示类型  语音/震动  弹窗  通知栏
	int type;
	//预警信息
	String msg;


	public AlarmBean(int level, int type, String msg) {
		this.level = level;
		this.type = type;
		this.msg = msg;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		time =sdf.format(new Date(System.currentTimeMillis()));
	}


	public AlarmBean(String time, int level, int type, String msg) {
		this.time = time;
		this.level = level;
		this.type = type;
		this.msg = msg;
	}


	public AlarmBean() {
	}


	public String getTime() {
		return this.time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public int getLevel() {
		return this.level;
	}


	public void setLevel(int level) {
		this.level = level;
	}


	public int getType() {
		return this.type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public String getMsg() {
		return this.msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "time='" + time + '\'' +
				", level=" + level +
				", msg='" + msg;
	}
}
