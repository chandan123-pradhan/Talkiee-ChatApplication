package com.example.talkiee;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference chatsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        chatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList=(RecyclerView)PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }


    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatsRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsFragment.ChatsViewHoler> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsFragment.ChatsViewHoler>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsFragment.ChatsViewHoler holder, int position, @NonNull Contacts model) {
                final String userIDs=getRef(position).getKey();
                final String[] retImage = {"default_image"};
                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){


                            if(dataSnapshot.hasChild("image")){
                                retImage[0] =dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            }

                            final String retName=dataSnapshot.child("name").getValue().toString();
                            final String retStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(retName);



                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){
                                    holder.userStatus.setText("Online");
                                }
                                else    if(state.equals("offline")){
                                    holder.userStatus.setText("Last Seen: "+date+" Time "+time);
                                }
                            }
                            else{
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent=new Intent(getContext(),ChatsActivity.class);
                                    chatIntent.putExtra("visit_user_id",userIDs);
                                    chatIntent.putExtra("visit_user_name",retName);
                                    chatIntent.putExtra("visit_image", retImage[0]);

                                    startActivity(chatIntent);
                                }
                            });



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsFragment.ChatsViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new ChatsFragment.ChatsViewHoler(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHoler extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userStatus,userName;
        public ChatsViewHoler(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            userStatus=itemView.findViewById(R.id.user_status);

            userName=itemView.findViewById(R.id.user_profile_name);


        }
    }

}

