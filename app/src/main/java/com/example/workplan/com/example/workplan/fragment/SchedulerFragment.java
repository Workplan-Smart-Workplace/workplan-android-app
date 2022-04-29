package com.example.workplan.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.workplan.DeclinedMeetingsActivity;
import com.example.workplan.DeletedTasksActivity;
import com.example.workplan.NewTaskActivity;
import com.example.workplan.NotificationsActivity;
import com.example.workplan.R;
import com.example.workplan.SchedulerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SchedulerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SchedulerFragment extends Fragment {



    public Button tNewTaskBtn,recentlyDeleted;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private CallbacksCommunicatoer mCallbacks = sDummyCallbacks;

    public SchedulerFragment() {
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
    public static SchedulerFragment newInstance(String param1, String param2) {
        SchedulerFragment fragment = new SchedulerFragment();
        return fragment;
    }

    private static CallbacksCommunicatoer sDummyCallbacks = new CallbacksCommunicatoer() {


        @Override
        public void DataSend(String Command) {

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CallbacksCommunicatoer)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (CallbacksCommunicatoer) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = sDummyCallbacks;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  tList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scheduler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // declare XML elements
        tNewTaskBtn = view.findViewById(R.id.newTask);
        recentlyDeleted=view.findViewById(R.id.recentlyDeleted);


        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        tNewTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), NewTaskActivity.class);
                startActivity(i);
            }
        });
  recentlyDeleted.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          mCallbacks.DataSend("DELETEDTASK");
      }
  });
        View declinedMeetings = view.findViewById(R.id.declinedMeetings);
        declinedMeetings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbacks.DataSend("DECLINEMEETING");
            }
        });


    }









}