package com.entity;

import java.util.Vector;

/**
 * Contain infomations about a book including id, rating, title, author,
 * comment, tags, price, image, keypoints and descriptors.
 * 
 * @author hoangtung
 * 
 */
public class TargetImageResponse implements Comparable<TargetImageResponse> {
	// protected IntegerField _ID, _rateCount, _width, _height;
	// protected CharField _name, _author, _description, _image, _bigImg;
	// protected DoubleField _rating;
	// protected BlobField _keys, _dess;

	public int ID, rateCount;
	public String name;
	public String author;
	public String description;
	public float rating;
	public String image;
	String bigImg;
	public Vector<KeyPoint> keys;
	public Mat dess;
	int matchKeys;
	public int width;
	public int height;

	// @Override
	// public boolean save(Context context) {
	// _ID.set(ID);
	// _rateCount.set(rateCount);
	// _width.set(width);
	// _height.set(height);
	// _name.set(name);
	// _author.set(author);
	// _description.set(description);
	// _image.set(image);
	// _bigImg.set(bigImg);
	// _rating.set((double) rating);
	// _keys.set(Util.objectToByteArray(keys));
	// _dess.set(Util.objectToByteArray(dess));
	// return super.save(context);
	// }

	/**
	 * Create a empty book
	 */
	public TargetImageResponse() {

	}

	/**
	 * Copy data from another book to this book
	 * 
	 * @param b
	 */
	public TargetImageResponse(TargetImageResponse b) {
		ID = b.ID;
		name = b.name;
		author = b.author;
		description = b.description;
		rating = b.rating;
		rateCount = b.rateCount;
		image = b.image;
		matchKeys = b.matchKeys;
	}

	/**
	 * Create a new book without keypoints and descriptor
	 * 
	 * @param id
	 * @param tit
	 * @param au
	 * @param in
	 * @param ta
	 * @param ra
	 * @param rc
	 * @param img
	 * @param p
	 */
	public TargetImageResponse(int id, String tit, String au, String de,
			float ra, int rc, String img) {
		ID = id;
		name = tit;
		author = au;
		description = de;
		rating = ra;
		rateCount = rc;
		image = img;
		matchKeys = 0;

	}

	/**
	 * Create a new book with keypoints and descriptors
	 * 
	 * @param id
	 * @param tit
	 * @param au
	 * @param in
	 * @param ta
	 * @param ra
	 * @param rc
	 * @param img
	 * @param p
	 * @param keys
	 * @param dess
	 */
	public TargetImageResponse(int id, String tit, String au, String de,
			float ra, int rc, String img, Vector<KeyPoint> keys, Mat dess) {
		ID = id;
		name = tit;
		author = au;
		description = de;
		rating = ra;
		rateCount = rc;
		image = img;
		matchKeys = 0;
		this.keys = keys;
		this.dess = dess;

	}

	public TargetImageResponse(int id, String tit, String au, String de,
			float ra, int rc, String img, Vector<KeyPoint> keys, Mat dess,
			int width, int height) {
		ID = id;
		name = tit;
		author = au;
		description = de;
		rating = ra;
		rateCount = rc;
		image = img;
		matchKeys = 0;
		this.keys = keys;
		this.dess = dess;
		this.width = width;
		this.height = height;
	}

	/**
	 * Create a book with keypoints, descritors and number of matches
	 * 
	 * @param id
	 * @param tit
	 * @param au
	 * @param in
	 * @param ta
	 * @param ra
	 * @param rc
	 * @param img
	 * @param p
	 * @param keys
	 * @param dess
	 * @param match
	 */
	public TargetImageResponse(int id, String tit, String au, String de,
			float ra, int rc, String img, Vector<KeyPoint> keys, Mat dess,
			int match) {
		ID = id;
		name = tit;
		author = au;
		description = de;
		rating = ra;
		rateCount = rc;
		image = img;
		matchKeys = 0;
		this.keys = keys;
		this.dess = dess;
		matchKeys = match;
	}

	@Override
	public int compareTo(TargetImageResponse arg0) {
		return matchKeys - arg0.matchKeys;
	}
}