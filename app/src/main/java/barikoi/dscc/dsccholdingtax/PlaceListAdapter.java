package barikoi.dscc.dsccholdingtax;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Sakib on 12/23/2017.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> implements Filterable {

    private ArrayList<Place> places;
    ///private Context context;
    private OnPlaceItemSelectListener opsl;
    private ArrayList<Place> placeListFiltered;

    public PlaceListAdapter(ArrayList<Place> places, OnPlaceItemSelectListener opsl){
        this.places=places;
        this.placeListFiltered=places;
        this.opsl=opsl;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.placelistitem, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = placeListFiltered.get(position);

        holder.placeView.setText(holder.mItem.getHoldingNo());
        holder.areatag.setText(holder.mItem.getArea());
        holder.postcodeView.setText(holder.mItem.getPostalcode());
        holder.ward.setText(holder.mItem.getWard());
        holder.zone.setText(holder.mItem.getZone());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                opsl.onPlaceItemSelected( holder.mItem,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();

                if (charString.isEmpty()) {
                    placeListFiltered = places;
                } else {
                    ArrayList<Place> filteredList = new ArrayList<>();
                    for (Place row : places) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getHoldingNo().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    placeListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = placeListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                placeListFiltered = (ArrayList<Place>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        final View mView;
        final TextView postcodeView;
        final TextView placeView;
        final TextView areatag;
        final TextView ward;
        final TextView zone;
        final ImageView delete;
        LinearLayout tagLayout;
        TextView textViewTags;
        Place mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            postcodeView =  mView.findViewById(R.id.textViewPostCode);
            placeView = mView.findViewById(R.id.textView_placename);
            areatag= mView.findViewById(R.id.textViewArea);
            ward= mView.findViewById(R.id.textViewWard);
            zone= mView.findViewById(R.id.textViewZone);

            delete=  mView.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.showContextMenu();
                }
            });
            mView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            //menuInfo is null
            MenuItem Edit = menu.add(Menu.NONE, 1, 1, "Edit");
            MenuItem Delete = menu.add(Menu.NONE, 2, 2, "Delete");
            MenuItem Okay = menu.add(Menu.NONE, 3, 3, "Correct");

            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);
            Okay.setOnMenuItemClickListener(onEditMenu);
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case 1:
                        //Edit function
                        opsl.onPlaceEdited(mItem);
                        break;

                    case 2:
                        //Delete place
                        opsl.onPlacedeleted(mItem);
                        break;
                    case 3:
                        //place is coorect verify
                        opsl.onPlaceCorrect(mItem);
                }
                return true;
            }
        };

        @Override
        public String toString() {
            return super.toString() + " '" + placeView.getText() + "'";
        }


    }

    public interface OnPlaceItemSelectListener{

        void onPlaceItemSelected(Place mItem, int position);

        void onPlacedeleted(Place mItem);

        void onPlaceEdited(Place mItem);

        void onPlaceCorrect(Place mItem);
    }

}
