package code.diegohdez.navbottom.githubapijava.Adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.PullsAdapter;
import code.diegohdez.githubapijava.AsyncTask.PullsRepo;
import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.PullsPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_PULLS;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class PullsFragment extends Fragment {

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    PullsFragment fragment;
    PullsAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<DataOfPulls> list = new ArrayList<>();
    Bundle args;
    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;

    public static PullsFragment newInstance() {
        return new PullsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pulls_fragment, container, false);
        fragment = this;
        args = getArguments();
        adapter = new PullsAdapter(this.list);
        recyclerView = root.findViewById(R.id.pulls_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PullsPaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadPulls() {
                isLoading = true;
                page++;
                AppManager.getOurInstance().setPullsPage(page);
                Toast.makeText(
                        getActivity(),
                        "Display " + page + " page",
                        Toast.LENGTH_SHORT).show();
                PullsRepo issuesRepo = new PullsRepo(fragment, args.getLong(ARG_ID));
                issuesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" +args.getString(ARG_REPO_NAME) + USER_PULLS + STATE_ALL + "&page=" + page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        setPullsList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        page = AppManager.getOurInstance().getCurrentPullsPage();
    }

    public void setPullsList() {
        Realm realm = Realm.getDefaultInstance();
        Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
        RealmList<Pull> pulls = repo != null ? repo.getPulls() : new RealmList<Pull>();
        ArrayList<DataOfPulls> list = DataOfPulls.createList(pulls);
        this.adapter.addPulls(list);
        adapter.addLoading();
        realm.close();
    }

    public void addPulls(RealmList<Pull> pulls) {
        adapter.deleteLoading();
        isLoading = false;
        ArrayList<DataOfPulls> list = DataOfPulls.createList(pulls);
        this.adapter.addPulls(list);
        if (list.size() < PAGE_SIZE) isLastPage = true;
        else adapter.addLoading();
    }
}
