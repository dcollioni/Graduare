package com.dcollioni.graduare;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dcollioni.graduare.R.id;
import com.dcollioni.graduare.R.layout;
import com.dcollioni.graduare.entities.Course;

public class CourseAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Course> courses;
	
	public CourseAdapter(Context context, ArrayList<Course> courses) {
		super();
		this.context = context;
		this.courses = courses;
	}
	
	@Override
	public void notifyDataSetChanged() {
		try {
			super.notifyDataSetChanged();
		}
		catch (Exception e) {
			Log.i("CourseAdapter", "notifyDataSetChanged: " + e.getMessage());
		}
	}
	
	@Override
	public int getCount() {
		return courses.size();
	}

	@Override
	public Object getItem(int position) {
		return courses.get(position);
	}

	@Override
	public long getItemId(int position) {
		return courses.get(position).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		try {
			ViewHolder viewHolder;
			
			if (convertView == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
				convertView = inflater.inflate(layout.list_item_course, parent, false);
				
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			Course course = courses.get(position);
			
			if (course != null) {
				viewHolder.tvName.setText(course.getName());
				viewHolder.tvTeacherName.setText(course.getTeacherName());
				viewHolder.tvCollegeName.setText(course.getCollegeName());
				viewHolder.tvStartDate.setText(course.getStartDate());
			}
			
			return convertView;
		} catch (Exception e) {
			Log.i("CourseAdapter", "getView: " + e.getMessage());
		}
		
		return null;
	}
	
	public static class ViewHolder {
		public final TextView tvName;
		public final TextView tvTeacherName;
		public final TextView tvCollegeName;
		public final TextView tvStartDate;
	 
	    public ViewHolder(View view) {
	    	tvName = (TextView)view.findViewById(id.list_item_course_textview);
			tvTeacherName = (TextView)view.findViewById(id.list_item_teacher_textview);
			tvCollegeName = (TextView)view.findViewById(id.list_item_college_textview);
			tvStartDate = (TextView)view.findViewById(id.list_item_date_textview);
	    }
	}
}