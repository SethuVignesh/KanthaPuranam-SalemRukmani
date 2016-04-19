package com.newtra.anatomictherapy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.newtra.anatomictherapy.Beans.Book;
import com.newtra.anatomictherapy.R;

import java.util.ArrayList;
import java.util.List;


public class ListViewAdapter extends BaseAdapter {

	// Declare Variables
	Context mContext;
	LayoutInflater inflater;
	private List<Book> worldpopulationlist = null;
	private ArrayList<Book> arraylist;

	public ListViewAdapter(Context context, List<Book> worldpopulationlist) {
		mContext = context;
		this.worldpopulationlist = worldpopulationlist;
		inflater = LayoutInflater.from(mContext);
		this.arraylist = new ArrayList<Book>();
		this.arraylist.addAll(worldpopulationlist);
	}

	public class ViewHolder {
		TextView rank;
		TextView country;
//		TextView population;
		Button bookCover;
	}

	@Override
	public int getCount() {
		return worldpopulationlist.size();
	}

	@Override
	public Book getItem(int position) {
		return worldpopulationlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup parent) {
		final ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.store_list_list_view_layout, null);
			// Locate the TextViews in listview_item.xml
			holder.rank = (TextView) view.findViewById(R.id.store_list_item_text);
			holder.country = (TextView) view.findViewById(R.id.textViewDescription);
			holder.bookCover = (Button) view.findViewById(R.id.btnBookCover);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		// Set the results into TextViews
		holder.rank.setText(worldpopulationlist.get(position).getTitle());
		holder.country.setText(worldpopulationlist.get(position).getDescription());
		holder.bookCover.setBackground(mContext.getResources().getDrawable(worldpopulationlist.get(position).getBookCoverId()));
		
		// Listen for ListView Item Click
//		view.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// Send single item click data to SingleItemView Class
//				Intent intent = new Intent(mContext, SingleItemView.class);
//				// Pass all data rank
//				intent.putExtra("rank",(worldpopulationlist.get(position).getRank()));
//				// Pass all data country
//				intent.putExtra("country",(worldpopulationlist.get(position).getCountry()));
//				// Pass all data population
//				intent.putExtra("population",(worldpopulationlist.get(position).getPopulation()));
//				// Pass all data flag
//				// Start SingleItemView Class
//				mContext.startActivity(intent);
//			}
//		});

		return view;
	}

	// Filter Class
//	public void filter(String charText) {
//		charText = charText.toLowerCase(Locale.getDefault());
//		worldpopulationlist.clear();
//		if (charText.length() == 0) {
//			worldpopulationlist.addAll(arraylist);
//		}
//		else
//		{
//			for (Book wp : arraylist)
//			{
//				if (wp.toLowerCase(Locale.getDefault()).contains(charText))
//				{
//					worldpopulationlist.add(wp);
//				}
//			}
//		}
//		notifyDataSetChanged();
//	}

}
