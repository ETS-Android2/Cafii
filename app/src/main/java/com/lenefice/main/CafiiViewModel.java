package com.lenefice.main;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CafiiViewModel extends ViewModel {

    private MutableLiveData<CafiiService.CafiiBinder> myBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> countDT = new MutableLiveData<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CafiiService.CafiiBinder binder = (CafiiService.CafiiBinder) iBinder;
            myBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myBinder.postValue(null);
        }
    };

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public LiveData<CafiiService.CafiiBinder> getBinder() {
        return myBinder;
    }

    public LiveData<Boolean> getcountDT() {
        return countDT;
    }

    public void setcountDT(Boolean value) {
        countDT.postValue(value);
    }

}
