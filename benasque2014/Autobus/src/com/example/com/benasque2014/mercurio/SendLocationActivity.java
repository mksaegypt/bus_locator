package com.example.com.benasque2014.mercurio;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.com.benasque2014.mercurio.connections.CCPClient;
import com.example.com.benasque2014.mercurio.model.Recorrido;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

public class SendLocationActivity extends Activity {
	private static final int REQUEST_CODE = 1234;
	
	private Recorrido recorrido;
	private String codigo;

	ImageButton speakButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_location);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		try {
			recorrido = (Recorrido) getIntent().getExtras().getSerializable(
					Recorrido.KEY);
		} catch (Exception e) {
			recorrido = null;
		}
		if (recorrido == null) {
			codigo = KeyStoreController.getKeyStore().getString(
					"transmitiendo", "");
		} else {
			codigo = recorrido.getCodigo();
			KeyStoreController.getKeyStore().setPreference("transmitiendo",
					codigo);
		}

		LocationLibrary.forceLocationUpdate(getApplicationContext());

		findViewById(R.id.stop).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LocationLibrary.stopAlarmAndListener(getApplicationContext());
				KeyStoreController.getKeyStore().setPreference("transmitiendo",
						"");
				KeyStoreController.getKeyStore().setPreference("message", "");
				CCPClient.dejarRutaBuses(Secure.getString(
						getApplicationContext().getContentResolver(),
						Secure.ANDROID_ID), null);
				finish();
			}
		});

		findViewById(R.id.message).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				enviaMensajeDeMierda();
			}

		});
		
		speakButton = (ImageButton) findViewById(R.id.buttonMic);
		// Disable button if no recognition service is present
		  PackageManager pm = getPackageManager();
		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
		    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		  if (activities.size() == 0) {
		   speakButton.setEnabled(false);
		   Toast.makeText(getApplicationContext(), "Recognizer Not Found",
		     Toast.LENGTH_SHORT).show();
		  }
		findViewById(R.id.buttonMic).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}

		});
		LocationBroadcastReceiver.lastPosition=null;
		LocationBroadcastReceiver.msg="";
	}

	private void sendMessage(String text){
		KeyStoreController.getKeyStore().setPreference("message", text);
		LocationLibrary.forceLocationUpdate(SendLocationActivity.this);
	}
	
	private void enviaMensajeDeMierda() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Enviar mensaje");
		alert.setMessage("Este mensaje aparecerá a todos los usuarios que estén monitorizando tu recorrido.");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				sendMessage(value);
			}
		});

		alert.setNegativeButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		AnimatorSet set;
		ImageView imgView;
		int imgResources[] = { R.drawable.circle0, R.drawable.circle1 };
		int index = 0;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_send_location,
					container, false);

			imgView = (ImageView) rootView.findViewById(R.id.image);
			setAnimation();

			return rootView;
		}

		private void setAnimation() {
			try {
				set = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity()
						.getApplicationContext(), R.animator.wave);
				set.setTarget(imgView);

				set.addListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						setAnimation();
					}

				});
				set.start();
			} catch (Exception e) {
			}
		}
	}

	 private void startVoiceRecognitionActivity() {
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		  intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
		    "Enviar mensaje");
		  startActivityForResult(intent, REQUEST_CODE);
		 }

		 @Override
		 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			  ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			  if (matches!=null && matches.size()>0){
				  sendMessage(matches.get(0));
			  } else {
				  Toast.makeText(getApplicationContext(), "No se han encontrado coincidencias", Toast.LENGTH_SHORT).show();  
			  }
		  }
		  super.onActivityResult(requestCode, resultCode, data);
		 }
}
