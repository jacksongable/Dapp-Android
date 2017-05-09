package com.thedappapp.dapp.app.camera;

import com.thedappapp.dapp.activities.DappActivity;

/**
 * Created by jackson on 5/7/17.
 */

public abstract class SupportCamera {

    public static SupportCamera getSupportCamera (DappActivity context) {
        //Check Android version and return the old or new camera as needed. For now just return the old one.

        return new CameraOld(context);
    }

    public abstract void onActivityPaused();

    public abstract void onActivityResumed();

}
