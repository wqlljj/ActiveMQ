package activemq.xmg.com.activemq_mqtt.bean;

import android.util.Log;

/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/10/26 0026
 */

public class Message {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_HEARTBEAT = 1;
    public static final int TYPE_DEVICE = 2;
    public static final int TYPE_ALARM = 3;

    public Long time;
    public String name;
    public String data;
    //类型
    public int type = 0;

    public transient boolean isLeft=true;


    public Message(String data, boolean isLeft) {
        time = System.currentTimeMillis();
        this.data = data;
        this.isLeft = isLeft;
    }

    public Message(String name, int type, String data, boolean isLeft) {
        time = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.isLeft = isLeft;
        this.data = data == null?getMsg(type):data;
    }

    private String getMsg(int type) {
        switch (type){
            case TYPE_DEVICE:
                return "获取设备信息";
        }
        return "指令："+type;
    }

    public Message(String name, String data, boolean isLeft) {
        time = System.currentTimeMillis();
        type = Message.TYPE_MESSAGE;
        this.name = name;
        this.data = data;
        this.isLeft = isLeft;
        Log.i("MQTT", "Message: "+this);
    }

    @Override
    public String toString() {
        return "Message{" +
                "time=" + time +
                ", name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", type=" + type +
                ", isLeft=" + isLeft +
                '}';
    }
}
