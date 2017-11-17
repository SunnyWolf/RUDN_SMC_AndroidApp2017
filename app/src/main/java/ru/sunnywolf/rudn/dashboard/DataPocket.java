package ru.sunnywolf.rudn.dashboard;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by whiteraven on 11/11/17.
 */

public class DataPocket{
    public static final int POCKET_LENGTH = 11;

    public static final int MAGIC_1 = 0x13;
    public static final int MAGIC_2 = 0x37;
    public static final int STOP = 0xFF;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_SET = 1;
    public static final int TYPE_REQUEST = 2;
    public static final int TYPE_STATUS = 3;
    public static final int TYPE_ERROR = 0x10;

    public static final int ELEMENT_NONE = 0;
    public static final int ELEMENT_TLIGHT = 1;
    public static final int ELEMENT_LIGHT = 2;
    public static final int ELEMENT_SIGNAL = 3;
    public static final int ELEMENT_FAN = 4;
    public static final int ELEMENT_ACC = 5;
    public static final int ELEMENT_GIRO = 6;

    private ByteBuffer bb;

    public DataPocket(){
        bb = ByteBuffer.allocate(POCKET_LENGTH);
    }

    public DataPocket(byte[] array){
        this();

        if (array.length == POCKET_LENGTH){
            bb.put(array, 0, POCKET_LENGTH);
        }
    }

    public void init(){
        bb.put(0, (byte)MAGIC_1);
        bb.put(1, (byte)MAGIC_2);
        bb.put(9, (byte)STOP);
    }

    public byte[] getBytes(){
        return bb.array();
    }

    public static int bytes2int(byte[] bytes){
        int ret = 0;
        for(int i=0; i<4 && i<bytes.length; i++){
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }

    public boolean is_CRC_valid(){
        byte[] temp = new byte[2];
        temp[1] = bb.get(POCKET_LENGTH-3);
        temp[0] = bb.get(POCKET_LENGTH-2);

        int crc = bytes2int(temp);
        int test_crc = calculateCRC();
        if (test_crc != crc){
            return false;
        }
        else {
            return true;
        }
    }

    public int getType(){
        byte[] temp = new byte[2];
        temp[0] = bb.get(3);
        temp[1] = bb.get(2);
        return bytes2int(temp);
    }

    public int getElement(){
        byte[] temp = new byte[2];
        temp[0] = bb.get(5);
        temp[1] = bb.get(4);
        return bytes2int(temp);
    }

    public int getData(){
        byte[] temp = new byte[2];
        temp[0] = bb.get(7);
        temp[1] = bb.get(6);
        return bytes2int(temp);
    }

    public int calculateCRC(){
        int crc = 0;
        for (int i=0; i < POCKET_LENGTH - 3; i++){
            crc += bb.get(i);
        }
        return crc;
    }
}
