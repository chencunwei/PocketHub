package com.github.mobile.issue;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.ConfirmDialogFragment;
import com.github.mobile.DialogFragmentActivity;
import com.github.mobile.HomeActivity;
import com.github.mobile.RequestFuture;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.util.AccountHelper;
import com.github.mobile.util.GitHubIntents.Builder;
import com.google.inject.Inject;

/**
 * Activity to browse a list of bookmarked {@link IssueFilter} items
 */
public class FilterBrowseActivity extends DialogFragmentActivity implements OnItemLongClickListener {

    /**
     * Create intent to browse issue filters
     *
     * @return intent
     */
    public static Intent createIntent() {
        return new Builder("repo.issues.filters.VIEW").toIntent();
    }

    private static final String ARG_FILTER = "filter";

    private static final int REQUEST_DELETE = 1;

    @Inject
    private AccountDataManager cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.issue_filter_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.saved_filters_title);
        actionBar.setSubtitle(AccountHelper.getLogin(this));
        actionBar.setDisplayHomeAsUpEnabled(true);

        FilterListFragment filterFragment = (FilterListFragment) getSupportFragmentManager().findFragmentById(
                android.R.id.list);
        filterFragment.getListView().setOnItemLongClickListener(this);
    }

    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (requestCode == REQUEST_DELETE && resultCode == RESULT_OK) {
            IssueFilter filter = (IssueFilter) arguments.getSerializable(ARG_FILTER);
            cache.removeIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    ((FilterListFragment) getSupportFragmentManager().findFragmentById(android.R.id.list)).refresh();
                }
            });
            return;
        }
        super.onDialogResult(requestCode, resultCode, arguments);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        IssueFilter filter = (IssueFilter) parent.getItemAtPosition(position);
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, filter);
        ConfirmDialogFragment.show(this, REQUEST_DELETE, null, getString(string.confirm_filter_delete_message), args);
        return true;
    }
}