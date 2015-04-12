package com.labs.okey.commonride.utils;

import android.content.Context;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.net.MalformedURLException;

/**
 * Created by Oleg Kleiman on 06-Feb-15.
 */
public class Globals {

    private static class DManClassFactory {

        static DrawMan drawMan;

        static DrawMan getDrawMan(){
            if( drawMan == null )
                return drawMan = new DrawMan();
            else
                return drawMan;
        }
    }
    public static final DrawMan drawMan = DManClassFactory.getDrawMan();

    public static final String FIRST_NAME_PREF = "firstname";
    public static final String LAST_NAME_PREF = "lastname";
    public static final String FB_USERNAME_PREF = "username";
    public static final String FB_LASTNAME__PREF = "lastUsername";
    public static final String REG_PROVIDER_PREF = "registrationProvider";
    public static final String REG_ID_PREF = "regid";
    public static final String PICTURE_URL_PREF = "pictureurl";
    public static final String EMAIL_PREF = "email";
    public static final String PHONE_PREF = "phone";
    public static final String USE_PHONE_PFER = "usephone";
    public static final String REG_CODE_PREF = "regcode";

    public static final String FB_PROVIDER_FOR_STORE = "Facebook:";
    public static final String GOOGLE_PROVIDER_FOR_STORE = "Google:";
    public static final String MS_PROVIDER_FOR_STORE = "MS:";
    public static final String TWITTER_PROVIDER_FOR_STORE = "Twitter:";

    public static final String JOIN_STATUS_INIT = "init";
    public static final String JOIN_STATUS_ACCEPTED = "accepted";
    public static final String JOIN_STATUS_DECLINED = "declined";

    public static float PICTURE_CORNER_RADIUS = 20;
    public static float PICTURE_BORDER_WIDTH = 4;
    public static String MY_PICTURE_FILE_NAME = "me.png";

    public static final String USERIDPREF = "userid";
    public static final String TOKENPREF = "accessToken";
    public static final String WAMSTOKENPREF = "wamsToken";

    // 'Project number' of project 'FastRide"
    // See Google Developer Console -> Billing & settings
    // https://console.developers.google.com/project/micro-shoreline-836/settings
    public static final String SENDER_ID = "574878603809";

    public static final String WAMS_URL = "https://commonride.azure-mobile.net/";
    public static final String WAMS_API_KEY = "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70";
}
