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
import com.dcollioni.graduare.entities.ExamResult;

public class ExamResultAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ExamResult> mExamResults;
	
	public ExamResultAdapter(Context context, ArrayList<ExamResult> examResults) {
		super();
		this.mContext = context;
		this.mExamResults = examResults;
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
		return mExamResults.size();
	}

	@Override
	public Object getItem(int position) {
		return mExamResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mExamResults.get(position).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		try {
			ViewHolder viewHolder;
			
			if (convertView == null) {
				LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
				convertView = inflater.inflate(layout.list_item_exam_result, parent, false);
				
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			ExamResult obj = mExamResults.get(position);
			
			if (obj != null) {
				viewHolder.tvExamName.setText(obj.getExamName());
				viewHolder.tvExamDate.setText(obj.getExamDateStr());
				viewHolder.tvExamValue.setText(Utility.parseDouble(obj.getExamValue()));
				viewHolder.tvResultValue.setText(Utility.parseDouble(obj.getValue()));
			}
			
			return convertView;
		} catch (Exception e) {
			Log.i("CourseAdapter", "getView: " + e.getMessage());
		}
		
		return null;
	}
	
	public static class ViewHolder {
		public final TextView tvExamName;
		public final TextView tvExamDate;
		public final TextView tvExamValue;
		public final TextView tvResultValue;
	 
	    public ViewHolder(View view) {
	    	tvExamName = (TextView)view.findViewById(id.list_item_exam_name_textview);
			tvExamDate = (TextView)view.findViewById(id.list_item_exam_date_textview);
			tvExamValue = (TextView)view.findViewById(id.list_item_exam_value_textview);
			tvResultValue = (TextView)view.findViewById(id.list_item_result_value_textview);
	    }
	}
}