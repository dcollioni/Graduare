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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dcollioni.graduare.entities.Course;
import com.dcollioni.graduare.entities.ExamResult;
import com.dcollioni.graduare.entities.PartialGrade;

public class ExamActivity extends ActionBarActivity {

	public static final String COURSE_KEY = "course_key";
	public static final String STUDENT_KEY = "student_key";
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	private ArrayList<PartialGrade> mPartialGrades;
	private static Course mCourse;
	private static int mStudentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exam);
		
		Intent intent = getIntent();
		
		if (intent != null && intent.hasExtra(COURSE_KEY) && intent.hasExtra(STUDENT_KEY)) {
			
			mCourse = (Course) intent.getSerializableExtra(COURSE_KEY);
			setTitle(mCourse.getName());
			
			mStudentId = intent.getIntExtra(STUDENT_KEY, 0);
			
			new LoadPartialGradesAsyncTask().execute(mCourse.getId());	
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.exam, menu);
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
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			
			if (mPartialGrades != null) {
				int partialGradeId = mPartialGrades.get(position).getId();
				return PlaceholderFragment.newInstance(partialGradeId);	
			}
			
			return null;
		}

		@Override
		public int getCount() {
			
			if (mPartialGrades != null) {
				return mPartialGrades.size();
			}
			
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			if (mPartialGrades != null) {
				return mPartialGrades.get(position).getName();				
			}
			
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		private ProgressBar mPbLoading;
		private TextView mResultTotalTextView, mExamsTotalTextView;
		
		private ArrayList<ExamResult> mExamResults;
		private ExamResultAdapter mExamResultsAdapter;
		private ListView mExamResultsListView;
		
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_exam, container,
					false);
			
			mPbLoading = (ProgressBar) rootView.findViewById(R.id.progressbar_loading);
			mResultTotalTextView = (TextView) rootView.findViewById(R.id.result_total_textview);
			mExamsTotalTextView = (TextView) rootView.findViewById(R.id.exams_total_textview);
			
			mExamResults = new ArrayList<ExamResult>();
			
			mExamResultsAdapter = new ExamResultAdapter(getActivity(), mExamResults);

			mExamResultsListView = (ListView) rootView.findViewById(R.id.listview_exam_results);
			mExamResultsListView.setAdapter(mExamResultsAdapter);
			
			Bundle args = getArguments();
			
			if (args != null && args.containsKey(ARG_SECTION_NUMBER)) {
				int partialGradeId = args.getInt(ARG_SECTION_NUMBER);
				
				new LoadExamResultsAsyncTask().execute(partialGradeId);
			}
			
			return rootView;
		}
		
		private class LoadExamResultsAsyncTask extends AsyncTask<Integer, Void, ArrayList<ExamResult>> {

			@Override
			protected ArrayList<ExamResult> doInBackground(Integer... params) {
				
				publishProgress();
				
				int partialGradeId = params[0];
				int courseId = mCourse.getId();
				
				HttpURLConnection urlConnection = null;
		        BufferedReader reader = null;

		        String dataJsonStr = null;

		        try {
		            final String GET_EXAM_RESULTS_BASE_URL = "http://schoollineup.apphb.com/api/ExamResults?";
		            
		            final String PARTIAL_GRADE_ID = "partialGradeId";
		            final String STUDENT_ID = "studentId";
		            final String COURSE_ID = "courseId";

		            Uri builtUri = Uri.parse(GET_EXAM_RESULTS_BASE_URL).buildUpon()
		                    .appendQueryParameter(PARTIAL_GRADE_ID, Integer.toString(partialGradeId))
		                    .appendQueryParameter(STUDENT_ID, Integer.toString(mStudentId))
		                    .appendQueryParameter(COURSE_ID, Integer.toString(courseId))
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

		        ArrayList<ExamResult> result = new ArrayList<ExamResult>();
		        
		        try {
		            if (dataJsonStr != null) {
		            	
		            	JSONArray jsonArray = new JSONArray(dataJsonStr);
		            	
		            	if (jsonArray.length() > 0) {
		            		
		            		for (int i = 0; i < jsonArray.length(); i++) {
	            				
		            			JSONObject jsonObj = jsonArray.getJSONObject(i);
		            			
		            			ExamResult obj = new ExamResult();
		            			obj.setId(jsonObj.getInt("Id"));
		            			obj.setDescription(jsonObj.getString("Description"));
		            			obj.setExamDateStr(jsonObj.getString("ExamDateStr"));
		            			obj.setExamDescription(jsonObj.getString("ExamDescription"));
		            			obj.setExamId(jsonObj.getInt("ExamId"));
		            			obj.setExamName(jsonObj.getString("ExamName"));
		            			obj.setExamValue(jsonObj.getDouble("ExamValue"));
		            			obj.setValue(jsonObj.getDouble("Value"));
		            			
		            			result.add(obj);
							}
		            	}
		            }
		        } catch (JSONException e) {
		            e.printStackTrace();
		        }
				
				return result;
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
				mPbLoading.setVisibility(View.VISIBLE);
			}
			
			@Override
			protected void onPostExecute(ArrayList<ExamResult> result) {
				super.onPostExecute(result);
				mPbLoading.setVisibility(View.GONE);
				
				if (result != null) {
					
					mExamResults.clear();
					mExamResults.addAll(result);
					
					mExamResultsAdapter.notifyDataSetChanged();
					
					double resultsTotal = 0;
					double examsTotal = 0;
					
					for (ExamResult examResult : mExamResults) {
						resultsTotal += examResult.getValue();
						examsTotal += examResult.getExamValue();
					}
					
					mResultTotalTextView.setText(Utility.parseDouble(resultsTotal));
					mExamsTotalTextView.setText(Utility.parseDouble(examsTotal));
					
					Bundle args = getArguments();
					
					if (args != null && args.containsKey(ARG_SECTION_NUMBER)) {
						if (args.getInt(ARG_SECTION_NUMBER) < 0) {
							mResultTotalTextView.setText(Utility.parseDouble(resultsTotal/mExamResults.size()));
							mExamsTotalTextView.setText(Utility.parseDouble(examsTotal/mExamResults.size()));
						}
					}
					
				} else {
					Toast.makeText(getActivity(), getString(R.string.no_exam_results), Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	private class LoadPartialGradesAsyncTask extends AsyncTask<Integer, Void, ArrayList<PartialGrade>> {

		@Override
		protected ArrayList<PartialGrade> doInBackground(Integer... params) {
			
			publishProgress();
			
			int courseId = params[0];
			
			HttpURLConnection urlConnection = null;
	        BufferedReader reader = null;

	        String dataJsonStr = null;

	        try {
	            final String GET_PARTIAL_GRADES_BASE_URL =
	                    "http://schoollineup.apphb.com/api/PartialGrades?";
	            
	            final String COURSE_ID = "courseId";

	            Uri builtUri = Uri.parse(GET_PARTIAL_GRADES_BASE_URL).buildUpon()
	                    .appendQueryParameter(COURSE_ID, Integer.toString(courseId))
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

	        ArrayList<PartialGrade> result = new ArrayList<PartialGrade>();
	        
	        try {
	            if (dataJsonStr != null) {
	            	
	            	JSONArray jsonArray = new JSONArray(dataJsonStr);
	            	
	            	if (jsonArray.length() > 0) {
	            		
	            		for (int i = 0; i < jsonArray.length(); i++) {
            				
	            			JSONObject jsonObj = jsonArray.getJSONObject(i);
	            			
	            			PartialGrade obj = new PartialGrade();
	            			obj.setId(jsonObj.getInt("Id"));
	            			obj.setOrder(jsonObj.getInt("Order"));
	            			obj.setName(jsonObj.getString("Name"));
	            			
	            			result.add(obj);
						}
	            		
	            		PartialGrade total = new PartialGrade();
	            		total.setId(-1);
	            		total.setName("Total");
	            		
	            		result.add(total);
	            	}
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
			
			return result;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(ArrayList<PartialGrade> result) {
			super.onPostExecute(result);
			
			mPartialGrades = result;
			
			// Create the adapter that will return a fragment for each of the three
			// primary sections of the activity.
			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());

			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
		}
	}
}
