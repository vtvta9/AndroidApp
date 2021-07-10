package dev.edmt.android_app.Common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import dev.edmt.android_app.model.User;

public class Common {
    public static User currentUser;
    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "Đang được vận chuyển!!";
        else
            return "Đã giao";

    }
    public static boolean isConnectedToInterner(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
           NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
                for(int i=0;i<info.length;i++)
                {
                    if(info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }return false;
    }
    public static String DELETE = "Delete";
}
