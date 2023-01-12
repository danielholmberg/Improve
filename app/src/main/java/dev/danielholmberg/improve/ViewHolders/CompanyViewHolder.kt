package dev.danielholmberg.improve.ViewHolders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Models.Company;
import dev.danielholmberg.improve.Fragments.CompanyDetailsDialogFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

public class CompanyViewHolder extends RecyclerView.ViewHolder {

    private View mView;

    private TextView companyName;
    private Button quickAddNewContact;
    public RecyclerView contactRecyclerView;

    public CompanyViewHolder(View companyView) {
        super(companyView);
        mView = companyView;
    }

    public void bindModelToView(final Company company) {
        companyName = (TextView) mView.findViewById(R.id.company_list_tv);
        companyName.setText(company.getName());
        quickAddNewContact = (Button) mView.findViewById(R.id.quick_add_contact_btn);
        contactRecyclerView = (RecyclerView) mView.findViewById(R.id.contact_recycler_view);

        quickAddNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addContactIntent = new Intent(Improve.getInstance(), AddContactActivity.class);
                addContactIntent.putExtra(AddContactActivity.PRE_SELECTED_COMPANY, company);
                addContactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Improve.getInstance().startActivity(addContactIntent);
            }
        });

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = createCompanyBundle(company);
                CompanyDetailsDialogFragment companyDetailsDialogFragment = new CompanyDetailsDialogFragment();
                companyDetailsDialogFragment.setArguments(bundle);
                companyDetailsDialogFragment.show(Improve.getInstance().getMainActivityRef().getSupportFragmentManager(),
                        companyDetailsDialogFragment.getTag());
            }
        });
    }

    private Bundle createCompanyBundle(Company company) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CompanyDetailsDialogFragment.COMPANY_KEY, company);
        return bundle;
    }
}
