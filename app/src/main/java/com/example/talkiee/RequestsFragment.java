package com.example.talkiee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestRef,userRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
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
        RequestFragmentView=inflater.inflate(R.layout.fragment_requests, container, false);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        myRequestList=(RecyclerView)RequestFragmentView.findViewById(R.id.chat_reqeust_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestFragmentView;
    }


    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatRequestRef.child(currentUserID), Contacts.class).build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHoler> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHoler>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHoler holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(position).getKey();

                final DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {

                            String type=dataSnapshot.getValue().toString();
                            if(type.equals("received"))
                            {
                                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){

                                            final String requestprofileImage=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestprofileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("Want to connect With you");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]{
                                                        "Accept",
                                                        "Cancel"

                                                };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName +"  Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i==0){
                                                            ContactsRef.child(currentUserID).child(list_user_id).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        ContactsRef.child(list_user_id).child(currentUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(),"New Contact Saved",Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                })  ;
                                                                                            }
                                                                                        }
                                                                                    })  ;
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if(i==1){

                                                            ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        })  ;
                                                                    }
                                                                }
                                                            })  ;
                                                        }
                                                    }
                                                });
                                                builder.show();


                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(type.equals("sent")){
                                Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_button);
                                request_sent_btn.setText("Request Sent");
                                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);


                                userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){

                                            final String requestprofileImage=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestprofileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }
                                        final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("You have sent a request to "+requestUserName);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]{

                                                        "Cancel chat Request"

                                                };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {

                                                        if(i==0){

                                                            ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"You have Cancel The Chat Request.",Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        })  ;
                                                                    }
                                                                }
                                                            })  ;
                                                        }
                                                    }
                                                });
                                                builder.show();


                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestViewHoler holder=new RequestViewHoler(view);
                return holder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHoler extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;
        public RequestViewHoler(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_button);
            CancelButton=itemView.findViewById(R.id.request_cancel_button);
        }
    }
}

