package org.namelessrom.perception.theme;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.settings.SettingsProvider;

import org.namelessrom.perception.LauncherConfiguration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IconPackHelper {
    private static final String TAG = "IconPackHelper";
    private static final boolean DEBUG = true;

    public static final String KEY_ICON_PACK = "key_icon_pack";

    public final static String[] sSupportedActions = new String[]{
            "org.adw.launcher.THEMES", "com.gau.go.launcherex.theme", "com.novalauncher.THEME"
            //, "com.tsf.shell.themes"
    };

    public static final String[] sSupportedCategories = new String[]{
            "com.fede.launcher.THEME_ICONPACK", "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
    };

    private static IconPackHelper sInstance;

    private final Context mContext;
    private Map<ComponentName, String> mIconPackResources;
    private String mLoadedIconPackName;
    private Resources mLoadedIconPackResource;

    private Toast mToast;

    private IconPackHelper(Context context) {
        mContext = context;
    }

    public static IconPackHelper get(Context context) {
        if (sInstance == null) {
            sInstance = new IconPackHelper(context);
        }
        return sInstance;
    }

    public String getIconPack() {
        return SettingsProvider.getStringCustomDefault(mContext, KEY_ICON_PACK, "");
    }

    public void setIconPack(String value) {
        SettingsProvider.putString(mContext, KEY_ICON_PACK, value);
    }

    public HashMap<String, String> getSupportedPackages() {
        Intent i = new Intent();
        HashMap<String, String> packages = new HashMap<>();
        PackageManager pkgManager = mContext.getPackageManager();

        // check supported actions
        for (String action : sSupportedActions) {
            i.setAction(action);
            for (ResolveInfo r : pkgManager.queryIntentActivities(i, 0)) {
                packages.put(String.valueOf(r.loadLabel(pkgManager)), r.activityInfo.packageName);
            }
        }

        // check supported categories
        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sSupportedCategories) {
            i.addCategory(category);
            for (ResolveInfo r : pkgManager.queryIntentActivities(i, 0)) {
                packages.put(String.valueOf(r.loadLabel(pkgManager)), r.activityInfo.packageName);
            }
            i.removeCategory(category);
        }

        return packages;
    }

    public void loadIconPack(String packageName) {
        if (DEBUG) {
            Log.d(TAG, "loading icon pack -> " + packageName);
        }

        mIconPackResources = getIconPackResources(mContext, packageName);
        Resources res = null;
        try {
            res = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "could not load resources");
            }
        }
        mLoadedIconPackResource = res;
        mLoadedIconPackName = packageName;
    }

    public void flushIconPack() {
        if (mIconPackResources != null) {
            mIconPackResources.clear();
            mIconPackResources = null;
        }

        mLoadedIconPackResource = null;
        mLoadedIconPackName = null;
    }

    public boolean isIconPackLoaded() {
        final boolean isIconPackLoaded = (mIconPackResources != null
                && mLoadedIconPackResource != null
                && mLoadedIconPackName != null);
        if (DEBUG) {
            Log.d(TAG, "isIconPackLoaded -> " + String.valueOf(isIconPackLoaded));
        }
        return isIconPackLoaded;
    }

    public static Map<ComponentName, String> getIconPackResources(Context context,
            String packageName) {
        Resources res;
        try {
            res = context.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "could not load icon pack resources", e);
            }
            return null;
        }

        XmlPullParser parser = null;
        InputStream inputStream = null;
        Map<ComponentName, String> iconPackResources = new HashMap<>();

        try {
            inputStream = res.getAssets().open("appfilter.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");
        } catch (Exception e) {
            // Catch any exception since we want to fall back to parsing the xml/
            // resource in all cases
            int resId = res.getIdentifier("appfilter", "xml", packageName);
            if (resId != 0) {
                parser = res.getXml(resId);
            }
        }

        if (parser != null) {
            try {
                loadResourcesFromXmlParser(parser, iconPackResources);
                return iconPackResources;
            } catch (XmlPullParserException ignored) {
            } catch (IOException ignored) {
            } finally {
                // Cleanup resources
                if (parser instanceof XmlResourceParser) {
                    ((XmlResourceParser) parser).close();
                } else {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) { }
                }
            }
        }

        // Application uses a different theme format (most likely launcher pro)
        int arrayId = res.getIdentifier("theme_iconpack", "array", packageName);
        if (arrayId == 0) {
            arrayId = res.getIdentifier("icon_pack", "array", packageName);
        }

        if (arrayId != 0) {
            String[] iconPack = res.getStringArray(arrayId);
            ComponentName compName;
            for (String entry : iconPack) {
                if (TextUtils.isEmpty(entry)) {
                    continue;
                }

                String icon = entry;
                entry = entry.replaceAll("_", ".");

                compName = new ComponentName(entry.toLowerCase(), "");
                iconPackResources.put(compName, icon);

                int activityIndex = entry.lastIndexOf(".");
                if (activityIndex <= 0 || activityIndex == entry.length() - 1) {
                    continue;
                }

                String iconPackage = entry.substring(0, activityIndex);
                if (TextUtils.isEmpty(iconPackage)) {
                    continue;
                }

                String iconActivity = entry.substring(activityIndex + 1);
                if (TextUtils.isEmpty(iconActivity)) {
                    continue;
                }

                // Store entries as lower case to ensure match
                iconPackage = iconPackage.toLowerCase();
                iconActivity = iconActivity.toLowerCase();

                iconActivity = iconPackage + "." + iconActivity;
                compName = new ComponentName(iconPackage, iconActivity);
                iconPackResources.put(compName, icon);
            }
        } else {
            loadApplicationResources(context, iconPackResources, packageName);
        }
        return iconPackResources;
    }

    private static void loadResourcesFromXmlParser(XmlPullParser parser,
            Map<ComponentName, String> iconPackResources) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            if (!parser.getName().equalsIgnoreCase("item")) {
                continue;
            }

            String component = parser.getAttributeValue(null, "component");
            String drawable = parser.getAttributeValue(null, "drawable");

            // Validate component/drawable exist
            if (TextUtils.isEmpty(component) || TextUtils.isEmpty(drawable)) {
                continue;
            }

            // Validate format/length of component
            if (!component.startsWith("ComponentInfo{") || !component.endsWith("}")
                    || component.length() < 16 || drawable.length() == 0) {
                continue;
            }

            // Sanitize stored value
            component = component.substring(14, component.length() - 1).toLowerCase();

            ComponentName name;
            if (!component.contains("/")) {
                // Package icon reference
                name = new ComponentName(component.toLowerCase(), "");
            } else {
                name = ComponentName.unflattenFromString(component);
            }

            if (name != null) {
                iconPackResources.put(name, drawable);
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
    }

    private static void loadApplicationResources(Context context,
            Map<ComponentName, String> iconPackResources, String packageName) {
        Field[] drawableItems;
        try {
            Context appContext = context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            drawableItems = Class.forName(packageName + ".R$drawable",
                    true, appContext.getClassLoader()).getFields();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "could not load application resources", e);
            }
            return;
        }

        for (Field f : drawableItems) {
            String name = f.getName();

            String icon = name;
            name = name.replaceAll("_", ".");

            ComponentName compName = new ComponentName(name.toLowerCase(), "");
            iconPackResources.put(compName, icon);

            int activityIndex = name.lastIndexOf(".");
            if (activityIndex <= 0 || activityIndex == name.length() - 1) {
                continue;
            }

            String iconPackage = name.substring(0, activityIndex);
            if (TextUtils.isEmpty(iconPackage)) {
                continue;
            }

            String iconActivity = name.substring(activityIndex + 1);
            if (TextUtils.isEmpty(iconActivity)) {
                continue;
            }

            // Store entries as lower case to ensure match
            iconPackage = iconPackage.toLowerCase();
            iconActivity = iconActivity.toLowerCase();

            iconActivity = iconPackage + "." + iconActivity;
            compName = new ComponentName(iconPackage, iconActivity);
            iconPackResources.put(compName, icon);
        }
    }

    private int getResourceIdForDrawable(String resource) {
        return mLoadedIconPackResource.getIdentifier(resource, "drawable", mLoadedIconPackName);
    }

    public Resources getIconPackResources() {
        return mLoadedIconPackResource;
    }

    public int getResourceIdForActivityIcon(ActivityInfo info) {
        ComponentName compName = new ComponentName(info.packageName.toLowerCase(),
                info.name.toLowerCase());
        String drawable = mIconPackResources.get(compName);
        if (DEBUG) {
            Log.d(TAG, "getResourceIdForActivityIcon -> " + drawable);
        }
        if (drawable == null) {
            // Icon pack doesn't have an icon for the activity, fallback to package icon
            compName = new ComponentName(info.packageName.toLowerCase(), "");
            drawable = mIconPackResources.get(compName);
            if (drawable == null) {
                return 0;
            }
        }
        return getResourceIdForDrawable(drawable);
    }

    public void pickIconPack(Context context) {
        final HashMap<String, String> supportedPackages = getSupportedPackages();

        if (supportedPackages.isEmpty()) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, R.string.no_iconpacks_summary, Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }

        final List<String> entries = new ArrayList<>(supportedPackages.size() + 1);
        entries.addAll(supportedPackages.keySet());

        final String defaultIcons =
                context.getResources().getString(R.string.default_icon_pack_title);
        Collections.sort(entries);
        entries.add(0, defaultIcons);

        final String iconPack = getIconPack();

        int index = -1;
        int defaultIndex = 0;
        final int length = entries.size();
        for (int i = 0; i < length; i++) {
            String appLabel = entries.get(i);
            if (defaultIcons.equals(appLabel)) {
                defaultIndex = i;
            } else if (supportedPackages.get(appLabel).equals(iconPack)) {
                index = i;
                break;
            }
        }

        // Icon pack either uninstalled or
        // user had selected default icons
        if (index == -1) {
            index = defaultIndex;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_pick_icon_pack_title);
        builder.setSingleChoiceItems(entries.toArray(new String[entries.size()]),
                index, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedPackage = entries.get(which);
                        if (selectedPackage.equals(defaultIcons)) {
                            setIconPack("");
                        } else {
                            setIconPack(supportedPackages.get(selectedPackage));
                        }

                        if (!iconPack.equals(selectedPackage)) {
                            if (DEBUG) {
                                Log.d(TAG, "setting flag to flush icon cache");
                            }
                            LauncherConfiguration.flushIconCache = true;
                        }

                        dialog.dismiss();
                    }
                });
        builder.show();
    }

}
