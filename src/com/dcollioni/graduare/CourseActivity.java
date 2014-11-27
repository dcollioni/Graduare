package com.dcollioni.graduare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.db4o.ObjectSet;
import com.dcollioni.graduare.data.Db4oHelper;
import com.dcollioni.graduare.entities.Course;
import com.dcollioni.graduare.entities.Student;

public class CourseActivity extends ActionBarActivity {

	private static Db4oHelper db4o;
	private static Student loggedStudent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		loggedStudent = null;
		
		String dataPath = getDir("data", 0).getPath();
        db4o = new Db4oHelper(dataPath);
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	db4o.openDB();
    	
    	if (loggedStudent == null) {
    		
	    	ObjectSet<Student> students = db4o.db().query(Student.class);
	    	
	    	if (students.size() <= 0) {
	    		Log.i("logged_student", "not found");
	    		openMainActivity();
	    	} else {
	    		
	    		loggedStudent = students.get(0);
	    		
	    		for (Student student : students) {
	    			Log.i("logged_student", student.getId() + " " + student.getName() + " " + student.getEmail());
				}
	    	}
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	db4o.closeDB();
    }
    
    private void openMainActivity() {
    	Intent intent = new Intent(CourseActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			ObjectSet<Student> students = db4o.db().query(Student.class);
			
			for (Student student : students) {
				db4o.db().delete(student);
			}
			
			db4o.db().commit();
			
			openMainActivity();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private ProgressBar pbLoading;
		
		private CourseAdapter courseAdapter;
		private ListView courseListView;
		private ArrayList<Course> courses;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_course,
					container, false);
			
			courses = new ArrayList<Course>();
			
			courseAdapter = new CourseAdapter(getActivity(), courses);
	        
			courseListView = (ListView)rootView.findViewById(R.id.listview_courses);
			courseListView.setAdapter(courseAdapter);
			
			courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
					
					Course course = (Course)adapter.getItemAtPosition(position);
					
					Intent intent = new Intent(getActivity(), ExamActivity.class);
					intent.putExtra(ExamActivity.COURSE_KEY, course);
					intent.putExtra(ExamActivity.STUDENT_KEY, loggedStudent.getId());
					startActivity(intent);
				}
			});
			
			pbLoading = (ProgressBar)rootView.findViewById(R.id.progressbar_loading);
			
			return rootView;
		}
		
		@Override
		public void onResume() {
			super.onResume();
			Log.i("fragment", "onResume...");
			
			if (loggedStudent != null) {
				
				getActivity().setTitle(loggedStudent.getName());
				new LoadAsyncTask().execute(loggedStudent.getId());
			}
		}
		
		private class LoadAsyncTask extends AsyncTask<Integer, Void, ArrayList<Course>> {

			@Override
			protected ArrayList<Course> doInBackground(Integer... params) {
				
				publishProgress();
				
				int studentId = params[0];
				
				HttpURLConnection urlConnection = null;
		        BufferedReader reader = null;

		        String dataJsonStr = null;

		        try {
		            final String GET_COURSES_BASE_URL =
		                    "http://schoollineup.apphb.com/api/Courses?";
		            
		            final String STUDENT_ID = "studentId";

		            Uri builtUri = Uri.parse(GET_COURSES_BASE_URL).buildUpon()
		                    .appendQueryParameter(STUDENT_ID, Integer.toString(studentId))
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
		            	return null;
		            }
		            
		            reader = new BufferedReader(new InputStreamReader(inputStream));

		            String line;
		            
		            while ((line = reader.readLine()) != null) {
		                buffer.append(line + "\n");
		            }

		            if (buffer.length() == 0) {
		            	return null;
		            }
		            
		            dataJsonStr = buffer.toString();
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

		        ArrayList<Course> courses = new ArrayList<Course>();
		        
		        try {
		            if (dataJsonStr != null) {
		            	
		            	JSONArray jsonArray = new JSONArray(dataJsonStr);
		            	
		            	if (jsonArray.length() > 0) {
		            		
		            		for (int i = 0; i < jsonArray.length(); i++) {
	            				
		            			JSONObject jsonCourse = jsonArray.getJSONObject(i);
		            			
		            			Course course = new Course();
		            			course.setClosed(jsonCourse.getBoolean("IsClosed"));
		            			course.setCollegeId(jsonCourse.getInt("CollegeId"));
		            			course.setCollegeName(jsonCourse.getString("CollegeName"));
		            			course.setFinishDate(jsonCourse.getString("FinishDateStr"));
		            			course.setId(jsonCourse.getInt("Id"));
		            			course.setName(jsonCourse.getString("Name"));
		            			course.setStartDate(jsonCourse.getString("StartDateStr"));
		            			course.setTeacherId(jsonCourse.getInt("TeacherId"));
		            			course.setTeacherName(jsonCourse.getString("TeacherName"));
		            			
		            			courses.add(course);
							}
		            	}
		            }
		        } catch (JSONException e) {
		            e.printStackTrace();
		        }
				
				return courses;
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
				pbLoading.setVisibility(View.VISIBLE);
			}
			
			@Override
			protected void onPostExecute(ArrayList<Course> result) {
				super.onPostExecute(result);
				pbLoading.setVisibility(View.GONE);
				
				if (result != null) {
					
					for (Course course : result) {
						Log.i("courses", course.getName() + " " + course.getTeacherName() + " " + course.getCollegeName() + " " + course.getStartDate());
					}
					
					courses.clear();
					courses.addAll(result);
					
					courseAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(getActivity(), getString(R.string.no_courses_yet), Toast.LENGTH_LONG).show();
				}
			}
        }
	}
}
