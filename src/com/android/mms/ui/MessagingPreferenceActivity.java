/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 * QuickMessage: Copyright (C) 2012 The CyanogenMod Project (DvTonder)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.templates.TemplatesListActivity;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.Recycler;

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MessagingPreferenceActivity extends PreferenceActivity
            implements OnPreferenceChangeListener {
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String SMS_SPLIT_MESSAGE        = "pref_key_sms_split_160";
    public static final String SMS_SPLIT_COUNTER        = "pref_key_sms_split_counter";
    public static final String PREF_SMS_MULTI_PART      = "pref_key_sms_multi_part";
    public static final int SMS_MULTI_PART_MIN          = 0;
    public static final int SMS_MULTI_PART_MAX          = 100;
    public static final String PREF_SMS_SPLIT           = "pref_key_sms_split";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String NOTIFICATION_VIBRATE     = "pref_key_vibrate";
    public static final String NOTIFICATION_VIBRATE_WHEN= "pref_key_vibrateWhen";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String GROUP_MMS_MODE           = "pref_key_mms_group_mms";
    public static final String MANAGE_TEMPLATES         = "pref_key_templates_manage";
    public static final String SHOW_GESTURE             = "pref_key_templates_show_gesture";
    public static final String GESTURE_SENSITIVITY      = "pref_key_templates_gestures_sensitivity";
    public static final String GESTURE_SENSITIVITY_VALUE= "pref_key_templates_gestures_sensitivity_value";
    public static final String STRIP_UNICODE            = "pref_key_strip_unicode";
    public static final String ENABLE_EMOJIS            = "pref_key_enable_emojis";
    public static final String ENABLE_QUICK_SMILEY_BTN  = "pref_key_quick_smiley_btn";
    public static final String FULL_TIMESTAMP           = "pref_key_mms_full_timestamp";
    public static final String SENT_TIMESTAMP           = "pref_key_mms_use_sent_timestamp";
    public static final String NOTIFICATION_VIBRATE_PATTERN = "pref_key_mms_notification_vibrate_pattern";
    public static final String NOTIFICATION_VIBRATE_PATTERN_CUSTOM = "pref_key_mms_notification_vibrate_pattern_custom";
    public static final String NOTIFICATION_VIBRATE_CALL ="pre_key_mms_notification_vibrate_call";

    // Privacy mode
    public static final String PRIVACY_MODE_ENABLED = "pref_key_enable_privacy_mode";

    // Keyboard input type
    public static final String INPUT_TYPE                = "pref_key_mms_input_type";

    // QuickMessage
    public static final String QUICKMESSAGE_ENABLED      = "pref_key_quickmessage";
    public static final String QM_LOCKSCREEN_ENABLED     = "pref_key_qm_lockscreen";
    public static final String QM_CLOSE_ALL_ENABLED      = "pref_key_close_all";
    

    private static final String DIRECT_CALL_PREF = "direct_call_pref";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

    private Preference mSmsLimitPref;
    private Preference mSmsDeliveryReportPref;
    private CheckBoxPreference mSmsSplitCounterPref;
    private CheckBoxPreference mSmsSplitPref;
    private Preference mSmsMultiPartPref;
    private Preference mMmsLimitPref;
    private Preference mMmsDeliveryReportPref;
    private Preference mMmsGroupMmsPref;
    private Preference mMmsReadReportPref;
    private Preference mManageSimPref;
    private Preference mClearHistoryPref;
    private CheckBoxPreference mEnableMultipartSMS;
    private Preference mSmsToMmsTextThreshold;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mEnableNotificationsPref;
    private CheckBoxPreference mEnablePrivacyModePref;
    private CheckBoxPreference mMmsAutoRetrievialPref;
    private RingtonePreference mRingtonePref;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    private Preference mManageTemplate;
    private ListPreference mGestureSensitivity;
    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;

    // QuickMessage
    private CheckBoxPreference mEnableQuickMessagePref;
    private CheckBoxPreference mEnableQmLockscreenPref;
    private CheckBoxPreference mEnableQmCloseAllPref;
    

    private CheckBoxPreference mDirectCall;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        loadPrefs();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Since the enabled notifications pref can be changed outside of this activity,
        // we have to reload it whenever we resume.
        setEnabledNotificationsPref();
        registerListeners();
    }

    private void loadPrefs() {
        addPreferencesFromResource(R.xml.preferences);

        mManageSimPref = findPreference("pref_key_manage_sim_messages");
        mSmsLimitPref = findPreference("pref_key_sms_delete_limit");
        mSmsDeliveryReportPref = findPreference("pref_key_sms_delivery_reports");
        mSmsSplitCounterPref = (CheckBoxPreference) findPreference("pref_key_sms_split_counter");
        mSmsSplitPref = (CheckBoxPreference) findPreference(PREF_SMS_SPLIT);
        mSmsSplitPref.setChecked(mSmsSplitPref.isChecked() || MmsConfig.getSplitSmsEnabled());
        mSmsMultiPartPref = findPreference(PREF_SMS_MULTI_PART);
        setMultiPartSmsSummary();
        mMmsDeliveryReportPref = findPreference("pref_key_mms_delivery_reports");
        mMmsGroupMmsPref = findPreference("pref_key_mms_group_mms");
        mMmsReadReportPref = findPreference("pref_key_mms_read_reports");
        mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mDirectCall = (CheckBoxPreference) findPreference("direct_call_pref");
        mEnableNotificationsPref = (CheckBoxPreference) findPreference(NOTIFICATION_ENABLED);
        mMmsAutoRetrievialPref = (CheckBoxPreference) findPreference(AUTO_RETRIEVAL);
        mEnablePrivacyModePref = (CheckBoxPreference) findPreference(PRIVACY_MODE_ENABLED);
        mVibratePref = (CheckBoxPreference) findPreference(NOTIFICATION_VIBRATE);
        mRingtonePref = (RingtonePreference) findPreference(NOTIFICATION_RINGTONE);
        mManageTemplate = findPreference(MANAGE_TEMPLATES);
        mGestureSensitivity = (ListPreference) findPreference(GESTURE_SENSITIVITY);

        mEnableMultipartSMS = (CheckBoxPreference)findPreference("pref_key_sms_EnableMultipartSMS");
        mSmsToMmsTextThreshold = findPreference("pref_key_sms_SmsToMmsTextThreshold");

       
        // QuickMessage
        mEnableQuickMessagePref = (CheckBoxPreference) findPreference(QUICKMESSAGE_ENABLED);
        mEnableQmLockscreenPref = (CheckBoxPreference) findPreference(QM_LOCKSCREEN_ENABLED);
        mEnableQmCloseAllPref = (CheckBoxPreference) findPreference(QM_CLOSE_ALL_ENABLED);
        

        setMessagePreferences();
    }

    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        setPreferenceScreen(null);
        loadPrefs();

        // NOTE: After restoring preferences, the auto delete function (i.e. message recycler)
        // will be turned off by default. However, we really want the default to be turned on.
        // Because all the prefs are cleared, that'll cause:
        // ConversationList.runOneTimeStorageLimitCheckForLegacyMessages to get executed the
        // next time the user runs the Messaging app and it will either turn on the setting
        // by default, or if the user is over the limits, encourage them to turn on the setting
        // manually.
    }

    private void setMessagePreferences() {
        if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
            // No SIM card, remove the SIM-related prefs
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mManageSimPref);
        }

        if (!MmsConfig.getSMSDeliveryReportsEnabled()) {
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsDeliveryReportPref);
            if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
                getPreferenceScreen().removePreference(smsCategory);
            }
        }

        if (!MmsConfig.getSplitSmsEnabled()) {
            // SMS Split disabled, remove SplitCounter pref
            PreferenceCategory smsCategory =
            (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsSplitCounterPref);
        }

        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

            PreferenceCategory storageOptions =
                (PreferenceCategory)findPreference("pref_key_storage_settings");
            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        } else {
            PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
            if (!MmsConfig.getMMSDeliveryReportsEnabled()) {
                mmsOptions.removePreference(mMmsDeliveryReportPref);
            }
            if (!MmsConfig.getMMSReadReportsEnabled()) {
                mmsOptions.removePreference(mMmsReadReportPref);
            }
            // If the phone's SIM doesn't know it's own number, disable group mms.
            if (!MmsConfig.getGroupMmsEnabled() ||
                    TextUtils.isEmpty(MessageUtils.getLocalNumber())) {
                mmsOptions.removePreference(mMmsGroupMmsPref);
            }
        }

        mEnableMultipartSMS.setChecked(!MmsConfig.getMultipartSmsEnabled());
        mSmsToMmsTextThreshold.setDefaultValue(MmsConfig.getSmsToMmsTextThreshold()-1);

        setEnabledNotificationsPref();

        // Privacy mode
        setEnabledPrivacyModePref();

        // QuickMessage
        setEnabledQuickMessagePref();
        setEnabledQmLockscreenPref();
        setEnabledQmCloseAllPref();
        

        // If needed, migrate vibration setting from the previous tri-state setting stored in
        // NOTIFICATION_VIBRATE_WHEN to the boolean setting stored in NOTIFICATION_VIBRATE.
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains(NOTIFICATION_VIBRATE_WHEN)) {
            String vibrateWhen = sharedPreferences.
                    getString(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN, null);
            boolean vibrate = "always".equals(vibrateWhen);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor.putBoolean(NOTIFICATION_VIBRATE, vibrate);
            prefsEditor.remove(NOTIFICATION_VIBRATE_WHEN);  // remove obsolete setting
            prefsEditor.apply();
            mVibratePref.setChecked(vibrate);
        }

        mManageTemplate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MessagingPreferenceActivity.this,
                        TemplatesListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        String gestureSensitivity = String.valueOf(sharedPreferences.getInt(GESTURE_SENSITIVITY_VALUE, 3));

        mGestureSensitivity.setSummary(gestureSensitivity);
        mGestureSensitivity.setValue(gestureSensitivity);
        mGestureSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                sharedPreferences.edit().putInt(GESTURE_SENSITIVITY_VALUE, value).commit();
                mGestureSensitivity.setSummary(String.valueOf(value));
                return true;
            }
        });

        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        setSmsDisplayLimit();
        setMmsDisplayLimit();

        // Fix up the sms to mms treshold
        setSmsToMmsTextThreshold();

        String soundValue = sharedPreferences.getString(NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(soundValue);
    }

    private void setRingtoneSummary(String soundValue) {
        Uri soundUri = TextUtils.isEmpty(soundValue) ? null : Uri.parse(soundValue);
        Ringtone tone = soundUri != null ? RingtoneManager.getRingtone(this, soundUri) : null;
        mRingtonePref.setSummary(tone != null ? tone.getTitle(this)
                : getResources().getString(R.string.silent_ringtone));
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(getNotificationEnabled(this));
    }

    private void setEnabledPrivacyModePref() {
        // The "enable privacy mode" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        boolean isPrivacyModeEnabled = getPrivacyModeEnabled(this);
        mEnablePrivacyModePref.setChecked(isPrivacyModeEnabled);

        // Enable/Disable the "enable quickmessage" setting according to
        // the "enable privacy mode" setting state
        mEnableQuickMessagePref.setEnabled(!isPrivacyModeEnabled);

        
    }

    private void setEnabledQuickMessagePref() {
        // The "enable quickmessage" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQuickMessagePref.setChecked(getQuickMessageEnabled(this));
    }

    private void setEnabledQmLockscreenPref() {
        // The "enable quickmessage on lock screen " setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQmLockscreenPref.setChecked(getQmLockscreenEnabled(this));
    }

    private void setEnabledQmCloseAllPref() {
        // The "enable close all" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQmCloseAllPref.setChecked(getQmCloseAllEnabled(this));
    }

   

    private void setSmsDisplayLimit() {
        mSmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mSmsRecycler.getMessageLimit(this)));
    }

    private void setMmsDisplayLimit() {
        mMmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mMmsRecycler.getMessageLimit(this)));
    }

    private void setSmsToMmsTextThreshold() {
        mSmsToMmsTextThreshold.setSummary(
                getString(R.string.pref_summary_sms_SmsToMmsTextThreshold,
                        MmsConfig.getSmsToMmsTextThreshold()-1));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;

            case android.R.id.home:
                // The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mSmsLimitPref) {
            new NumberPickerDialog(this,
                    mSmsLimitListener,
                    mSmsRecycler.getMessageLimit(this),
                    mSmsRecycler.getMessageMinLimit(),
                    mSmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_sms_delete).show();

        } else if (preference == mMmsLimitPref) {
            new NumberPickerDialog(this,
                    mMmsLimitListener,
                    mMmsRecycler.getMessageLimit(this),
                    mMmsRecycler.getMessageMinLimit(),
                    mMmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_mms_delete).show();

        } else if (preference == mSmsMultiPartPref) {
            new NumberPickerDialog(this,
                    mSmsMultiPartListener,
                    getMultiPartSmsSize(this),
                    SMS_MULTI_PART_MIN,
                    SMS_MULTI_PART_MAX,
                    R.string.pref_title_sms_multi_part).show();

        } else if (preference == mSmsToMmsTextThreshold) {
            new NumberPickerDialog(this,
                    mSmsToMmsTextThresholdListener,
                    MmsConfig.getSmsToMmsTextThreshold()-1,
                    MmsConfig.getSmsToMmsTextThresholdMin()-1,
                    MmsConfig.getSmsToMmsTextThresholdMax()-1,
                    R.string.pref_title_sms_SmsToMmsTextThreshold).show();

        } else if (preference == mManageSimPref) {
            startActivity(new Intent(this, ManageSimMessages.class));

        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;

        } else if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
            enableNotifications(mEnableNotificationsPref.isChecked(), this);
       
        } else if (preference == mMmsAutoRetrievialPref) {
            if (mMmsAutoRetrievialPref.isChecked()) {
                startMmsDownload();
            }

        } else if (preference == mEnableMultipartSMS) {
            //should be false when the checkbox is checked
            MmsConfig.setEnableMultipartSMS(!mEnableMultipartSMS.isChecked());

        } else if (preference == mEnablePrivacyModePref) {
            // Update the actual "enable private mode" value that is stored in secure settings.
            enablePrivacyMode(mEnablePrivacyModePref.isChecked(), this);

            // Update "enable quickmessage" checkbox state
            mEnableQuickMessagePref.setEnabled(!mEnablePrivacyModePref.isChecked());

          
        } else if (preference == mEnableQuickMessagePref) {
            // Update the actual "enable quickmessage" value that is stored in secure settings.
            enableQuickMessage(mEnableQuickMessagePref.isChecked(), this);

        } else if (preference == mEnableQmLockscreenPref) {
            // Update the actual "enable quickmessage on lockscreen" value that is stored in secure settings.
            enableQmLockscreen(mEnableQmLockscreenPref.isChecked(), this);

        } else if (preference == mEnableQmCloseAllPref) {
            // Update the actual "enable close all" value that is stored in secure settings.
            enableQmCloseAll(mEnableQmCloseAllPref.isChecked(), this);

        
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * Trigger the TransactionService to download any outstanding messages.
     */
    private void startMmsDownload() {
        startService(new Intent(TransactionService.ACTION_ENABLE_AUTO_RETRIEVE, null, this,
                TransactionService.class));
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setSmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setMmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mSmsToMmsTextThresholdListener =
            new NumberPickerDialog.OnNumberSetListener() {
                public void onNumberSet(int limit) {
                    SharedPreferences.Editor editPrefs =
                            PreferenceManager.getDefaultSharedPreferences(MessagingPreferenceActivity.this).edit();
                    editPrefs.putInt("pref_key_sms_SmsToMmsTextThreshold", limit);
                    editPrefs.apply();
                    MmsConfig.setSmsToMmsTextThreshold(limit+1);
                    setSmsToMmsTextThreshold();
                }
        };

    NumberPickerDialog.OnNumberSetListener mSmsMultiPartListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int value) {
                setMultiPartSmsSize(MessagingPreferenceActivity.this, value);
                setMultiPartSmsSummary();
            }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((MmsApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .create();
        }
        return super.onCreateDialog(id);
    }

    public static boolean getNotificationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        return notificationsEnabled;
    }

    public static void enableNotifications(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, enabled);

        editor.apply();
    }

    public static boolean getPrivacyModeEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean privacyModeEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.PRIVACY_MODE_ENABLED, false);
        return privacyModeEnabled;
    }

    public static void enablePrivacyMode(boolean enabled, Context context) {
        // Store the value of private mode in SharedPreferences
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(MessagingPreferenceActivity.PRIVACY_MODE_ENABLED, enabled);
        editor.apply();
    }

    public static boolean getQuickMessageEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean quickMessageEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.QUICKMESSAGE_ENABLED, false);
        return quickMessageEnabled;
    }

    public static void enableQuickMessage(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(MessagingPreferenceActivity.QUICKMESSAGE_ENABLED, enabled);
        editor.apply();
    }

    public static boolean getQmLockscreenEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean qmLockscreenEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.QM_LOCKSCREEN_ENABLED, false);
        return qmLockscreenEnabled;
    }

    public static void enableQmLockscreen(boolean enabled, Context context) {
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(MessagingPreferenceActivity.QM_LOCKSCREEN_ENABLED, enabled);
        editor.apply();
    }

    public static boolean getQmCloseAllEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean qmCloseAllEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.QM_CLOSE_ALL_ENABLED, false);
        return qmCloseAllEnabled;
    }

    public static void enableQmCloseAll(boolean enabled, Context context) {
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(MessagingPreferenceActivity.QM_CLOSE_ALL_ENABLED, enabled);
        editor.apply();
    }

   

   
    public static boolean getGroupMMSEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean groupMMSEnabled = prefs.getBoolean(MessagingPreferenceActivity.GROUP_MMS_MODE, false);
        return groupMMSEnabled;
    }

    public static void enableGroupMMS(boolean enabled, Context context) {
        // Store the value of GroupMMS in SharedPreferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(MessagingPreferenceActivity.GROUP_MMS_MODE, enabled);

        editor.apply();
    }

    public static boolean getDirectCallEnabled(Context context) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(DIRECT_CALL_PREF, false);
    }

    public static boolean getSplitSmsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_SMS_SPLIT, MmsConfig.getSplitSmsEnabled());
    }

    private void setMultiPartSmsSize(Context context, int value) {
        SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editPrefs.putInt(PREF_SMS_MULTI_PART, value);
        editPrefs.apply();
    }

    public static int getMultiPartSmsSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_SMS_MULTI_PART, MmsConfig.getSmsToMmsTextThreshold());
    }

    public static boolean getMultiPartSmsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return MmsConfig.getMultipartSmsEnabled() && (getMultiPartSmsSize(context) > 0);
    }

    private void setMultiPartSmsSummary() {
        if (getMultiPartSmsEnabled(this)) {
            mSmsMultiPartPref.setSummary(
                    getString(R.string.pref_summary_sms_multi_part_mms,
                    getMultiPartSmsSize(this)));
        } else {
            mSmsMultiPartPref.setSummary(
                    getString(R.string.pref_summary_sms_multi_part));
        }
    }

    private void registerListeners() {
        mRingtonePref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mRingtonePref) {
            setRingtoneSummary((String)newValue);
            result = true;
        }
        return result;
    }

    // For the group mms feature to be enabled, the following must be true:
    //  1. the feature is enabled in mms_config.xml (currently on by default)
    //  2. the feature is enabled in the mms settings page
    //  3. the SIM knows its own phone number
    public static boolean getIsGroupMmsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean groupMmsPrefOn = prefs.getBoolean(
                MessagingPreferenceActivity.GROUP_MMS_MODE, true);
        return MmsConfig.getGroupMmsEnabled() &&
                groupMmsPrefOn &&
                !TextUtils.isEmpty(MessageUtils.getLocalNumber());
    }
}
