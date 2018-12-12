package com.implementhit.OptimizeHIT.api;

/**
 * Created by anhaytananun on 19.07.16.
 */
public interface OnCheckDomainListener {
    void onDomainCheckSuccess(String domain, String domainName, String imageUrl, String primaryColor);
    void onDomainCheckFailure(String error);
}
