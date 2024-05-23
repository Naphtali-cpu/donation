package com.edna.fooddonation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edna.fooddonation.R;
import com.edna.fooddonation.model.Donation;
import com.edna.fooddonation.ui.DonationDetail;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class DonationListAdapter extends RecyclerView.Adapter<DonationListAdapter.ViewHolder> {

    private Context context;
    private List<Donation> donationList;
    private DatabaseReference databaseReference;

    public DonationListAdapter(Context context, List<Donation> donationList, DatabaseReference databaseReference) {
        this.context = context;
        this.donationList = donationList;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public DonationListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.donation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationListAdapter.ViewHolder holder, int position) {
        Donation donation = donationList.get(position);
        String donationRefKey = databaseReference.getKey();

        holder.txtDonation.setText(donation.getFoodItem());
        holder.txtDescription.setText(donation.getDescription());
        holder.btnRequestDonation.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DonationDetail.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("donationRefKey", donationRefKey);
            intent.putExtra("donation", donation.getFoodItem());
            intent.putExtra("donorname", donation.getFullName());
            intent.putExtra("description", donation.getDescription());
            intent.putExtra("long", donation.getLongitude());
            intent.putExtra("lat", donation.getLatitude());
            intent.putExtra("phone", donation.getPhone());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDonation;
        TextView txtDescription;
        Button btnRequestDonation;

        public ViewHolder(View itemView) {
            super(itemView);
            txtDonation = itemView.findViewById(R.id.textDonation);
            txtDescription = itemView.findViewById(R.id.donationDescription);
            btnRequestDonation = itemView.findViewById(R.id.requestDonationBtn);
        }
    }
}
