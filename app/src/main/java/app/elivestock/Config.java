package app.elivestock;

import android.app.Activity;
import android.content.Context;

import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.dns.DohResolver;

import java.io.IOException;

public class Config {
    static String webLink = "https://f5ad-54-169-147-222.ap.ngrok.io";

    //Admin panel url
    public static final String ADMIN_PANEL_URL = webLink + "/eLivestock";
    //Mobile panel url
    public static final String MOBILE_PANEL_URL = ADMIN_PANEL_URL;
    public static String WEBAPP_HOST = MOBILE_PANEL_URL; // used for checking Intent-URLs
    //set false if you want price to be displayed in decimal
    public static final boolean ENABLE_DECIMAL_ROUNDING = true;
    //set true if you want to enable RTL (Right To Left) mode, e.g : Arabic Language
    public static final boolean ENABLE_RTL_MODE = false;
    //splash screen duration in milliseconds
    public static final int SPLASH_TIME = 2000;
    // User Agent tweaks
    public static boolean POSTFIX_USER_AGENT = true; // set to true to append USER_AGENT_POSTFIX to user agent
    public static boolean OVERRIDE_USER_AGENT = false; // set to true to use USER_AGENT instead of default one
    public static String USER_AGENT_POSTFIX = "AndroidApp"; // useful for identifying traffic, e.g. in Google Analytics
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36";
    // Constants
    // window transition duration in ms
    public static int SLIDE_EFFECT = 2200;
    // show your app when the page is loaded XX %.
    // lower it, if you've got server-side rendering (e.g. to 35),
    // bump it up to ~98 if you don't have SSR or a loading screen in your web app
    public static int PROGRESS_THRESHOLD = 65;
    // turn on/off mixed content (both https+http within one page) for API >= 21
    public static boolean ENABLE_MIXED_CONTENT = true;

    public Config() throws IOException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = new DnsUdpResolver("1.1.1.1");
        resolvers[1] = new DohResolver("https://cloudflare-dns.com/dns-query");
        DnsManager dns = new DnsManager(NetworkInfo.normal, resolvers);
        Record[] records = dns.queryRecords(ADMIN_PANEL_URL);
    }
}