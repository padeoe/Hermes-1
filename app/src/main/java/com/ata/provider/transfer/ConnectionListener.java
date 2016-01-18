package com.ata.provider.transfer;

import java.util.List;

/**
 * Created by raven on 2015/5/15.
 */

public interface ConnectionListener {
    void onConnectedSuccess(TransportCondition condition);
     void onConnectionLost();
     void onPeersAvailable(List<Device> peers);
}
