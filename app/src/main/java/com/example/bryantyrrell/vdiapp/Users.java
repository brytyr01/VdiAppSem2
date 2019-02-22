package com.example.bryantyrrell.vdiapp;

import java.util.ArrayList;
// class used to create user object
public class Users {
    private String UserName;
    private String UserID,FirstName;
    private boolean Instructor;
    //private ArrayList<Users> users=new ArrayList<>();


    public Users(String UserName, String UserID,String FirstName,boolean Instructor){
        this.UserName=UserName;
        this.UserID=UserID;
        this.FirstName=FirstName;
        this.Instructor=Instructor;

    }


    public Users() {

    }

    public String getUserName(){
        return UserName;
    }
    public String getFirstName(){
        return FirstName;
    }
    public String getUserID(){
        return UserID;
    }
    public Boolean getInstructor(){return Instructor;}
    public void setUserName(String UserName){
        this.UserName=UserName;
    }
    public void setFirstName(String FirstName){
        this.FirstName=FirstName;
    }
    public void setUserID(String UserID){
         this.UserID=UserID;
    }
    public void setInstructor(Boolean Instructor){this.Instructor=Instructor;}
}
