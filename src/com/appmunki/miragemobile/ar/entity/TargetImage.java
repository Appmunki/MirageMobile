package com.appmunki.miragemobile.ar.entity;

import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import com.orm.androrm.Model;
import com.orm.androrm.field.BlobField;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;

public class TargetImage extends Model{

	public IntegerField _ID, _rateCount, _width, _height;
	public CharField _name, _author, _description, _image, _bigImg;
	public DoubleField _rating;
	public BlobField _keys, _dess;

	public Vector<KeyPoint> keys;
	public Mat dess;
	
	public TargetImage() {
		super(true);

		_name = new CharField();
		_author = new CharField();
		_description = new CharField();
		_image = new CharField();
		_bigImg = new CharField();
		
		_ID = new IntegerField();
		_width = new IntegerField();
		_height = new IntegerField();
		_rateCount = new IntegerField();
		
		_rating = new DoubleField();
		
		keys = new Vector<KeyPoint>();
		dess = new Mat();
	}

}
