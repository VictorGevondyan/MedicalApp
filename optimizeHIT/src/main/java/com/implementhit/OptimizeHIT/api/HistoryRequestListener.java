package com.implementhit.OptimizeHIT.api;

import org.json.JSONObject;

/**
 * Created by victor on 8/25/16.
 */
public interface HistoryRequestListener {
    void historyRequestSuccess( JSONObject userHistory );
    void historyRequestFailure( String error );
}
