package org.smssecure.smssecure.components;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.util.SMSSecurePreferences;

public class OutgoingSmsPreference extends DialogPreference {
  private CheckBox dataUsers;
  private CheckBox askForFallback;
  private CheckBox neverFallbackMms;
  private CheckBox nonDataUsers;


  public OutgoingSmsPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPersistent(false);
    setDialogLayoutResource(R.layout.outgoing_sms_preference);
  }

  @Override
  protected void onBindDialogView(final View view) {
    super.onBindDialogView(view);
    dataUsers        = (CheckBox) view.findViewById(R.id.data_users);
    askForFallback   = (CheckBox) view.findViewById(R.id.ask_before_fallback_data);
    neverFallbackMms = (CheckBox) view.findViewById(R.id.never_send_mms);
    nonDataUsers     = (CheckBox) view.findViewById(R.id.non_data_users);

    dataUsers.setChecked(SMSSecurePreferences.isFallbackSmsAllowed(getContext()));
    askForFallback.setChecked(SMSSecurePreferences.isFallbackSmsAskRequired(getContext()));
    neverFallbackMms.setChecked(!SMSSecurePreferences.isFallbackMmsEnabled(getContext()));
    nonDataUsers.setChecked(SMSSecurePreferences.isDirectSmsAllowed(getContext()));

    dataUsers.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateEnabledViews();
      }
    });

    updateEnabledViews();
  }

  private void updateEnabledViews() {
    askForFallback.setEnabled(dataUsers.isChecked());
    neverFallbackMms.setEnabled(dataUsers.isChecked());
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      SMSSecurePreferences.setFallbackSmsAllowed(getContext(), dataUsers.isChecked());
      SMSSecurePreferences.setFallbackSmsAskRequired(getContext(), askForFallback.isChecked());
      SMSSecurePreferences.setDirectSmsAllowed(getContext(), nonDataUsers.isChecked());
      SMSSecurePreferences.setFallbackMmsEnabled(getContext(), !neverFallbackMms.isChecked());
      if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
    }
  }
}
