package com.implementhit.OptimizeHIT.api;

/**
 * Created by victor on 8/25/16.
 */
public interface RateSolutionListener {
    void rateSolutionSuccess();
    void rateSolutionFail( String error );
}
