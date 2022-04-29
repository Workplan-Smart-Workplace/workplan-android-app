package com.example.workplan.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.Adapter.DeclinedMeetingsAdapter;
import com.example.workplan.DeclinedMeetingsActivity;
import com.example.workplan.DeletedTasksActivity;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.NewMeetingActivity;
import com.example.workplan.NewTaskActivity;
import com.example.workplan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeclinedMeetingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeclinedMeetingFragment extends Fragment {



    // global variable declarations
    private DeclinedMeetingsAdapter declinedMeetingsAdapter;
    private RecyclerView recyclerView;
    private List<MeetingModel> mList;
    private String userID, acceptedCheck;


    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;


    public DeclinedMeetingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SchedulerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeclinedMeetingFragment newInstance(String param1, String param2) {
        DeclinedMeetingFragment fragment = new DeclinedMeetingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = new ArrayList<>();

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_declined_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // declare meetings recyclerView
        recyclerView = view.findViewById(R.id.homeCurrentMeetings);

        declinedMeetingsAdapter = new DeclinedMeetingsAdapter(getActivity(), mList);

        // Initialize recyclerView for meetings
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(declinedMeetingsAdapter);
        showMeetings();

    }

    private void showMeetings(){
        userID = fAuth.getCurrentUser().getUid();
        acceptedCheck = userID + "2";
        fStore.collection("meetings").whereArrayContains("invited", acceptedCheck).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null) {
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            String id = documentChange.getDocument().getId();
                            MeetingModel meetingModel = documentChange.getDocument().toObject(MeetingModel.class).withId(String.valueOf(id));
                            mList.add(meetingModel);
                            declinedMeetingsAdapter.notifyDataSetChanged();
                            if (mList.size() == 0) {
                                //topText.setText("You have no declined meetings.");
                            }
                        }
                    }
                }
            }
        });
    }










}