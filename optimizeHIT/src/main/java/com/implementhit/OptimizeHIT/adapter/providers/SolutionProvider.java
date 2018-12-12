package com.implementhit.OptimizeHIT.adapter.providers;

import com.implementhit.OptimizeHIT.models.Solution;

/**
 * Created by acerkinght on 7/29/16.
 */
public interface SolutionProvider {
    Solution solutionForPosition(int position);
}
