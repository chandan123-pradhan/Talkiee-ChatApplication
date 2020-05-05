package com.example.talkiee;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
       switch(position){
           case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
               ContatcsFragment contactsFragment = new  ContatcsFragment();
               return contactsFragment;

           case 2:
               RequestsFragment requestsFragment = new RequestsFragment();
               return requestsFragment;

           default:
               return null;
       }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Chats";


            case 1:
                return "Contacts";

            case 2:
                return "Requests";

            default:
                return null;
        }
    }

}
