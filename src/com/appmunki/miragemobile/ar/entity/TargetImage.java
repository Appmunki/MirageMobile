package com.appmunki.miragemobile.ar.entity;

import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import com.orm.androrm.field.BlobField;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;

public class TargetImage {

	protected IntegerField _ID, _rateCount, _width, _height;
	protected CharField _name, _author, _description, _image, _bigImg;
	protected DoubleField _rating;
	protected BlobField _keys, _dess;

	public Vector<KeyPoint> keys;
	public Mat dess;

}
