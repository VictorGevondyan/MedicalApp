package com.implementhit.OptimizeHIT.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Solution implements Parcelable, Comparable<Solution> {
	private final static String ID = "id";
	private final static String TITLE = "title";
	private final static String FULL_TITLE = "full_title";
	private final static String LIKES = "likes";
	private final static String DATE = "date";
	private final static String WEIGHT = "weight";
	private static final String VIEWED = "viewed";
	private static final String TYPE = "type";
	private static final String CME = "cme";
	private static final String RATING = "rating";
	
	private int id;
	private String title;
	private String categoryName = null;
	private int likes = -1;
	private String timestemp = null;
	private long date = -1;
	private int weight = -1;
	private int type = -1;
	private boolean viewed;
	private boolean hasCME;
	private float rating = -1 ;
	private boolean ratedByUser;
	private boolean skipsSubcategory = false;
	private boolean isFinalSolution = true; // TODO: Decouple in different models

	public Solution(JSONObject solution) {
		id = solution.optInt(ID);
		likes = solution.optInt(LIKES);
		
		if (solution.has(TITLE)) {
			title = solution.optString(TITLE);
		} else {
			title = solution.optString(FULL_TITLE);
		}
		
		if (solution.has(DATE)) {
			date = solution.optLong(DATE);
		}
		
		if (solution.has(WEIGHT)) {
			weight = solution.optInt(WEIGHT);
		}
		
		if (solution.has(VIEWED)) {
			viewed = solution.optString(VIEWED, "0").equals("1");
		}

		if (solution.has(CME)) {
			hasCME = true;
		}

		if (solution.has(RATING)) {
			String ratingString = solution.optString(RATING);
			if( ratingString == null || ratingString.equals("null")){
				rating = -1;
			} else {
				rating = Float.valueOf(ratingString);
			}

		}

		ratedByUser = false;

		if (solution.has(TYPE)) {
			type = solution.optInt(TYPE);
		}

		isFinalSolution = true;
	}
	
	public Solution(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int solutionId() {
		return id;
	}
	
	public String title() {
		return title;
	}

	public String categoryName() {
		return categoryName;
	}
	
	public int likes() {
		return likes;
	}
	
	public int weight() {
		return weight;
	}
	
	public boolean hasCME() {
		return hasCME;
	}

	public float rating() {
		return rating;
	}

	public boolean isRatedByUser() {
		return ratedByUser;
	}

	public float formattedRating() {
		int quarter = (int) Math.floor(rating / 0.25f);

		return quarter * 0.25f;
	}

	public boolean viewed() {
		return viewed;
	}

	public void setTimestemp(String timestemp) {
		this.timestemp = timestemp;
	}
	
	public String timestemp() {
		return timestemp;
	}
	
	public long date() {
		return date;
	}
	
	public int type() {
		return type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCategoryName(String categoryName ) {
		this.categoryName = categoryName;
	}

	public void setDate(int date) {
		this.date = date;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}

	public void setHasCME(boolean hasCME) {
		this.hasCME = hasCME;
	}

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setRatedByUser( boolean ratedByUser ) {
        this.ratedByUser = ratedByUser;
    }

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public void setSkipsSubcategory(boolean skipsSubcategory) {
		this.skipsSubcategory = skipsSubcategory;
	}

	public boolean isSkipsSubcategory() {
		return skipsSubcategory;
	}

	public boolean isFinalSolution() {
		return isFinalSolution;
	}

	public void setFinalSolution(boolean finalSolution) {
		isFinalSolution = finalSolution;
	}

	/*
	 * Comparable Methods
	 */

	@Override
	public int compareTo(Solution solution) {
		if (solution.likes != -1 && this.likes != -1) {
			int titleCriteria = solution.title.compareTo(this.title);
			
			if (titleCriteria == 0) {
				return solution.likes - this.likes;
			}
			
			return titleCriteria;
		} else if (solution.weight != -1 && this.weight != -1) {
			if ((solution.viewed && this.viewed)
					|| (!solution.viewed &&!this.viewed)) {
				return this.weight - solution.weight;
			} else {
				return solution.viewed ? 1 : -1;
			}
		}
		
		return 0;
	}
	
	/**
	 * Parcelable Methods
	 */
	
    public Solution(Parcel in){
        this.id = in.readInt();
        this.title = in.readString();
		this.categoryName = in.readString();
		likes = in.readInt();
		timestemp = in.readString();
		date = in.readLong();
		weight = in.readInt();
		type = in.readInt();
		viewed  = in.readByte() != 0;
		hasCME = in.readByte() != 0;
		rating = in.readFloat();
		skipsSubcategory = in.readByte() != 0;
		isFinalSolution = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
		dest.writeString(categoryName);
		dest.writeInt(likes);
		dest.writeString(timestemp);
		dest.writeLong(date);
		dest.writeInt(weight);
		dest.writeInt(type);
		dest.writeByte((byte) (viewed ? 1 : 0));
		dest.writeByte((byte) (hasCME ? 1 : 0));
		dest.writeFloat(rating);
		dest.writeByte((byte) (skipsSubcategory ? 1 : 0));
		dest.writeByte((byte) (isFinalSolution ? 1 : 0));
    }
    
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		@Override
        public Solution createFromParcel(Parcel in) {
            return new Solution(in); 
        }

		@Override
        public Solution[] newArray(int size) {
            return new Solution[size];
        }
    };

	@Override
	public int describeContents() {
		return id;
	}
	
	@Override
	public String toString() {
		return id + ":" + title;
	}
}