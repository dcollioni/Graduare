package com.dcollioni.graduare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.db4o.ObjectSet;
import com.dcollioni.graduare.data.Db4oHelper;
import com.dcollioni.graduare.entities.Student;

public class MainActivity extends ActionBarActivity {
	
	private static Db4oHelper db4o;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        String dataPath = getDir("data", 0).getPath();
        db4o = new Db4oHelper(dataPath);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	db4o.openDB();
    	
    	ObjectSet<Student> students = db4o.db().query(Student.class);
    	
    	if (students.size() > 0) {
    		openCourseActivity();
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	db4o.closeDB();
    }
    
    public void openCourseActivity() {
    	Intent intent = new Intent(MainActivity.this, CourseActivity.class);
		startActivity(intent);
		this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
        	
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	private EditText etEmail, etRegistrationCode;
    	private Button btLogin;
    	private ProgressBar pbLogin;
    	
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            
            etEmail = (EditText)rootView.findViewById(R.id.etEmail);
            etRegistrationCode = (EditText)rootView.findViewById(R.id.etRegistrationCode);
            btLogin = (Button)rootView.findViewById(R.id.btLogin);
            pbLogin = (ProgressBar)rootView.findViewById(R.id.pbLogin);
            
            btLogin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String email = etEmail.getText().toString();
					String registrationCode = etRegistrationCode.getText().toString();
					
					if (!email.isEmpty() && !registrationCode.isEmpty()) {
						new LoginAsyncTask().execute(email, registrationCode);
					}
				}
			});
            
            etRegistrationCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if (actionId == EditorInfo.IME_ACTION_DONE) {
	                    btLogin.performClick();
	                    return true;
	                }
	                return false;
				}
			});
            
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etEmail, InputMethodManager.SHOW_IMPLICIT);
            
            return rootView;
        }
        
        private void applyLoadingStyle(boolean isLoading) {
        	etEmail.setEnabled(!isLoading);
        	etRegistrationCode.setEnabled(!isLoading);
        	btLogin.setEnabled(!isLoading);
        	
        	int pbVisibility = isLoading ? View.VISIBLE : View.GONE;
			pbLogin.setVisibility(pbVisibility);
        }
        
        private class LoginAsyncTask extends AsyncTask<String, Void, Student> {

			@Override
			protected Student doInBackground(String... params) {
				
				publishProgress();
				
				Student student = null;
				
				String email = params[0];
				String registrationCode = params[1];
				
				HttpURLConnection urlConnection = null;
		        BufferedReader reader = null;

		        String studentJsonStr = null;

		        try {
		            final String GET_STUDENT_BASE_URL =
		                    "http://schoollineup.apphb.com/api/Students?";
		            
		            final String EMAIL = "email";
		            final String PASSWORD = "password";

		            Uri builtUri = Uri.parse(GET_STUDENT_BASE_URL).buildUpon()
		                    .appendQueryParameter(EMAIL, email)
		                    .appendQueryParameter(PASSWORD, registrationCode)
		                    .build();

		            URL url = new URL(builtUri.toString());
		            
		            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("brazil-proxy.geo.corp.hcl.in", 8080));
		            //proxy.
		            //HttpHost("brazil-proxy.geo.corp.hcl.in", 8080);

		            urlConnection = (HttpURLConnection) url.openConnection();
		            urlConnection.setRequestMethod("GET");
		            urlConnection.connect();

		            InputStream inputStream = urlConnection.getInputStream();
		            StringBuffer buffer = new StringBuffer();
		            
		            if (inputStream == null) {
		            	return student;
		            }
		            
		            reader = new BufferedReader(new InputStreamReader(inputStream));

		            String line;
		            
		            while ((line = reader.readLine()) != null) {
		                buffer.append(line + "\n");
		            }

		            if (buffer.length() == 0) {
		            	return student;
		            }
		            
		            studentJsonStr = buffer.toString();
		        } catch (IOException e) {
		        	e.printStackTrace();
		        } finally {
		            if (urlConnection != null) {
		                urlConnection.disconnect();
		            }
		            if (reader != null) {
		                try {
		                    reader.close();
		                } catch (final IOException e) {
		                	e.printStackTrace();
		                }
		            }
		        }

		        try {
		            if (studentJsonStr != null) {
		            	
		            	JSONObject json = new JSONObject(studentJsonStr);
		            	
		            	if (json.has("Id")) {
		            		
		            		student = new Student();
		            		student.setId(json.getInt("Id"));
		            		student.setEmail(json.getString("Email"));
		            		student.setName(json.getString("Name"));
		            	}
		            }
		        } catch (JSONException e) {
		            e.printStackTrace();
		        }
				
				return student;
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
				applyLoadingStyle(true);
			}
			
			@Override
			protected void onPostExecute(Student result) {
				super.onPostExecute(result);
				applyLoadingStyle(false);
				
				if (result != null) {
					db4o.db().store(result);
				    ((MainActivity)getActivity()).openCourseActivity();
				} else {
					Toast.makeText(getActivity(), getString(R.string.invalid_login), Toast.LENGTH_LONG).show();
				}
			}
        }
    }
}
