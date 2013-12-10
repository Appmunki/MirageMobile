package com.entity;

import java.util.Vector;

import android.content.Context;

import com.appmunki.miragemobile.utils.Util;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.BlobField;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;

/**
 * Contain infomations about a targetimage including id, rating, title, author,
 * comment, tags, price, image, keypoints and descriptors.
 * 
 * @author Radzell
 * 
 */
public class TargetImage extends Model implements Comparable<TargetImage> {
	protected IntegerField _ID, _rateCount, _width, _height;
	protected CharField _name, _author, _description, _image, _bigImg;
	protected DoubleField _rating;
	protected BlobField _keys, _dess;

	@SuppressWarnings("unchecked")
	public Vector<KeyPoint> getkeys() {
		return (Vector<KeyPoint>) Util.objectFromByteArray(_keys.get());
	}

	public void setkeys(Vector<KeyPoint> _keys) {
		this._keys.set(Util.objectToByteArray(_keys));
	}

	public Mat getdess() {
		return (Mat) Util.objectFromByteArray(_dess.get());
	}

	public void setdess(Mat _dess) {
		this._dess.set(Util.objectToByteArray(_dess));
	}

	public void init() {
		_ID = new IntegerField();
		_rateCount = new IntegerField();
		_width = new IntegerField();
		_height = new IntegerField();
		_name = new CharField();
		_author = new CharField();
		_description = new CharField();
		_image = new CharField();
		_bigImg = new CharField();
		_rating = new DoubleField();
		_keys = new BlobField();
		_dess = new BlobField();
	}

	/**
	 * Create a empty targetimage
	 */
	public TargetImage() {
		init();
	}

	/**
	 * Copy data from another targetimage to this targetimage
	 * 
	 * @param b
	 */
	public TargetImage(TargetImageResponse b) {
		init();
		_ID.set(b.ID);
		_rateCount.set(b.rateCount);
		_width.set(b.width);
		_height.set(b.height);
		_name.set(b.name);
		_author.set(b.author);
		_description.set(b.description);
		_image.set(b.image);
		_bigImg.set(b.bigImg);
		_rating.set((double) b.rating);
		_keys.set(Util.objectToByteArray(b.keys));
		_dess.set(Util.objectToByteArray(b.dess));
	}

	/**
	 * Create a targetimage without keypoints and descriptor
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
	public TargetImage(int id, String tit, String au, String de, float ra,
			int rc, String img) {

		_ID.set(id);
		_rateCount.set(rc);
		_name.set(tit);
		_author.set(au);
		_description.set(de);
		_image.set(img);

	}

	/**
	 * Create a targetimage with keypoints and descriptors
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
	public TargetImage(int id, String tit, String au, String de, float ra,
			int rc, String img, Vector<KeyPoint> keys, Mat dess) {

		_ID.set(id);
		_rateCount.set(rc);
		_name.set(tit);
		_author.set(au);
		_description.set(de);
		_image.set(img);
		_rating.set((double) ra);
		_keys.set(Util.objectToByteArray(keys));
		_dess.set(Util.objectToByteArray(dess));

	}

	public TargetImage(int id, String tit, String au, String de, float ra,
			int rc, String img, Vector<KeyPoint> keys, Mat dess, int width,
			int height) {
		init();
		_ID.set(id);
		_rateCount.set(rc);
		_width.set(width);
		_height.set(height);
		_name.set(tit);
		_author.set(au);
		_description.set(de);
		_image.set(img);
		_rating.set((double) ra);
		_keys.set(Util.objectToByteArray(keys));
		_dess.set(Util.objectToByteArray(dess));
	}

	/**
	 * Create a targetimage with keypoints, descritors and number of matches
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
	public TargetImage(int id, String tit, String au, String de, float ra,
			int rc, String img, Vector<KeyPoint> keys, Mat dess, int match) {

		_ID.set(id);
		_rateCount.set(rc);

		_name.set(tit);
		_author.set(au);
		_description.set(de);
		_image.set(img);
		_rating.set((double) ra);
		_keys.set(Util.objectToByteArray(keys));
		_dess.set(Util.objectToByteArray(dess));
	}

	@Override
	public int compareTo(TargetImage arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final QuerySet<TargetImage> objects(Context context) {
		return objects(context, TargetImage.class);
	}

	public int getheight() {
		// TODO Auto-generated method stub
		return _height.get();
	}

	public int getwidth() {
		// TODO Auto-generated method stub
		return _width.get();
	}

	public String getname() {
		// TODO Auto-generated method stub
		return _name.get();
	}
}