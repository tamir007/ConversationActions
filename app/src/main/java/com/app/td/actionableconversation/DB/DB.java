package com.app.td.actionableconversation.DB;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by user on 14/02/2016.
 */
public class DB implements Serializable{

    HashMap<String,Character> sTocMap;
    HashMap<Character,String> cTosMap;
    public DB() {
        sTocMap = new HashMap<>();
        cTosMap = new HashMap<>();
    }

    public void addUsers(String[] users){
        if(users.length != 5){
            return;
        }
        sTocMap.put(users[0], '1');
        sTocMap.put(users[1], '2');
        sTocMap.put(users[2], '3');
        sTocMap.put(users[3], '4');
        sTocMap.put(users[4], '5');
        sTocMap.put("other", '6');

        cTosMap.put('1', users[0]);
        cTosMap.put('2', users[1]);
        cTosMap.put('3', users[2]);
        cTosMap.put('4', users[3]);
        cTosMap.put('5', users[4]);
        cTosMap.put('6', "other");
    }

    public HashMap<String,Character> getSToC(){
        return sTocMap;
    }

    public HashMap<Character,String> getCToS(){
        return cTosMap;
    }



}
