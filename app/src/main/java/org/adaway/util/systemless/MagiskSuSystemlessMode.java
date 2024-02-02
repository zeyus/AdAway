package org.adaway.util.systemless;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.adaway.R;
import org.adaway.helper.PreferenceHelper;
import org.adaway.util.Constants;
import org.adaway.util.Log;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

/**
 * This class provides methods to systemless mode Magisk module.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class MagiskSuSystemlessMode extends AbstractSystemlessMode {
    /**
     * The Magisk systemless hosts module location
     */
    private static final String HOSTS_MODULE_PATH = "/data/adb/modules/hosts";
    private static final String HOSTS_FILE_PATH = "/data/adb/modules/hosts/system/etc/hosts";

    /**
     * Check if preferences are applied for systemless mode.
     *
     * @param context The application context.
     * @return <code>true</code> if preferences are set, <code>false</code> otherwise.
     */
    private static boolean isPreferencesApplied(Context context) {
        return PreferenceHelper.getApplyMethod(context).equals("customTarget") &&
                PreferenceHelper.getCustomTarget(context).equals(HOSTS_FILE_PATH);
    }

    /**
     * Reset preferences for systemless mode.
     *
     * @param context The application context.
     */
    private static void resetPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(context.getString(R.string.pref_apply_method_key), context.getString(R.string.pref_apply_method_def));
        preferencesEditor.putString(context.getString(R.string.pref_custom_target_key), context.getString(R.string.pref_custom_target_def));
        preferencesEditor.apply();
    }

    /**
     * Apply preferences for systemless mode.
     *
     * @param context The application context.
     */
    private static void applyPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(context.getString(R.string.pref_apply_method_key), "customTarget");
        preferencesEditor.putString(context.getString(R.string.pref_custom_target_key), HOSTS_FILE_PATH);
        preferencesEditor.apply();
    }

    @Override
    boolean isEnabled(Context context, Shell shell) throws Exception {
        // Look for systemless module
        // host file is now located in:
        // /data/adb/modules/hosts/system/etc/hosts
        SimpleCommand command = new SimpleCommand(
                "su -c test -d " + HOSTS_MODULE_PATH,
                "su -c test -f " + HOSTS_FILE_PATH
        );
        shell.add(command).waitForFinish();
        boolean enabled = command.getExitCode() == 0;
        if (enabled) {
            // Check if the preferences are already applied
            if (!MagiskSuSystemlessMode.isPreferencesApplied(context)) {
                // Apply preferences
                MagiskSuSystemlessMode.applyPreferences(context);
                if (!MagiskSuSystemlessMode.isPreferencesApplied(context)) {
                    Log.w(Constants.TAG, "Could not apply preferences.");
                }
            }
        // if the preferences are applied and systemless is disabled
        } else if (MagiskSuSystemlessMode.isPreferencesApplied(context)) {
            // Reset preferences
            MagiskSuSystemlessMode.resetPreferences(context);
            if (MagiskSuSystemlessMode.isPreferencesApplied(context)) {
                Log.w(Constants.TAG, "Could not reset preferences.");
            }
        }
        return enabled;
    }

    @Override
    public boolean enable(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enable_systemless_magisk_title)
                .setMessage(R.string.enable_systemless_magisk)
                .setNeutralButton(R.string.button_close, (d, which) -> d.dismiss())
                .create()
                .show();
        return false;
    }

    @Override
    public boolean disable(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.disable_systemless_magisk_title)
                .setMessage(R.string.disable_systemless_magisk)
                .setNeutralButton(R.string.button_close, (d, which) -> d.dismiss())
                .create()
                .show();
        return false;
    }

    @Override
    public boolean isRebootNeededAfterActivation() {
        return false;
    }

    @Override
    public boolean isRebootNeededAfterDeactivation() {
        return false;
    }

}
