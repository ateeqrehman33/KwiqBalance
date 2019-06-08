package com.kwiqbalance.models;

/**
 * Created by Ankit Chouhan on 29-12-2017.
 */

public class OperatorModel {

    private String displayName;
    private int subscriptionId;


    public OperatorModel(String displayName,int subscriptionId){
        this.displayName = displayName;
        this.subscriptionId = subscriptionId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
