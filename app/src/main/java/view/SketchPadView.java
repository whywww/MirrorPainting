package view;

import java.util.ArrayList;



import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.DisplayMetrics;

import interfaces.ISketchPadCallback;
import interfaces.ISketchPadTool;
import interfaces.IUndoCommand;
import utils.BitmapUtil;
import utils.CommonDef;
import utils.SketchPadEraser;
import utils.SketchPadPen;

public class SketchPadView extends View implements IUndoCommand
{
    public static final int STROKE_NONE = 0;
    public static final int STROKE_PEN = 1;
    public static final int STROKE_ERASER = 2;
    public static final int UNDO_SIZE = 20;

    private boolean m_isEnableDraw = true;
    private boolean m_isDirty = false;
    private boolean m_isTouchUp = false;
    private boolean m_isSetForeBmp = false;
    private int m_bkColor = Color.WHITE;

    private int m_strokeType = STROKE_PEN;
    private int m_strokeColor = Color.BLACK;
    private int m_penSize = CommonDef.MIDDLE_PEN_WIDTH;
    private int m_eraserSize = CommonDef.MIDDLE_ERASER_WIDTH;
    private int m_canvasWidth;
    private int m_canvasHeight;
    private boolean m_canClear = true;

    private Paint m_bitmapPaint = null;
    private Bitmap m_foreBitmap = null;
    private SketchPadUndoStack m_undoStackL = null;
    private SketchPadUndoStack m_undoStackR = null;
    private Bitmap m_tempForeBitmap = null;
    private Bitmap m_bkBitmap = null;
    private Canvas m_canvas = null;
    private ISketchPadTool m_curToolR = null;
    private ISketchPadTool m_curToolL = null;
    private ISketchPadCallback m_callback = null;

    public SketchPadView(Context context)
    {
        this(context, null);
    }

    public SketchPadView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize();
    }

    public SketchPadView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize();
    }

    public void getSCREEN_WH(){
        DisplayMetrics dm = new DisplayMetrics();
        dm = this.getResources().getDisplayMetrics();

        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        m_canvasWidth = screenWidth;
        m_canvasHeight = screenHeight;
    }

    public boolean isDirty()
    {
        return m_isDirty;
    }

    public void setDrawStrokeEnable(boolean isEnable)
    {
        m_isEnableDraw = isEnable;
    }

    public void setBkColor(int color)
    {
        if (m_bkColor != color)
        {
            m_bkColor = color;
            invalidate();
        }
    }

    public void setForeBitmap(Bitmap foreBitmap)
    {
        if (foreBitmap != m_foreBitmap && null != foreBitmap)
        {
            // Recycle the bitmap.
            if (null != m_foreBitmap)
            {
                m_foreBitmap.recycle();
            }

            m_isSetForeBmp = true;

            // Remember the temporary fore bitmap.
            m_tempForeBitmap = BitmapUtil.duplicateBitmap(foreBitmap);

            // Here create a new fore bitmap to avoid crashing when set bitmap to canvas.
            m_foreBitmap = BitmapUtil.duplicateBitmap(foreBitmap);
            if (null != m_foreBitmap && null != m_canvas)
            {
                m_canvas.setBitmap(m_foreBitmap);
            }

            m_canClear = true;

            invalidate();
        }
    }

    public Bitmap getForeBitmap()
    {
        return m_foreBitmap;
    }

    public void setBkBitmap(Bitmap bmp)
    {
        if (m_bkBitmap != bmp)
        {
            m_bkBitmap = bmp;
            invalidate();
        }
    }

    public Bitmap getBkBitmap()
    {
        return m_bkBitmap;
    }

    public void setStrokeType(int type)
    {
        switch(type)
        {
            case STROKE_PEN:
                m_curToolR = new SketchPadPen(m_penSize, m_strokeColor);
                m_curToolL = new SketchPadPen(m_penSize, m_strokeColor);
                break;

            case STROKE_ERASER:
                m_curToolR = new SketchPadEraser(m_eraserSize);
                m_curToolL = new SketchPadEraser(m_eraserSize);
                break;
        }

        m_strokeType = type;
    }

    public void setStrokeSize(int size, int type)
    {
        switch(type)
        {
            case STROKE_PEN:
                m_penSize = size;
                break;

            case STROKE_ERASER:
                m_eraserSize = size;
                break;
        }
    }

    public void setStrokeColor(int color)
    {
        m_strokeColor = color;
    }

    public int getStrokeSize()
    {
        return m_penSize;
    }

    public int getStrokeColor()
    {
        return m_strokeColor;
    }

    public void clearAllStrokes()
    {
        if (m_canClear)
        {
            // Clear the undo stack.
            m_undoStackR.clearAll();
            m_undoStackL.clearAll();

            // Recycle the temporary fore bitmap
            if (null != m_tempForeBitmap)
            {
                m_tempForeBitmap.recycle();
                m_tempForeBitmap = null;
            }

            // Create a new fore bitmap and set to canvas.
            createStrokeBitmap(m_canvasWidth, m_canvasHeight);

            invalidate();
            m_isDirty = true;
            m_canClear = false;
        }
    }

    public Bitmap getCanvasSnapshot()
    {
        setDrawingCacheEnabled(true);
        buildDrawingCache(true);
        Bitmap bmp = getDrawingCache(true);

        if (null == bmp)
        {
            android.util.Log.d("leehong2", "getCanvasSnapshot getDrawingCache == null");
        }

        return BitmapUtil.duplicateBitmap(bmp);
    }

    public void setCallback(ISketchPadCallback callback)
    {
        m_callback = callback;
    }

    public ISketchPadCallback getCallback()
    {
        return m_callback;
    }

    @Override
    public void onDeleteFromRedoStack()
    {
        // Do nothing currently.
    }

    @Override
    public void onDeleteFromUndoStack()
    {
        // Do nothing currently.
    }

    @Override
    public void redo()
    {
        if (null != m_undoStackL)
        {
            m_undoStackR.redo();
            m_undoStackL.redo();
        }
    }

    @Override
    public void undo()
    {
        if (null != m_undoStackL)
        {
            m_undoStackR.undo();
            m_undoStackL.undo();
        }
    }

    @Override
    public boolean canUndo()
    {
        if ( null != m_undoStackL )
        {
            return ( m_undoStackL.canUndo());
        }

        return false;
    }

    @Override
    public boolean canRedo()
    {
        if ( null != m_undoStackL)
        {
            return (m_undoStackL.canRedo());
        }

        return false;
    }

    public boolean onRight(int x){
        if(x>m_canvasWidth/2||x<m_canvasWidth){
            return true;
        }
        else return false;
    }

    @Override

    public boolean onTouchEvent(MotionEvent event)
    {

        if (null != m_callback)
        {
            int action = event.getAction();
            if (MotionEvent.ACTION_DOWN == action)
            {
                m_callback.onTouchDown(this, event);
            }
            else if (MotionEvent.ACTION_UP == action)
            {
                m_callback.onTouchUp(this, event);
            }
        }



        if (m_isEnableDraw)
        {
            m_isTouchUp = false;
            int Rx = (int)event.getX();
            int y = (int)event.getY();
            int Lx = m_canvasWidth - Rx;
//                if(onRight((int)event.getX())){
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    // This will create a new stroke tool instance with specified stroke type.
                    // And the created tool will be added to undo stack.
                    setStrokeType(m_strokeType);
                    m_curToolL.touchDown(Lx, y);
                    m_curToolR.touchDown(Rx,y);

                    invalidate();
                    break;

                case MotionEvent.ACTION_MOVE:
                    m_curToolL.touchMove(Lx,y);
                    m_curToolR.touchMove(Rx, y);

                    if (STROKE_ERASER == m_strokeType)
                    {
                        m_curToolL.draw(m_canvas);
                        m_curToolR.draw(m_canvas);

                    }
                    // If current stroke type is eraser, draw strokes on bitmap hold by m_canvas.

                    invalidate();
                    m_isDirty = true;
                    m_canClear = true;
                    break;

                case MotionEvent.ACTION_UP:
                    m_isTouchUp = true;
//                        if (m_curToolL.hasDraw() && m_curToolR.hasDraw())
//                        {
                    // Add to undo stack.

                    m_undoStackL.push(m_curToolR);
                    m_undoStackL.push(m_curToolL);

//                        }

                    m_curToolL.touchUp(Lx, y);
                    // Draw strokes on bitmap which is hold by m_canvas.
                    m_curToolL.draw(m_canvas);

                    m_curToolR.touchUp(Rx, y);
                    m_curToolR.draw(m_canvas);



                    invalidate();
                    m_isDirty = true;
                    m_canClear = true;
                    break;
            }
        }
//        }
        // Here must return true if enable to draw, otherwise the stroke may NOT be drawn.
        return true;
    }

    protected void setCanvasSize(int width, int height)
    {
        if (width > 0 && height > 0)
        {
            if (m_canvasWidth != width || m_canvasHeight != height)
            {
                m_canvasWidth = width;
                m_canvasHeight = height;

                createStrokeBitmap(m_canvasWidth, m_canvasHeight);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // Draw background color.
        canvas.drawColor(m_bkColor);

        // Draw background bitmap.
        if (null != m_bkBitmap)
        {
            RectF dst = new RectF(getLeft(), getTop(), getRight(), getBottom());
            Rect  rst = new Rect(0, 0, m_bkBitmap.getWidth(), m_bkBitmap.getHeight());
            canvas.drawBitmap(m_bkBitmap, rst, dst, m_bitmapPaint);
        }

        if (null != m_foreBitmap)
        {
            canvas.drawBitmap(m_foreBitmap, 0, 0, m_bitmapPaint);
        }

        if (null != m_curToolR && null != m_curToolL)
        {
            // Do NOT draw current tool stroke real time if stroke type is NOT eraser, because
            // eraser is drawn on bitmap hold by m_canvas.
            if (STROKE_ERASER != m_strokeType)
            {
                // We do NOT draw current tool's stroke to canvas when ACTION_UP event is occurring,
                // because the stroke has been drawn to bitmap hold by m_canvas. But the tool will be
                // drawn if undo or redo operation is performed.
                if (!m_isTouchUp)
                {
                    m_curToolL.draw(canvas);
                    m_curToolR.draw(canvas);

                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        // If NOT set fore bitmap, call setCanvasSize() method to set canvas size, it will
        // create a new fore bitmap and set to canvas.
        if (!m_isSetForeBmp)
        {
            setCanvasSize(w, h);
        }

        m_canvasWidth = w;
        m_canvasHeight = h;

        m_isSetForeBmp = false;
    }

    protected void setTempForeBitmap(Bitmap tempForeBitmap)
    {
        if (null != tempForeBitmap)
        {
            if (null != m_foreBitmap)
            {
                m_foreBitmap.recycle();
            }

            m_foreBitmap = BitmapUtil.duplicateBitmap(tempForeBitmap);

            if (null != m_foreBitmap && null != m_canvas)
            {
                m_canvas.setBitmap(m_foreBitmap);
                invalidate();
            }
        }
    }

    protected void initialize()
    {
        m_canvas = new Canvas();
        m_bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_undoStackR = new SketchPadUndoStack(this, UNDO_SIZE);
        m_undoStackL = new SketchPadUndoStack(this, UNDO_SIZE);

        // Set stroke type and create a stroke tool.
        setStrokeType(STROKE_PEN);
    }

    protected void createStrokeBitmap(int w, int h)
    {
        m_canvasWidth = w;
        m_canvasHeight = h;

        Bitmap bitmap = Bitmap.createBitmap(m_canvasWidth, m_canvasHeight, Bitmap.Config.ARGB_8888);
        if (null != bitmap)
        {
            m_foreBitmap = bitmap;
            // Set the fore bitmap to m_canvas to be as canvas of strokes.
            m_canvas.setBitmap(m_foreBitmap);
        }
    }

    public class SketchPadUndoStack
    {
        private int m_stackSize = 0;
        private SketchPadView m_sketchPad = null;
        private ArrayList<ISketchPadTool> m_undoStackR = new ArrayList<ISketchPadTool>();
        private ArrayList<ISketchPadTool> m_redoStackR = new ArrayList<ISketchPadTool>();
        private ArrayList<ISketchPadTool> m_undoStackL = new ArrayList<ISketchPadTool>();
        private ArrayList<ISketchPadTool> m_redoStackL = new ArrayList<ISketchPadTool>();
//        private ArrayList<ISketchPadTool> m_removedStackR = new ArrayList<ISketchPadTool>();
//        private ArrayList<ISketchPadTool> m_removedStackL = new ArrayList<ISketchPadTool>();

        public SketchPadUndoStack(SketchPadView sketchPad, int stackSize)
        {
            m_sketchPad = sketchPad;
            m_stackSize = stackSize;
        }

        public void push(ISketchPadTool sketchPadTool)
        {
            if (null != sketchPadTool)
            {
                if (m_undoStackR.size() == m_stackSize && m_stackSize > 0&&m_undoStackL.size() == m_stackSize)
                {
//                    ISketchPadTool removedTool1 = m_undoStackR.get(0);
//                    m_removedStackR.add(removedTool1);
                    m_undoStackR.remove(0);

//                    ISketchPadTool removedTool2 = m_undoStackL.get(0);
//                    m_removedStackL.add(removedTool2);
                    m_undoStackL.remove(0);
                }
                m_undoStackL.add(sketchPadTool);
                m_undoStackR.add(sketchPadTool);
            }
        }

        public void clearAll()
        {
            m_redoStackR.clear();
            m_undoStackR.clear();
//            m_removedStackR.clear();
            m_redoStackL.clear();
            m_undoStackL.clear();
//            m_removedStackL.clear();
        }

        public void undo()
        {
            if (canUndo() && null != m_sketchPad)
            {
                ISketchPadTool removedTool1 = m_undoStackR.get(m_undoStackR.size() - 1);
                m_redoStackR.add(removedTool1);
                m_undoStackR.remove(m_undoStackR.size() - 1);

                ISketchPadTool removedTool2 = m_undoStackL.get(m_undoStackL.size() - 1);
                m_redoStackL.add(removedTool2);
                m_undoStackL.remove(m_undoStackL.size() - 1);
                if (null != m_tempForeBitmap)
                {
                    // Set the temporary fore bitmap to canvas.
                    m_sketchPad.setTempForeBitmap(m_sketchPad.m_tempForeBitmap);
                }
                else
                {
                    // Create a new bitmap and set to canvas.
                    m_sketchPad.createStrokeBitmap(m_sketchPad.m_canvasWidth, m_sketchPad.m_canvasHeight);
                }

                Canvas canvas = m_sketchPad.m_canvas;


//				Iterator<ISketchPadTool> Riter2 = m_undoStackR.iterator();
//				Iterator<ISketchPadTool> Liter2 = m_undoStackL.iterator();
//				while(Riter2.hasNext() && Liter2.hasNext()){
//					ISketchPadTool Rdp = Riter2.next();
//					Rdp.draw(m_canvas);
//
//					ISketchPadTool Ldp = Liter2.next();
//					Ldp.draw(m_canvas);
//
//				}
//                 First draw the removed tools from undo stack.
//                for (ISketchPadTool sketchPadTool : m_removedStackR)
//                {
//                    sketchPadTool.draw(canvas);
//                }
                Iterator<ISketchPadTool> iterL = m_undoStackL.iterator();
                Iterator<ISketchPadTool> iterR = m_undoStackR.iterator();
                while(iterL.hasNext()){
                    ISketchPadTool temp1 = iterL.next();
//                    ISketchPadTool temp2 = iterR.next();
                    temp1.draw(canvas);
//                    temp2.draw(canvas);
                }
//                for (ISketchPadTool sketchPadTool : m_undoStackL)
//                {
//                    sketchPadTool.draw(canvas);
//                }
//                for (ISketchPadTool sketchPadTool : m_undoStackR)
//                {
//                    sketchPadTool.draw(canvas);
//                }
//                for (ISketchPadTool sketchPadTool : m_removedStackL)
//                {
//                    sketchPadTool.draw(canvas);
//                }



                m_sketchPad.invalidate();
            }
        }

        public void redo()
        {
            if (canRedo() && null != m_sketchPad)
            {
                ISketchPadTool removedTool1 = m_redoStackR.get(m_redoStackR.size() - 1);
                m_undoStackR.add(removedTool1);
                m_redoStackR.remove(m_redoStackR.size() - 1);

                ISketchPadTool removedTool2 = m_redoStackL.get(m_redoStackL.size() - 1);
                m_undoStackL.add(removedTool2);
                m_redoStackL.remove(m_redoStackL.size() - 1);

                if (null != m_tempForeBitmap)
                {
                    // Set the temporary fore bitmap to canvas.
                    m_sketchPad.setTempForeBitmap(m_sketchPad.m_tempForeBitmap);
                }
                else
                {
                    // Create a new bitmap and set to canvas.
                    m_sketchPad.createStrokeBitmap(m_sketchPad.m_canvasWidth, m_sketchPad.m_canvasHeight);
                }

                Canvas canvas = m_sketchPad.m_canvas;

                // First draw the removed tools from undo stack.
//                for (ISketchPadTool sketchPadTool : m_removedStackR)
//                {
//                    sketchPadTool.draw(canvas);
//                }
//                for (ISketchPadTool sketchPadTool : m_removedStackL)
//                {
//                    sketchPadTool.draw(canvas);
//                }
                for (ISketchPadTool sketchPadTool : m_undoStackR)
                {
                    sketchPadTool.draw(canvas);
                }
                for (ISketchPadTool sketchPadTool : m_undoStackL)
                {
                    sketchPadTool.draw(canvas);
                }
                m_sketchPad.invalidate();
            }
        }

        public boolean canUndo()
        {
            return (m_undoStackR.size() > 0 && m_undoStackL.size() > 0);
        }


        public boolean canRedo()
        {
            return (m_redoStackR.size() > 0 && m_undoStackL.size() > 0);
        }
    }
}