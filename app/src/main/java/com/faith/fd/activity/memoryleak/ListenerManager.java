package com.faith.fd.activity.memoryleak;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor hongbing
 * @date 2017/7/22
 */

public class ListenerManager {
    private static ListenerManager sInstance;
    private ListenerManager(){}

    private List<SampleListener> mListeners = new ArrayList<>();

    public static ListenerManager getInstance(){
        if(sInstance == null){
            sInstance = new ListenerManager();
        }
        return sInstance;
    }

    public void addListener(SampleListener listener){
        mListeners.add(listener);
    }

    public void removeListener(SampleListener listener) {
        mListeners.remove(listener);
    }

    public void removeListener(){
        if(mListeners != null){
            mListeners.clear();
        }
    }

    public int getListenerSize() {
        if (mListeners != null) {
            return mListeners.size();
        }
        return -1;
    }

}
