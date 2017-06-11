package activitytest.example.com.test01;

        import java.io.File;
        import java.util.Calendar;

        import android.app.Activity;
        import android.content.ContentResolver;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.Uri;
        import android.os.Bundle;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;

        import interfaces.ISketchPadCallback;
        import utils.BitmapUtil;
        import utils.MediaUtil.ImageInfo;
        import view.ColorSelector;
        import view.SketchPadView;
        import view.ColorSelector.ColorSelectorCallback;

public class EditActivity extends BaseActivity implements View.OnClickListener, ISketchPadCallback{
    private static final int SKETCHPAD_PEN_COLOR = 0x01;
    private static final int SKETCHPAD_BK_COLOR = 0x02;

    private SketchPadView m_sketchPad = null;
    private ColorSelector m_penColorSelector = null;

    private int m_mode = 1;
    private int m_size = 5;

    private Button m_penBtn;
    private Button m_eraserBtn = null;
    private Button m_clearBtn = null;
    private Button m_undoBtn = null;
    private Button m_redoBtn = null;
    private Button m_saveBtn = null;
    private Button m_pencolorBtn = null;
    private Button m_bkcolorBtn = null;
    private Button m_penUpBtn = null;
    private Button m_penDownBtn = null;
    private Button m_chooseBtn = null;

    private int m_colorSelectorType = SKETCHPAD_PEN_COLOR;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        m_sketchPad = (SketchPadView)this.findViewById(R.id.sketchpad);
        m_penColorSelector = (ColorSelector)this.findViewById(R.id.pencolorselector);
        m_penColorSelector.setCallback(new ColorSelectorCallback()
        {
            @Override
            public void onColorSelectDone(ColorSelector sender)
            {
                int color = sender.getPenColor();
                switch (m_colorSelectorType)
                {
                    case SKETCHPAD_PEN_COLOR:
                        m_sketchPad.setStrokeColor(color);
                        break;

                    case SKETCHPAD_BK_COLOR:
                        m_sketchPad.setBkColor(color);
                        break;
                }
            }

            @Override
            public void onColorSelectChange(ColorSelector sender)
            {
                int color = sender.getPenColor();
                switch (m_colorSelectorType)
                {
                    case SKETCHPAD_PEN_COLOR:
                        break;

                    case SKETCHPAD_BK_COLOR:
                        m_sketchPad.setBkColor(color);
                        break;
                }
            }

            @Override
            public void onColorSelectCancel(ColorSelector sender)
            {
                int color = sender.getPenColor();
                switch (m_colorSelectorType)
                {
                    case SKETCHPAD_PEN_COLOR:
                        break;

                    case SKETCHPAD_BK_COLOR:
                        m_sketchPad.setBkColor(color);
                        break;
                }
            }
        });

        m_penBtn = (Button)this.findViewById(R.id.pen);
        m_eraserBtn = (Button)this.findViewById(R.id.eraser);
        m_clearBtn = (Button)this.findViewById(R.id.clear);
        m_undoBtn = (Button)this.findViewById(R.id.undo);
        m_redoBtn = (Button)this.findViewById(R.id.redo);
        m_saveBtn = (Button)this.findViewById(R.id.save);
        m_pencolorBtn = (Button)this.findViewById(R.id.penColor);
        m_bkcolorBtn = (Button)this.findViewById(R.id.bkColor);
        m_penUpBtn = (Button)this.findViewById(R.id.penUpSize);
        m_penDownBtn = (Button)this.findViewById(R.id.penDownSize);
        m_chooseBtn = (Button)this.findViewById(R.id.choose);

        m_penBtn.setOnClickListener(this);
        m_eraserBtn.setOnClickListener(this);
        m_clearBtn.setOnClickListener(this);
        m_undoBtn.setOnClickListener(this);
        m_redoBtn.setOnClickListener(this);
        m_saveBtn.setOnClickListener(this);
        m_pencolorBtn.setOnClickListener(this);
        m_bkcolorBtn.setOnClickListener(this);
        m_penUpBtn.setOnClickListener(this);
        m_penDownBtn.setOnClickListener(this);
        m_chooseBtn.setOnClickListener(this);

        m_sketchPad.setCallback(this);

        m_penBtn.setEnabled(false);
        m_clearBtn.setEnabled(false);
        m_redoBtn.setEnabled(false);
        m_undoBtn.setEnabled(false);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.pen:
                onPenClick(v);
                break;

            case R.id.eraser:
                onEraseClick(v);
                break;

            case R.id.clear:
                onClearClick(v);
                break;

            case R.id.undo:
                onUndoClick(v);
                break;

            case R.id.redo:
                onRedoClick(v);
                break;

            case R.id.save:
                onSaveClick(v);
                break;

            case R.id.penColor:
                onPenColorClick(v);
                break;

            case R.id.bkColor:
                onBkColorClick(v);
                break;

            case R.id.penUpSize:
                onPenSizeUpClick(v);
                break;

            case R.id.penDownSize:
                onPenSizeDownClick(v);
                break;

            case R.id.choose:
                onChoosePictureClick(v);
                break;
        }
    }

    protected void onPenClick(View v)
    {
        m_sketchPad.setStrokeType(SketchPadView.STROKE_PEN);
        m_penBtn.setEnabled(false);
        m_eraserBtn.setEnabled(true);
    }

    protected void onEraseClick(View v)
    {
        m_sketchPad.setStrokeType(SketchPadView.STROKE_ERASER);
        m_penBtn.setEnabled(true);
        m_eraserBtn.setEnabled(false);
    }

    protected void onClearClick(View v)
    {
        m_sketchPad.clearAllStrokes();

        m_clearBtn.setEnabled(false);
        m_redoBtn.setEnabled(false);
        m_undoBtn.setEnabled(false);
    }

    protected void onUndoClick(View v)
    {
        m_sketchPad.undo();
        m_undoBtn.setEnabled(m_sketchPad.canUndo());
        m_redoBtn.setEnabled(m_sketchPad.canRedo());
    }

    protected void onRedoClick(View v)
    {
        m_sketchPad.redo();
        m_undoBtn.setEnabled(m_sketchPad.canUndo());
        m_redoBtn.setEnabled(m_sketchPad.canRedo());
    }

    protected void onSaveClick(View v)
    {
        String strFilePath = getStrokeFilePath();
        Bitmap bmp = m_sketchPad.getCanvasSnapshot();
        if (null != bmp)
        {
            BitmapUtil.saveBitmapToSDCard(bmp, strFilePath);
            Intent intent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    protected void onLoadClick(View v)
    {
        String strFilePath = getStrokeFilePath();
        Bitmap bmp = BitmapUtil.loadBitmapFromSDCard(strFilePath);
        if (null != bmp)
        {
            m_sketchPad.setForeBitmap(bmp);
        }

        m_clearBtn.setEnabled(true);
    }

    protected void onPenColorClick(View v)
    {
        m_colorSelectorType = SKETCHPAD_PEN_COLOR;
        if (null != m_penColorSelector)
        {
            m_penColorSelector.setVisibility(View.VISIBLE);
        }
    }

    protected void onBkColorClick(View v)
    {
        m_colorSelectorType = SKETCHPAD_BK_COLOR;
        if (null != m_penColorSelector)
        {
            m_penColorSelector.setVisibility(View.VISIBLE);
        }
    }

    protected void onPenSizeUpClick(View v)
    {
        if(m_size <= 25){
            m_size = m_size + 5;
            m_sketchPad.setStrokeSize(m_size, m_mode);
        }
    }

    protected void onPenSizeDownClick(View v){
        if(m_size >= 10){
            m_size = m_size - 5;
            m_sketchPad.setStrokeSize(m_size, m_mode);
        }
    }

    protected void onChoosePictureClick(View v)
    {
        Intent intent = new Intent(this, PictureSelectActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case 0:
                if (RESULT_OK == resultCode)
                {
                    try
                    {
                        Bundle bundle = data.getExtras();
                        ImageInfo imgInfo = PictureSelectActivity.getImageInfoFromBundle(bundle);
                        Uri uri = imgInfo.imageUri;

                        ContentResolver cr = this.getContentResolver();
                        Bitmap bmp = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        if (null != bmp)
                        {
                            m_sketchPad.setBkBitmap(bmp);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy(SketchPadView obj)
    {
    }

    @Override
    public void onTouchDown(SketchPadView obj, MotionEvent event)
    {
        m_clearBtn.setEnabled(true);
        m_undoBtn.setEnabled(true);
    }

    @Override
    public void onTouchUp(SketchPadView obj, MotionEvent event)
    {
    }

    public String getStrokeFilePath()
    {
        File sdcarddir = android.os.Environment.getExternalStorageDirectory();
        String strDir = sdcarddir.getPath() + "/DCIM/sketchpad/";
        String strFileName = getStrokeFileName();
        File file = new File(strDir);
        if (!file.exists())
        {
            file.mkdirs();
        }

        String strFilePath = strDir + strFileName;

        return strFilePath;
    }

    public String getStrokeFileName()
    {
        String strFileName = "";

        Calendar rightNow = Calendar.getInstance();
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH);
        int date = rightNow.get(Calendar.DATE);
        int hour = rightNow.get(Calendar.HOUR);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);

        strFileName = String.format("%02d%02d%02d%02d%02d%02d.png", year, month, date, hour, minute, second);
        return strFileName;
    }
}