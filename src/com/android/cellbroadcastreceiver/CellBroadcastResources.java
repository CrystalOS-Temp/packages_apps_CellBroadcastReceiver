/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.cellbroadcastreceiver;

import android.annotation.NonNull;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import android.telephony.SmsCbMessage;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.android.cellbroadcastreceiver.CellBroadcastChannelManager.CellBroadcastChannelRange;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Returns the string resource ID's for CMAS and ETWS emergency alerts.
 */
public class CellBroadcastResources {

    private CellBroadcastResources() {
    }

    /**
     * Returns a styled CharSequence containing the message date/time and alert details.
     * @param context a Context for resource string access
     * @param showDebugInfo {@code true} if adding more information for debugging purposes.
     * @param message The cell broadcast message.
     * @param locationCheckTime The EPOCH time in milliseconds that Device-based Geo-fencing (DBGF)
     * was last performed. 0 if the message does not have DBGF information.
     * @param isDisplayed {@code true} if the message is displayed to the user.
     * @param geometry Geometry string for device-based geo-fencing message.
     *
     * @return a CharSequence for display in the broadcast alert dialog
     */
    public static CharSequence getMessageDetails(Context context, boolean showDebugInfo,
                                                 SmsCbMessage message, long locationCheckTime,
                                                 boolean isDisplayed, String geometry) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        // Alert date/time
        appendMessageDetail(context, buf, R.string.delivery_time_heading,
                DateFormat.getDateTimeInstance().format(message.getReceivedTime()));

        // Message id
        if (showDebugInfo) {
            appendMessageDetail(context, buf, R.string.message_identifier,
                    Integer.toString(message.getServiceCategory()));
            appendMessageDetail(context, buf, R.string.message_serial_number,
                    Integer.toString(message.getSerialNumber()));
        }

        if (message.isCmasMessage()) {
            // CMAS category, response type, severity, urgency, certainty
            appendCmasAlertDetails(context, buf, message.getCmasWarningInfo());
        }

        if (showDebugInfo) {
            appendMessageDetail(context, buf, R.string.data_coding_scheme,
                    Integer.toString(message.getDataCodingScheme()));

            appendMessageDetail(context, buf, R.string.message_content, message.getMessageBody());

            appendMessageDetail(context, buf, R.string.location_check_time, locationCheckTime == -1
                    ? "N/A"
                    : DateFormat.getDateTimeInstance().format(locationCheckTime));

            appendMessageDetail(context, buf, R.string.maximum_waiting_time,
                    message.getMaximumWaitingDuration() + " "
                            + context.getString(R.string.seconds));

            appendMessageDetail(context, buf, R.string.message_displayed,
                    Boolean.toString(isDisplayed));

            appendMessageDetail(context, buf, R.string.message_coordinates,
                    TextUtils.isEmpty(geometry) ? "N/A" : geometry);
        }

        return buf;
    }

    private static void appendCmasAlertDetails(Context context, SpannableStringBuilder buf,
            SmsCbCmasInfo cmasInfo) {
        // CMAS category
        int categoryId = getCmasCategoryResId(cmasInfo);
        if (categoryId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_category_heading,
                    context.getString(categoryId));
        }

        // CMAS response type
        int responseId = getCmasResponseResId(cmasInfo);
        if (responseId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_response_heading,
                    context.getString(responseId));
        }

        // CMAS severity
        int severityId = getCmasSeverityResId(cmasInfo);
        if (severityId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_severity_heading,
                    context.getString(severityId));
        }

        // CMAS urgency
        int urgencyId = getCmasUrgencyResId(cmasInfo);
        if (urgencyId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_urgency_heading,
                    context.getString(urgencyId));
        }

        // CMAS certainty
        int certaintyId = getCmasCertaintyResId(cmasInfo);
        if (certaintyId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_certainty_heading,
                    context.getString(certaintyId));
        }
    }

    private static void appendMessageDetail(Context context, SpannableStringBuilder buf,
                                           int typeId, String value) {
        if (buf.length() != 0) {
            buf.append("\n");
        }
        int start = buf.length();
        buf.append(context.getString(typeId));
        int end = buf.length();
        buf.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        buf.append(" ");
        buf.append(value);
    }

    /**
     * Returns the string resource ID for the CMAS category.
     * @return a string resource ID, or 0 if the CMAS category is unknown or not present
     */
    private static int getCmasCategoryResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getCategory()) {
            case SmsCbCmasInfo.CMAS_CATEGORY_GEO:
                return R.string.cmas_category_geo;

            case SmsCbCmasInfo.CMAS_CATEGORY_MET:
                return R.string.cmas_category_met;

            case SmsCbCmasInfo.CMAS_CATEGORY_SAFETY:
                return R.string.cmas_category_safety;

            case SmsCbCmasInfo.CMAS_CATEGORY_SECURITY:
                return R.string.cmas_category_security;

            case SmsCbCmasInfo.CMAS_CATEGORY_RESCUE:
                return R.string.cmas_category_rescue;

            case SmsCbCmasInfo.CMAS_CATEGORY_FIRE:
                return R.string.cmas_category_fire;

            case SmsCbCmasInfo.CMAS_CATEGORY_HEALTH:
                return R.string.cmas_category_health;

            case SmsCbCmasInfo.CMAS_CATEGORY_ENV:
                return R.string.cmas_category_env;

            case SmsCbCmasInfo.CMAS_CATEGORY_TRANSPORT:
                return R.string.cmas_category_transport;

            case SmsCbCmasInfo.CMAS_CATEGORY_INFRA:
                return R.string.cmas_category_infra;

            case SmsCbCmasInfo.CMAS_CATEGORY_CBRNE:
                return R.string.cmas_category_cbrne;

            case SmsCbCmasInfo.CMAS_CATEGORY_OTHER:
                return R.string.cmas_category_other;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS response type.
     * @return a string resource ID, or 0 if the CMAS response type is unknown or not present
     */
    private static int getCmasResponseResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getResponseType()) {
            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_SHELTER:
                return R.string.cmas_response_shelter;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_EVACUATE:
                return R.string.cmas_response_evacuate;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_PREPARE:
                return R.string.cmas_response_prepare;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_EXECUTE:
                return R.string.cmas_response_execute;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_MONITOR:
                return R.string.cmas_response_monitor;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_AVOID:
                return R.string.cmas_response_avoid;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_ASSESS:
                return R.string.cmas_response_assess;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_NONE:
                return R.string.cmas_response_none;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS severity.
     * @return a string resource ID, or 0 if the CMAS severity is unknown or not present
     */
    private static int getCmasSeverityResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getSeverity()) {
            case SmsCbCmasInfo.CMAS_SEVERITY_EXTREME:
                return R.string.cmas_severity_extreme;

            case SmsCbCmasInfo.CMAS_SEVERITY_SEVERE:
                return R.string.cmas_severity_severe;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS urgency.
     * @return a string resource ID, or 0 if the CMAS urgency is unknown or not present
     */
    private static int getCmasUrgencyResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getUrgency()) {
            case SmsCbCmasInfo.CMAS_URGENCY_IMMEDIATE:
                return R.string.cmas_urgency_immediate;

            case SmsCbCmasInfo.CMAS_URGENCY_EXPECTED:
                return R.string.cmas_urgency_expected;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS certainty.
     * @return a string resource ID, or 0 if the CMAS certainty is unknown or not present
     */
    private static int getCmasCertaintyResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getCertainty()) {
            case SmsCbCmasInfo.CMAS_CERTAINTY_OBSERVED:
                return R.string.cmas_certainty_observed;

            case SmsCbCmasInfo.CMAS_CERTAINTY_LIKELY:
                return R.string.cmas_certainty_likely;

            default:
                return 0;
        }
    }

    /**
     * Return the English string for the SMS sender address.
     * This exists as a temporary workaround for b/174972822
     * @param context
     * @param message
     * @return
     */
    public static String getSmsSenderAddressResourceEnglishString(@NonNull Context context,
            @NonNull SmsCbMessage message) {

        int resId = getSmsSenderAddressResource(context, message);

        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(Locale.ENGLISH);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources().getText(resId).toString();
    }

    /**
     * @return the string resource ID for the SMS sender address.
     * As a temporary workaround for b/174972822, prefer getSmsSenderAddressResourceEnglishString,
     * which ignores all translations for non-English languages for these 4 strings.
     */
    public static int getSmsSenderAddressResource(@NonNull Context context,
            @NonNull SmsCbMessage message) {
        CellBroadcastChannelManager channelManager = new CellBroadcastChannelManager(
                context, message.getSubscriptionId());
        final int serviceCategory = message.getServiceCategory();
        // store to different SMS threads based on channel mappings.
        switch (channelManager.getCellBroadcastChannelResourcesKey(serviceCategory)) {
            case R.array.cmas_presidential_alerts_channels_range_strings:
                return R.string.sms_cb_sender_name_presidential;
            case R.array.emergency_alerts_channels_range_strings:
                return R.string.sms_cb_sender_name_emergency;
            case R.array.public_safety_messages_channels_range_strings:
                return R.string.sms_cb_sender_name_public_safety;
        }

        return R.string.sms_cb_sender_name_default;
    }

    static int getDialogTitleResource(Context context, SmsCbMessage message) {
        // ETWS warning types
        SmsCbEtwsInfo etwsInfo = message.getEtwsWarningInfo();
        if (etwsInfo != null) {
            switch (etwsInfo.getWarningType()) {
                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE:
                    return R.string.etws_earthquake_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_TSUNAMI:
                    return R.string.etws_tsunami_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE_AND_TSUNAMI:
                    return R.string.etws_earthquake_and_tsunami_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_TEST_MESSAGE:
                    return R.string.etws_test_message;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_OTHER_EMERGENCY:
                default:
                    return R.string.etws_other_emergency_type;
            }
        }

        SmsCbCmasInfo cmasInfo = message.getCmasWarningInfo();
        int subId = message.getSubscriptionId();
        CellBroadcastChannelManager channelManager = new CellBroadcastChannelManager(
                context, subId);
        final int serviceCategory = message.getServiceCategory();
        int resourcesKey = channelManager.getCellBroadcastChannelResourcesKey(serviceCategory);
        CellBroadcastChannelRange range = channelManager
                .getCellBroadcastChannelRange(serviceCategory);

        switch (resourcesKey) {
            case R.array.emergency_alerts_channels_range_strings:
                return R.string.pws_other_message_identifiers;
            case R.array.cmas_presidential_alerts_channels_range_strings:
                return R.string.cmas_presidential_level_alert;
            case R.array.cmas_alert_extreme_channels_range_strings:
                return R.string.cmas_extreme_alert;
            case R.array.cmas_alerts_severe_range_strings:
                return R.string.cmas_severe_alert;
            case R.array.cmas_amber_alerts_channels_range_strings:
                return R.string.cmas_amber_alert;
            case R.array.required_monthly_test_range_strings:
                return R.string.cmas_required_monthly_test;
            case R.array.exercise_alert_range_strings:
                return R.string.cmas_exercise_alert;
            case R.array.operator_defined_alert_range_strings:
                return R.string.cmas_operator_defined_alert;
            case R.array.public_safety_messages_channels_range_strings:
                return R.string.public_safety_message;
            case R.array.state_local_test_alert_range_strings:
                return R.string.state_local_test_alert;
        }

        if (channelManager.isEmergencyMessage(message)) {
            if (resourcesKey == R.array.additional_cbs_channels_strings) {
                switch (range.mAlertType) {
                    case DEFAULT:
                        return R.string.pws_other_message_identifiers;
                    case ETWS_EARTHQUAKE:
                        return R.string.etws_earthquake_warning;
                    case ETWS_TSUNAMI:
                        return R.string.etws_tsunami_warning;
                    case TEST:
                        return R.string.etws_test_message;
                    case ETWS_DEFAULT:
                    case OTHER:
                        return R.string.etws_other_emergency_type;
                    default:
                        break;
                }
            }
            return R.string.pws_other_message_identifiers;
        } else {
            return R.string.cb_other_message_identifiers;
        }
    }

    /**
     * Choose pictogram resource according to etws type.
     *
     * @param context Application context
     * @param message Cell broadcast message
     *
     * @return The resource of the pictogram, -1 if not available.
     */
    static int getDialogPictogramResource(Context context, SmsCbMessage message) {
        SmsCbEtwsInfo etwsInfo = message.getEtwsWarningInfo();
        if (etwsInfo != null) {
            switch (etwsInfo.getWarningType()) {
                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE:
                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE_AND_TSUNAMI:
                    return R.drawable.pict_icon_earthquake;
                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_TSUNAMI:
                    return R.drawable.pict_icon_tsunami;
            }
        }

        final int serviceCategory = message.getServiceCategory();
        int subId = message.getSubscriptionId();
        CellBroadcastChannelManager channelManager = new CellBroadcastChannelManager(
                context, subId);
        if (channelManager.isEmergencyMessage(message)) {
            if (channelManager.getCellBroadcastChannelResourcesKey(serviceCategory)
                    == R.array.additional_cbs_channels_strings) {
                CellBroadcastChannelRange range = channelManager
                        .getCellBroadcastChannelRangeFromMessage(message);
                // Apply the closest title to the specified tones.
                switch (range.mAlertType) {
                    case ETWS_EARTHQUAKE:
                        return R.drawable.pict_icon_earthquake;
                    case ETWS_TSUNAMI:
                        return R.drawable.pict_icon_tsunami;
                    default:
                        break;
                }
            }
            return -1;
        }
        return -1;
    }
}
