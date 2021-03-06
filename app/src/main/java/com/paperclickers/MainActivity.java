/*
 * Paperclickers - Affordable solution for classroom response system.
 * 
 * Copyright (C) 2015 Jomara Bindá <jbinda@dca.fee.unicamp.br>
 * Copyright (C) 2015-2016 Eduardo Valle Jr <dovalle@dca.fee.unicamp.br>
 * Copyright (C) 2015-2016 Eduardo Seiti de Oliveira <eduseiti@dca.fee.unicamp.br>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *   
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package com.paperclickers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paperclickers.camera.CameraEmulator;
import com.paperclickers.camera.CameraMain;
import com.paperclickers.onboarding.OnboardingActivity;
import com.paperclickers.overlay.OverlayManager;
import com.paperclickers.result.AnswersLog;

import java.util.Timer;


public class MainActivity extends Activity {

	static String TAG = "MainActivity";

	static final int DEVELOPMENT_OPTIONS_ACTIVATION_THRESHOLD = 5;
	static final int DEVELOPMENT_OPTIONS_ACTIVATION_INTERVAL  = 300;

	int mDebugOptionsActivationTapCounter   = 0;
	long mDebugOptionsActivationLastTapTime = 0;
	
	boolean mManualQuestionsTagging = false;
	boolean mUseRegularCamera = false;

	private Analytics mAnalytics = null;

	private OverlayManager mOverlayManager = null;

	private SharedPreferences mSharedPreferences = null;



	@Override
	public void onBackPressed() {
		mOverlayManager.markOverlayAsShown();

		super.onBackPressed();
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mAnalytics = new Analytics(getApplicationContext());

		mOverlayManager = new OverlayManager(getApplicationContext(), getFragmentManager());

		setContentView(R.layout.start_activity);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Bundle intentExtras = getIntent().getExtras();

		boolean restartedInternally = false;

		if (intentExtras != null) {
			restartedInternally = intentExtras.getBoolean("restartingInternally");
		}

		log.d(TAG, "onCreate - Received restarted internally indication: " + restartedInternally);

		if (!restartedInternally) {
			AnswersLog.resetQuestionsSequenceNumber();

			log.d(TAG, ">>> New execution sequence; restart sequence number in answer log");

			mOverlayManager.checkAndTurnOnOverlayTimer(OverlayManager.INITIAL_SCREEN);
		}


		// Reset previously opened log entry, allowing new question to be registered in the answers' log.

		AnswersLog.resetOpenLogEntry(getResources().getText(R.string.answerslog_file_name).toString());


		// Adding listener for "about" button
		Button about = (Button) findViewById(R.id.appIcon);
		about.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), AboutActivity.class);

				startActivity(i);
			}
		});


		// Adding listener for "settings" button
		Button settingsButton = (Button) findViewById(R.id.settingsIcon);
		settingsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!SettingsActivity.isDevelopmentMode()) {
					// Disable help overlay, since has entered settings

					mOverlayManager.markOverlayAsShown();
				}

				Intent i = new Intent(getApplicationContext(), SettingsActivity.class);

				startActivity(i);
			}
		});


		// Adding listener for "start" button
		Button startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				mOverlayManager.markOverlayAsShown();

				Intent newActivity;

				if (mManualQuestionsTagging) {
					newActivity = new Intent(getApplicationContext(), SaveQuestionActivity.class);

					newActivity.putExtra("useRegularCamera", mUseRegularCamera);
				} else {
					if (mUseRegularCamera) {
						newActivity = new Intent(getApplicationContext(), CameraMain.class);
					} else {
						newActivity = new Intent(getApplicationContext(), CameraEmulator.class);
					}
				}

				startActivity(newActivity);
			}
		});


		if (SettingsActivity.DEVELOPMENT_OPTIONS) {

			// Adding listener for Debug mode activation

			TextView appNameText = (TextView) findViewById(R.id.appNameText);

			appNameText.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					long currentTime = System.currentTimeMillis();

					if (currentTime - mDebugOptionsActivationLastTapTime < DEVELOPMENT_OPTIONS_ACTIVATION_INTERVAL) {
						mDebugOptionsActivationTapCounter++;

						if (mDebugOptionsActivationTapCounter >= DEVELOPMENT_OPTIONS_ACTIVATION_THRESHOLD) {

							CharSequence activationText;
							boolean newDevelopmentModeStatus;

							if (SettingsActivity.isDevelopmentMode()) {
								activationText = getResources().getText(R.string.development_mode_off);

								newDevelopmentModeStatus = false;
							} else {
								activationText = getResources().getText(R.string.development_mode_on);

								newDevelopmentModeStatus = true;
							}

							Toast.makeText(getApplicationContext(), activationText, Toast.LENGTH_SHORT).show();

							SettingsActivity.setDevelopmentMode(newDevelopmentModeStatus);

							mDebugOptionsActivationTapCounter = 0;

							mAnalytics.send_debugMode(newDevelopmentModeStatus);

							updateRegularCameraUsage();
						}
					} else {
						mDebugOptionsActivationTapCounter = 1;
					}

					mDebugOptionsActivationLastTapTime = currentTime;
				}
			});
		}
	}
	


	@Override
	protected void onPause() {
		super.onPause();

		log.d(TAG, "onPause");

		mOverlayManager.removeOverlay();
	}


	
    @Override
    protected void onResume() {
		super.onResume();

		// Reset previously opened log entry: it can be returning from completed detection before this activity has died.

		AnswersLog.resetOpenLogEntry(getResources().getText(R.string.answerslog_file_name).toString());


		mDebugOptionsActivationTapCounter = 0;
		mDebugOptionsActivationLastTapTime = 0;

		String questionsTaggingStr = mSharedPreferences.getString("questions_tagging", "0");

		mManualQuestionsTagging = questionsTaggingStr.equals("1");

		updateRegularCameraUsage();

		if (mSharedPreferences.getBoolean(OnboardingActivity.ONBOARDING_ACTIVE, true)) {
			Intent i = new Intent(getApplicationContext(), OnboardingActivity.class);

			startActivity(i);
		} else {
			mOverlayManager.checkAndTurnOnOverlayTimer(OverlayManager.INITIAL_SCREEN);
		}
	}



    private void updateRegularCameraUsage() {
		mUseRegularCamera = true;

		if (SettingsActivity.isDevelopmentMode()) {
			mUseRegularCamera = !mSharedPreferences.getBoolean("development_use_camera_emulation", false);
		}
	}
}
