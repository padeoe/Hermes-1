package com.ata.provider.transfer;

import android.content.Context;

/**
 * Created by raven on 2015/5/15.
 */
public abstract class ConnectModule {
    public abstract void Init( ConnectionListener Listener, Context context);
    public abstract void Start();
    public abstract void Stop();
    public abstract void Discover();
    public abstract boolean TryConnect(Device device);
    public abstract Device getLocalDevice();
}
