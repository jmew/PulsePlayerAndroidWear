/*
 * Copyright Kirill Morozov 2012
 * 
 * 
	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package jmew.ca.pulseplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineGraphView extends View
{
	private List<Float> points = new ArrayList<>();

	private Paint linePaint = new Paint();
	private Paint graphPaint = new Paint();

	private final int WIDTH = 1000;
	private final int HEIGHT = 1000;
	private float xScale, yScale;
	private final int maxDataWidth;

	public LineGraphView(Context context, int dataWidth) {
		super(context);

		linePaint.setStrokeWidth(6f);
		graphPaint.setStrokeWidth(6f);
		maxDataWidth = dataWidth;
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(WIDTH, HEIGHT);
	}

	private void drawLine(Canvas canvas, int count, Float rawSrc, Float rawDest, Paint paint){
		float graphSrcX, graphSrcY, graphDestX, graphDestY;

		xScale = WIDTH / (points.size()+1);
		yScale = (HEIGHT / 2);

		graphSrcX = (count - 1) * xScale;
		graphSrcY = HEIGHT - (rawSrc * yScale + (HEIGHT / 2));
		graphDestX = (count) * xScale;
		graphDestY = HEIGHT - (rawDest * yScale + (HEIGHT / 2));

		canvas.drawLine(graphSrcX, graphSrcY, graphDestX, graphDestY, paint);
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		Log.e("drawing", "drawing");
		for(int j = 1; j < points.size(); j++){
			drawLine(canvas, j, points.get(j-1), points.get(j), linePaint);
		}

	}

	public void addPoint(float y){
		points.add(y);
		if(points.size() > maxDataWidth)
			points.remove(0);
		invalidate();
	}

	public void purge(){
		points.clear();
		invalidate();
	}
}