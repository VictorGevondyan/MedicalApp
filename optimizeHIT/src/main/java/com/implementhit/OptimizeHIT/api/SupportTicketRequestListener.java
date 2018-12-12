package com.implementhit.OptimizeHIT.api;

/**
 * Created by anhaytananun on 19.01.16.
 */
public interface SupportTicketRequestListener {
    void onSupportTicketRequestSuccess(String title, String message);
    void onSupportTicketRequestFail(String error, String message);
}
